using Windows.Graphics;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Windowing;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Shapes;

namespace Known.Windows;

/// <summary>
///     The three-step onboarding wizard: who the student is, where they are, and which subjects they
///     sit. Finishing writes the answers and clears the first login flag, so the shell opens the
///     dashboard next time.
/// </summary>
public sealed partial class GuideWindow
{
    private const int StepCount = 3;

    /// <summary>The frame the wizard opens at: portrait, and narrower than it is tall.</summary>
    private const int DefaultWidth = 460;

    private const int DefaultHeight = 720;

    /// <summary>
    ///     The smallest the frame may be dragged to. These match the minimums on the root grid, and
    ///     the presenter is what actually stops the drag rather than the layout merely refusing to
    ///     shrink.
    /// </summary>
    private const int MinimumWidth = 420;

    private const int MinimumHeight = 620;

    /// <summary>
    ///     The frame is never worth enlarging past this, so there is nothing to maximise into. The
    ///     limits have to clear the default size with room to spare: the presenter clamps
    ///     <see cref="AppWindow.Resize" /> too, so a default that reached the ceiling on a scaled
    ///     display would silently open smaller than asked for.
    /// </summary>
    private const int MaximumWidth = 720;

    private const int MaximumHeight = 1100;

    private readonly HashSet<string> _selectedSubjects = new(StringComparer.Ordinal);
    private readonly StringLocalizer _strings = new();
    private readonly ThemeService _themeService;

    private readonly TaskCompletionSource _dismissed = new(TaskCreationOptions.RunContinuationsAsynchronously);

    private bool _activated;
    private bool _completed;
    private bool _finishing;
    private bool _locating;
    private int _identity;
    private string _region;
    private int _step;

    /// <summary>
    ///     Completes once the window has closed and the first login flag has been dealt with. The
    ///     caller waits on this rather than on <see cref="Window.Closed" />, because closing the
    ///     wizard also writes that flag, and the shell opening before the writing landed would race it.
    /// </summary>
    public Task Dismissed => _dismissed.Task;

    /// <summary>Creates the wizard, fills in each step and shows the first one.</summary>
    public GuideWindow()
    {
        InitializeComponent();

        _themeService = AppServices.Theme;
        _themeService.ThemeChanged += OnThemeChanged;

        _identity = AppServices.Settings.Current.Identity;
        _region = AppServices.Settings.Current.Region;

        foreach (var key in AppServices.Settings.SelectedSubjects) _selectedSubjects.Add(key);

        Title = _strings.Get("app_name");

        ApplyCaptions();
        BuildIdentityStep();
        BuildRegionStep();
        BuildSubjectStep();
        BuildDots();
        ConfigurePresenter();
        ShowStep(0);
        ApplyTheme();

        Activated += OnFirstActivated;
        Closed += OnClosed;
    }

    /// <summary>
    ///     Clamps the frame to a portrait wizard and stops it being maximised. The presenter is applied
    ///     before the window is shown so the limits are in place from the first frame, rather than the
    ///     wizard appearing at some arbitrary size and then snapping.
    /// </summary>
    private void ConfigurePresenter()
    {
        var presenter = OverlappedPresenter.Create();

        // A hard maximum and maximisation do not go together: a maximised window would be clamped to
        // the maximum anyway, which looks like a broken maximise button
        presenter.IsMaximizable = false;
        presenter.PreferredMinimumWidth = MinimumWidth;
        presenter.PreferredMinimumHeight = MinimumHeight;
        presenter.PreferredMaximumWidth = MaximumWidth;
        presenter.PreferredMaximumHeight = MaximumHeight;

        AppWindow.SetPresenter(presenter);
    }

    /// <summary>
    ///     Sizes and centres the frame the first time it is shown. The initial size is left until now
    ///     because it is expressed in physical pixels and the scale factor of the display it lands on
    ///     is not known until the window has one.
    /// </summary>
    private void OnFirstActivated(object sender, WindowActivatedEventArgs args)
    {
        // The wizard is shown once and then dismissed, so there is never a second pass worth sizing
        if (_activated) return;

        _activated = true;

        // The system theme can have changed while the app was starting, and nothing would have raised
        // a notification yet, so this is the first moment the wizard can pick up a "follow system"
        // preference that no longer matches what it rendered with
        _themeService.Refresh();

        if (Content is not FrameworkElement root) return;

        var scale = root.XamlRoot?.RasterizationScale ?? 1d;

        var width = (int)(DefaultWidth * scale);
        var height = (int)(DefaultHeight * scale);

        var workArea = DisplayArea.GetFromWindowId(AppWindow.Id, DisplayAreaFallback.Nearest)?.WorkArea;

        // Without a work area there is nothing to centre against, but the wizard is still worth
        // opening at its intended size rather than at whatever the window defaults to
        if (workArea is not { } area)
        {
            AppWindow.Resize(new SizeInt32(width, height));
            return;
        }

        AppWindow.MoveAndResize(new RectInt32(
            area.X + (area.Width - width) / 2,
            area.Y + (area.Height - height) / 2,
            width,
            height));
    }

    /// <summary>Window title and the captions that are the same on every step.</summary>
    private void ApplyCaptions()
    {
        WelcomeText.Text = _strings.Get("onboarding_welcome");
        DescriptionText.Text = _strings.Get("onboarding_description");

        IdentityHeading.Text = _strings.Get("onboarding_identity");
        RegionHeading.Text = _strings.Get("onboarding_region");
        SubjectHeading.Text = _strings.Get("onboarding_subjects");

        SkipButton.Content = _strings.Get("skip");
        BackButton.Content = _strings.Get("back_step");
        LocateCaption.Text = _strings.Get("use_location");
    }

    private void BuildIdentityStep()
    {
        IdentityOptions.ItemsSource = new[]
        {
            _strings.Get("student"),
            _strings.Get("teacher"),
            _strings.Get("parent")
        };

        // RadioButtons reports the selection by index, so the identity is derived from it
        IdentityOptions.SelectedIndex = IndexOfIdentity(_identity);
        IdentityOptions.SelectionChanged += (_, _) =>
        {
            if (IdentityOptions.SelectedIndex >= 0) _identity = IdentityForIndex(IdentityOptions.SelectedIndex);

            UpdatePrimaryButton();
        };
    }

    private void BuildRegionStep()
    {
        RegionBox.Text = _region;
        RegionBox.TextChanged += (_, _) =>
        {
            _region = RegionBox.Text ?? string.Empty;
            UpdatePrimaryButton();
        };
    }

    private void BuildSubjectStep()
    {
        foreach (var key in SubjectCatalogue.SubjectKeys)
        {
            var box = new CheckBox
            {
                Content = SubjectLabels.NameOf(_strings, key),
                Tag = key,
                MinWidth = 180,
                Margin = new Thickness(0, 0, 12, 8),
                IsChecked = _selectedSubjects.Contains(key)
            };

            box.Checked += OnSubjectToggled;
            box.Unchecked += OnSubjectToggled;

            SubjectGrid.Items.Add(box);
        }
    }

    /// <summary>Draws one dot per step, sized and coloured to show which one is showing.</summary>
    private void BuildDots()
    {
        var dots = new List<Ellipse>();

        for (var index = 0; index < StepCount; index++)
            dots.Add(new Ellipse
            {
                Width = 8,
                Height = 8,
                VerticalAlignment = VerticalAlignment.Center
            });

        Dots.ItemsSource = dots;
    }

    private void OnSubjectToggled(object sender, RoutedEventArgs e)
    {
        if (sender is not CheckBox box || box.Tag is not string key) return;

        if (box.IsChecked == true)
            _selectedSubjects.Add(key);
        else
            _selectedSubjects.Remove(key);

        UpdatePrimaryButton();
    }

    /// <summary>
    ///     Shows one step at a time. All three are alive the whole time, so stepping back and forth
    ///     keeps whatever the student already typed.
    /// </summary>
    private void ShowStep(int index)
    {
        _step = Math.Clamp(index, 0, StepCount - 1);

        var isFirstStep = _step == 0;
        var isLastStep = _step == StepCount - 1;

        IdentityPanel.Visibility = _step == 0 ? Visibility.Visible : Visibility.Collapsed;
        RegionPanel.Visibility = _step == 1 ? Visibility.Visible : Visibility.Collapsed;
        SubjectPanel.Visibility = isLastStep ? Visibility.Visible : Visibility.Collapsed;

        // There is nowhere to go back to from the first step, and the wizard has a skip button, which
        // is hidden on the final step where finishing takes its place
        BackButton.Visibility = isFirstStep ? Visibility.Collapsed : Visibility.Visible;
        SkipButton.Visibility = isLastStep ? Visibility.Collapsed : Visibility.Visible;
        PrimaryButton.Content = isLastStep ? _strings.Get("get_started") : _strings.Get("next");

        UpdateDots();
        UpdatePrimaryButton();
    }

    /// <summary>
    ///     The primary button only becomes live once the step on screen has been answered, so the
    ///     wizard cannot be walked past a question it never asked. Each step asks for something
    ///     different, and the region one counts the trimmed text rather than the raw value, because a
    ///     box holding only spaces is not an answer.
    /// </summary>
    private void UpdatePrimaryButton()
    {
        PrimaryButton.IsEnabled = _step switch
        {
            0 => IdentityOptions.SelectedIndex >= 0,
            1 => !string.IsNullOrWhiteSpace(_region),
            _ => _selectedSubjects.Count > 0
        };
    }

    /// <summary>
    ///     Asks Windows where the student is and fills the region box with the city. A failure is
    ///     reported under the button and leaves the box exactly as it was, because the student can
    ///     always type the region instead.
    /// </summary>
    private async void OnLocate(object sender, RoutedEventArgs e)
    {
        if (_locating) return;

        _locating = true;

        SetLocateBusy(true);
        HideLocateMessage();

        try
        {
            var result = await AppServices.Location.DetectCityAsync();

            if (result.HasCity)
            {
                // Assigning the text raises TextChanged, which is what re-enables the primary button
                RegionBox.Text = result.City!;
                return;
            }

            ShowLocateMessage(_strings.Get(result.Message!));
        }
        catch (Exception)
        {
            // The prompt was declined, location services are off, the machine is offline or the
            // endpoint refused. None of those are worth an unhandled exception over a convenience
            // button, and every one of them leaves the region box exactly as it was
            ShowLocateMessage(_strings.Get("location_no_data"));
        }
        finally
        {
            _locating = false;
            SetLocateBusy(false);
        }
    }

    /// <summary>Greys out the button and shows the spinner while a lookup is running.</summary>
    private void SetLocateBusy(bool busy)
    {
        LocateButton.IsEnabled = !busy;
        LocateProgress.IsActive = busy;
        LocateProgress.Visibility = busy ? Visibility.Visible : Visibility.Collapsed;
    }

    private void ShowLocateMessage(string message)
    {
        LocateMessage.Text = message;
        LocateMessage.Visibility = Visibility.Visible;
    }

    private void HideLocateMessage()
    {
        LocateMessage.Text = string.Empty;
        LocateMessage.Visibility = Visibility.Collapsed;
    }

    private void UpdateDots()
    {
        if (Dots.ItemsSource is not IEnumerable<Ellipse> dots) return;

        var accent = (Brush)Application.Current.Resources["ChartAccentBrush"];
        var muted = (Brush)Application.Current.Resources["ChartGridLineBrush"];

        var index = 0;

        foreach (var dot in dots)
        {
            dot.Fill = index == _step ? accent : muted;
            dot.Opacity = index == _step ? 1d : 0.6d;
            index++;
        }
    }

    private void OnBack(object sender, RoutedEventArgs e)
    {
        ShowStep(_step - 1);
    }

    private async void OnPrimaryAction(object sender, RoutedEventArgs e)
    {
        if (_step < StepCount - 1)
        {
            ShowStep(_step + 1);
            return;
        }

        await FinishAsync();
    }

    /// <summary>
    ///     Skipping is the same gesture as closing the window: the wizard has nothing worth keeping
    ///     once the student dismisses it, and both routes end up in <see cref="OnClosed" />.
    /// </summary>
    private void OnSkip(object sender, RoutedEventArgs e)
    {
        Close();
    }

    private async Task FinishAsync()
    {
        if (_finishing) return;

        _finishing = true;

        try
        {
            var settings = AppServices.Settings;

            await settings.SetIdentityAsync(_identity);
            await settings.SetRegionAsync(_region);
            await settings.SetSelectedSubjectsAsync(_selectedSubjects);
            await settings.CompleteOnboardingAsync();

            _completed = true;
            Close();
        }
        finally
        {
            _finishing = false;
        }
    }

    private void OnThemeChanged(object? sender, EventArgs e)
    {
        ApplyTheme();
    }

    private void ApplyTheme()
    {
        if (Content is not FrameworkElement root) return;

        root.RequestedTheme = _themeService.ShouldUseAppTheme
            ? ElementTheme.Dark
            : ElementTheme.Light;

        UpdateDots();
    }

    /// <summary>
    ///     Closing the window, with the title bar buttons or the skip button, counts as declining the
    ///     wizard, so the first login flag is cleared either way. Leaving it set would put the same
    ///     three questions back on screen at every launch, which is not what dismissing them meant.
    /// </summary>
    private async void OnClosed(object sender, WindowEventArgs args)
    {
        _themeService.ThemeChanged -= OnThemeChanged;

        try
        {
            if (!_completed) await AppServices.Settings.CompleteOnboardingAsync();
        }
        catch (Exception)
        {
            // The wizard has already gone, so a failed write only costs one more round of questions
        }
        finally
        {
            // Released last, so the shell cannot start against settings that are still being written
            _dismissed.TrySetResult();
        }
    }

    private static int IndexOfIdentity(int identity)
    {
        return identity switch
        {
            StudentIdentity.Student => 0,
            StudentIdentity.Teacher => 1,
            StudentIdentity.Parent => 2,
            _ => -1
        };
    }

    private static int IdentityForIndex(int index)
    {
        return index switch
        {
            0 => StudentIdentity.Student,
            1 => StudentIdentity.Teacher,
            2 => StudentIdentity.Parent,
            _ => StudentIdentity.None
        };
    }
}