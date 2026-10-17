using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Shapes;

namespace Known;

/// <summary>
///     The three step onboarding wizard: who the student is, where they are, and which subjects they
///     sit. Finishing writes the answers and clears the first login flag, so the shell opens the
///     dashboard next time.
/// </summary>
/// <remarks>
///     This used to be a <c>KnownPage</c> pushed onto the shell's shared frame. It is a window of its
///     own now, which keeps it out of the tab back stacks entirely, and it no longer needs
///     <see cref="ShellNavigator" />: closing it is enough to get back to the shell.
/// </remarks>
public sealed partial class GuideWindow : Window
{
    private const int StepCount = 3;

    private readonly HashSet<string> _selectedSubjects = new(StringComparer.Ordinal);
    private readonly StringLocalizer _strings = new();
    private readonly ThemeService _themeService;

    private bool _completed;
    private bool _finishing;
    private int _identity;
    private string _region;
    private int _step;

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
        ShowStep(0);
        ApplyTheme();

        Closed += OnClosed;
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
        };
    }

    private void BuildRegionStep()
    {
        RegionBox.Text = _region;
        RegionBox.TextChanged += (_, _) => _region = RegionBox.Text ?? string.Empty;
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

        root.RequestedTheme = _themeService.ShouldUseDarkTheme
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

        if (_completed) return;

        try
        {
            await AppServices.Settings.CompleteOnboardingAsync();
        }
        catch (Exception)
        {
            // The wizard has already gone, so a failed write only costs one more round of questions
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