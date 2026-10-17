using Known.Core.Domain;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The general settings: who the student is, their region and the subjects they sit. These are the
///     same three questions the onboarding wizard asks, editable later.
/// </summary>
public sealed partial class GeneralSettingsPage : KnownPage
{
    private readonly StringLocalizer _strings = new();
    private readonly List<CheckBox> _subjectBoxes = [];
    private readonly HashSet<string> _subjects = new(StringComparer.Ordinal);

    private bool _suppressEvents;

    /// <summary>Creates the page and fills it from the stored settings.</summary>
    public GeneralSettingsPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("general_settings");
        IdentityCaption.Text = _strings.Get("identity");
        RegionCaption.Text = _strings.Get("region");
        RegionBox.PlaceholderText = _strings.Get("district");
        SubjectsCaption.Text = _strings.Get("onboarding_subjects");

        IdentityOptions.ItemsSource = new[]
        {
            _strings.Get("student"),
            _strings.Get("teacher"),
            _strings.Get("parent")
        };

        BuildSubjectBoxes();
        ApplyStoredSettings();
    }

    private void BuildSubjectBoxes()
    {
        foreach (var key in SubjectCatalogue.SubjectKeys)
        {
            var box = new CheckBox
            {
                Content = SubjectLabels.NameOf(_strings, key),
                Tag = key,
                MinWidth = 180,
                Margin = new Thickness(0, 0, 12, 8)
            };

            box.Checked += OnSubjectToggled;
            box.Unchecked += OnSubjectToggled;

            _subjectBoxes.Add(box);
            SubjectGrid.Items.Add(box);
        }
    }

    private void ApplyStoredSettings()
    {
        _suppressEvents = true;

        try
        {
            IdentityOptions.SelectedIndex = Settings.Current.Identity switch
            {
                StudentIdentity.Student => 0,
                StudentIdentity.Teacher => 1,
                StudentIdentity.Parent => 2,
                _ => -1
            };

            RegionBox.Text = Settings.Current.Region;

            var selected = Settings.SelectedSubjects;

            foreach (var box in _subjectBoxes)
            {
                if (box.Tag is not string key) continue;

                var isSelected = selected.Contains(key);

                if (isSelected)
                    _subjects.Add(key);
                else
                    _subjects.Remove(key);

                box.IsChecked = isSelected;
            }
        }
        finally
        {
            _suppressEvents = false;
        }
    }

    private async void OnIdentityChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressEvents || IdentityOptions.SelectedIndex < 0) return;

        var identity = IdentityOptions.SelectedIndex switch
        {
            0 => StudentIdentity.Student,
            1 => StudentIdentity.Teacher,
            2 => StudentIdentity.Parent,
            _ => StudentIdentity.None
        };

        await Settings.SetIdentityAsync(identity);
    }

    private async void OnRegionLostFocus(object sender, RoutedEventArgs e)
    {
        if (_suppressEvents) return;

        await Settings.SetRegionAsync(RegionBox.Text);
    }

    private async void OnSubjectToggled(object sender, RoutedEventArgs e)
    {
        if (_suppressEvents || sender is not CheckBox box || box.Tag is not string key) return;

        if (box.IsChecked == true)
            _subjects.Add(key);
        else
            _subjects.Remove(key);

        await Settings.SetSelectedSubjectsAsync(_subjects);
    }
}