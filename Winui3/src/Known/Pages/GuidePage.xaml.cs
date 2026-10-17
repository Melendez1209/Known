using Known.Core.Domain;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Shapes;

namespace Known.Pages;

/// <summary>
///     The three step onboarding wizard: who the student is, where they are, and which subjects they
///     sit. Finishing writes the answers and clears the first login flag so the shell opens the
///     dashboard next time.
/// </summary>
public sealed partial class GuidePage : KnownPage
{
    private const int StepCount = 3;
    private readonly HashSet<string> _selectedSubjects = new(StringComparer.Ordinal);

    private readonly StringLocalizer _strings = new();
    private readonly List<CheckBox> _subjectBoxes = [];
    private bool _finishing;
    private int _identity;
    private string _region;

    /// <summary>Creates the wizard, builds its three steps and shows the first one.</summary>
    public GuidePage()
    {
        InitializeComponent();

        _identity = Settings.Current.Identity;
        _region = Settings.Current.Region;

        foreach (var key in Settings.SelectedSubjects) _selectedSubjects.Add(key);

        ApplyCaptions();
        BuildSteps();
        BuildDots();
        UpdateFooter();
    }

    private void ApplyCaptions()
    {
        WelcomeText.Text = _strings.Get("onboarding_welcome");
        DescriptionText.Text = _strings.Get("onboarding_description");
        SkipButton.Content = _strings.Get("skip");
    }

    /// <summary>
    ///     Builds one page per onboarding step. The steps are built eagerly and handed to the
    ///     FlipView as a source, so swiping back and forth keeps whatever the student already typed.
    /// </summary>
    private void BuildSteps()
    {
        Pager.ItemsSource = new List<UIElement>
        {
            BuildIdentityStep(),
            BuildRegionStep(),
            BuildSubjectStep()
        };
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
        UpdateDots();
    }

    private UIElement BuildIdentityStep()
    {
        var panel = new StackPanel { Spacing = 8, Margin = new Thickness(0, 12, 0, 0) };

        panel.Children.Add(Heading(_strings.Get("onboarding_identity")));

        var options = new RadioButtons
        {
            ItemsSource = new[]
            {
                _strings.Get("student"),
                _strings.Get("teacher"),
                _strings.Get("parent")
            },
            SelectedIndex = IndexOfIdentity(_identity)
        };

        // RadioButtons reports the selection by index, so the identity is derived from it
        options.SelectionChanged += (_, _) =>
        {
            if (options.SelectedIndex >= 0) _identity = IdentityForIndex(options.SelectedIndex);
        };

        panel.Children.Add(options);

        return panel;
    }

    private UIElement BuildRegionStep()
    {
        var panel = new StackPanel { Spacing = 8, Margin = new Thickness(0, 12, 0, 0) };

        panel.Children.Add(Heading(_strings.Get("onboarding_region")));

        // The Android build offered to fill this in from the device location. Windows exposes a
        // different geocoding stack behind a permission prompt, so v1 keeps it as plain text.
        var box = new TextBox
        {
            PlaceholderText = _strings.Get("district"),
            Text = _region,
            AcceptsReturn = false
        };

        box.TextChanged += (_, _) => _region = box.Text ?? string.Empty;

        panel.Children.Add(box);

        return panel;
    }

    private UIElement BuildSubjectStep()
    {
        var panel = new StackPanel { Spacing = 8, Margin = new Thickness(0, 12, 0, 0) };

        panel.Children.Add(Heading(_strings.Get("onboarding_subjects")));

        // A wrap grid rather than a vertical list, so a wide window shows several subjects per row
        // and a narrow one falls back to a single column without any extra layout code
        var wrap = new WrapGrid { Orientation = Orientation.Horizontal };

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

            _subjectBoxes.Add(box);
            wrap.Children.Add(box);
        }

        panel.Children.Add(wrap);

        return panel;
    }

    private TextBlock Heading(string text)
    {
        return new TextBlock
        {
            Text = text,
            Style = (Style)Application.Current.Resources["SubtitleTextBlockStyle"]
        };
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

    private void OnSubjectToggled(object sender, RoutedEventArgs e)
    {
        if (sender is not CheckBox box || box.Tag is not string key) return;

        if (box.IsChecked == true)
            _selectedSubjects.Add(key);
        else
            _selectedSubjects.Remove(key);
    }

    private void OnPageChanged(object sender, SelectionChangedEventArgs e)
    {
        UpdateFooter();
        UpdateDots();
    }

    private void UpdateDots()
    {
        if (Dots.ItemsSource is not IEnumerable<Ellipse> dots) return;

        var active = Math.Max(Pager.SelectedIndex, 0);
        var accent = (Brush)Application.Current.Resources["ChartAccentBrush"];
        var muted = (Brush)Application.Current.Resources["ChartGridLineBrush"];

        var index = 0;

        foreach (var dot in dots)
        {
            dot.Fill = index == active ? accent : muted;
            dot.Opacity = index == active ? 1d : 0.6d;
            index++;
        }
    }

    private void UpdateFooter()
    {
        var isLastStep = Math.Max(Pager.SelectedIndex, 0) >= StepCount - 1;

        // The wizard has a skip button, so it is hidden on the final step where finishing replaces it
        SkipButton.Visibility = isLastStep ? Visibility.Collapsed : Visibility.Visible;
        PrimaryButton.Content = isLastStep ? _strings.Get("get_started") : _strings.Get("next");
    }

    private async void OnPrimaryAction(object sender, RoutedEventArgs e)
    {
        if (Math.Max(Pager.SelectedIndex, 0) < StepCount - 1)
        {
            Pager.SelectedIndex += 1;
            return;
        }

        await FinishAsync();
    }

    private async void OnSkip(object sender, RoutedEventArgs e)
    {
        await FinishAsync();
    }

    private async Task FinishAsync()
    {
        if (_finishing) return;

        _finishing = true;

        try
        {
            await Settings.SetIdentityAsync(_identity);
            await Settings.SetRegionAsync(_region);
            await Settings.SetSelectedSubjectsAsync(_selectedSubjects);
            await Settings.CompleteOnboardingAsync();

            // The wizard lives on the shared stack, so popping it returns to the dashboard tab
            if (Shell.CanGoBack) Shell.GoBack();
        }
        finally
        {
            _finishing = false;
        }
    }
}