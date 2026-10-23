using Known.Core.Domain;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The general settings: who the student is, their region and the subjects they sit. These are the
///     same three questions the onboarding wizard asks, editable later.
/// </summary>
public sealed partial class GeneralSettingsPage : Page
{
    private readonly StringLocalizer _strings = new();
    private readonly List<CheckBox> _subjectBoxes = [];
    private readonly HashSet<string> _subjects = new(StringComparer.Ordinal);

    private bool _locating;
    private bool _suppressEvents;

    /// <summary>Creates the page and fills it from the stored AppServices.Settings.</summary>
    public GeneralSettingsPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("general_settings");
        IdentityCaption.Text = _strings.Get("identity");
        RegionCaption.Text = _strings.Get("region");
        RegionBox.PlaceholderText = _strings.Get("district");
        SubjectsCaption.Text = _strings.Get("onboarding_subjects");
        LocateCaption.Text = _strings.Get("use_location");

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
            IdentityOptions.SelectedIndex = AppServices.Settings.Current.Identity switch
            {
                StudentIdentity.Student => 0,
                StudentIdentity.Teacher => 1,
                StudentIdentity.Parent => 2,
                _ => -1
            };

            RegionBox.Text = AppServices.Settings.Current.Region;

            var selected = AppServices.Settings.SelectedSubjects;

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

        await AppServices.Settings.SetIdentityAsync(identity);
    }

    private async void OnRegionLostFocus(object sender, RoutedEventArgs e)
    {
        if (_suppressEvents) return;

        await AppServices.Settings.SetRegionAsync(RegionBox.Text);
    }

    /// <summary>
    ///     Fills the region box from the device location, the same lookup the onboarding wizard
    ///     offers. A failure is reported under the button and leaves the field untouched, because this
    ///     page exists to edit what the wizard stored and must not clear it by accident.
    /// </summary>
    private async void OnLocate(object sender, RoutedEventArgs e)
    {
        if (_locating) return;

        _locating = true;

        LocateButton.IsEnabled = false;
        LocateProgress.IsActive = true;
        LocateProgress.Visibility = Visibility.Visible;
        LocateMessage.Visibility = Visibility.Collapsed;

        try
        {
            var result = await AppServices.Location.DetectCityAsync();

            if (result.HasCity)
            {
                RegionBox.Text = result.City!;

                // The page saves on lost focus, so a lookup that fills the box has to be committed
                // here or the value would be lost the moment the student clicked something else
                await AppServices.Settings.SetRegionAsync(result.City);

                return;
            }

            LocateMessage.Text = _strings.Get(result.Message!);
            LocateMessage.Visibility = Visibility.Visible;
        }
        catch (Exception)
        {
            // See the wizard: a declined prompt, a machine with location services off and an offline
            // machine all end up here, and none of them justify taking the settings page down
            LocateMessage.Text = _strings.Get("location_no_data");
            LocateMessage.Visibility = Visibility.Visible;
        }
        finally
        {
            _locating = false;
            LocateButton.IsEnabled = true;
            LocateProgress.IsActive = false;
            LocateProgress.Visibility = Visibility.Collapsed;
        }
    }

    private async void OnSubjectToggled(object sender, RoutedEventArgs e)
    {
        if (_suppressEvents || sender is not CheckBox box || box.Tag is not string key) return;

        if (box.IsChecked == true)
            _subjects.Add(key);
        else
            _subjects.Remove(key);

        await AppServices.Settings.SetSelectedSubjectsAsync(_subjects);
    }
}

