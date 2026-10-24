using System.Diagnostics;
using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

/// <summary>
///     The single settings page. All desktop-relevant settings are grouped into cards on this
///     page instead of being spread across child pages: the student profile, identity and region,
///     the selected subjects, the theme and language, and the Microsoft account.
/// </summary>
public sealed partial class SettingsPage
{
    private readonly StringLocalizer _strings = new();
    private readonly List<CheckBox> _subjectBoxes = [];
    private readonly HashSet<string> _subjects = new(StringComparer.Ordinal);

    private bool _locating;
    private bool _suppressEvents;

    /// <summary>Creates the page and fills every card from the stored AppServices.Settings.</summary>
    public SettingsPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("settings");

        AccountCaption.Text = _strings.Get("account_section");
        GeneralCaption.Text = _strings.Get("general_section");
        AppearanceCaption.Text = _strings.Get("appearance_section");

        IdentityCaption.Text = _strings.Get("identity");
        RegionCaption.Text = _strings.Get("region");
        RegionBox.PlaceholderText = _strings.Get("district");
        SubjectsCaption.Text = _strings.Get("subjects");
        LocateCaption.Text = _strings.Get("location_use");

        DarkCaption.Text = _strings.Get("dark_theme");
        LanguageCaption.Text = _strings.Get("language");

        IdentityOptions.ItemsSource = new[]
        {
            _strings.Get("student"),
            _strings.Get("teacher"),
            _strings.Get("parent")
        };

        BuildSubjectBoxes();
        BuildThemeOptions();
        BuildLanguageOptions();

        RestartButton.Content = _strings.Get("restart_app");
        RestartButton.Visibility = Visibility.Collapsed;

        ApplyStoredSettings();
        ApplyAccountState(AppServices.Auth.State);

        AppServices.Settings.Changed += OnSettingsChanged;
        AppServices.Auth.StateChanged += OnAuthStateChanged;
    }

    /// <inheritdoc />
    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
        AppServices.Settings.Changed -= OnSettingsChanged;
        AppServices.Auth.StateChanged -= OnAuthStateChanged;

        base.OnNavigatedFrom(e);
    }

    // ------------------------------------------------------------------
    // Account
    // ------------------------------------------------------------------

    private void OnAuthStateChanged(object? sender, AccountState state)
    {
        ApplyAccountState(state);
    }

    private void ApplyAccountState(AccountState state)
    {
        AccountName.Text = state.IsSignedIn
            ? state.DisplayName ?? _strings.Get("sign_to")
            : _strings.Get("account_not_signed_in");

        AccountEmail.Text = state.Email ?? string.Empty;

        AuthButton.Content = state.IsSignedIn
            ? _strings.Get("sign_out")
            : _strings.Get("microsoft_sign_in");

        AuthButton.IsEnabled = state.IsSignedIn || AppServices.Auth.IsConfigured;

        if (!state.IsSignedIn)
        {
            Avatar.ProfilePicture = null;
            Avatar.DisplayName = string.Empty;
            return;
        }

        Avatar.DisplayName = state.DisplayName ?? string.Empty;
    }

    private async void OnAuthTapped(object sender, RoutedEventArgs e)
    {
        if (!AppServices.Auth.IsConfigured)
        {
            await ShowMessageAsync(_strings.Get("attention"));
            return;
        }

        try
        {
            if (AppServices.Auth.State.IsSignedIn)
                AppServices.Auth.SignOut();
            else
                await AppServices.Auth.SignInAsync();
        }
        catch (Exception)
        {
            await ShowMessageAsync(_strings.Get("attention"));
        }
    }

    // ------------------------------------------------------------------
    // General
    // ------------------------------------------------------------------

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

            ThemeOptions.SelectedIndex = AppServices.Settings.Current.DarkThemeValue switch
            {
                DarkThemeMode.On => 1,
                DarkThemeMode.Off => 2,
                _ => 0
            };

            var selectedLanguage = StringLocalizer.AvailableLanguages
                .FirstOrDefault(option => option.Tag == AppServices.Settings.Current.SelectedLanguage);

            LanguageBox.SelectedItem = selectedLanguage ?? StringLocalizer.AvailableLanguages[0];
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
                await AppServices.Settings.SetRegionAsync(result.City);
                return;
            }

            LocateMessage.Text = _strings.Get(result.Message!);
            LocateMessage.Visibility = Visibility.Visible;
        }
        catch (Exception)
        {
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

    // ------------------------------------------------------------------
    // Appearance
    // ------------------------------------------------------------------

    private void BuildThemeOptions()
    {
        ThemeOptions.ItemsSource = new[]
        {
            _strings.Get("follow_system"),
            _strings.Get("on"),
            _strings.Get("off")
        };
    }

    private void BuildLanguageOptions()
    {
        LanguageBox.ItemsSource = StringLocalizer.AvailableLanguages;
        LanguageBox.DisplayMemberPath = nameof(LanguageOption.EnglishName);
    }

    private void OnSettingsChanged(object? sender, Settings settings)
    {
        ApplyStoredSettings();
    }

    private async void OnThemeChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressEvents) return;

        var value = (ThemeOptions.SelectedIndex + 1) switch
        {
            1 => DarkThemeMode.On,
            2 => DarkThemeMode.Off,
            _ => DarkThemeMode.FollowSystem
        };

        await AppServices.Settings.SetDarkThemeAsync(value);
    }

    private async void OnLanguageChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressEvents || LanguageBox.SelectedItem is not LanguageOption option) return;

        await AppServices.Settings.SetLanguageAsync(option.Tag);
        StringLocalizer.UseLanguage(option.Tag);

        LanguageHint.Text = _strings.Get("language_restart_hint");
        RestartButton.Visibility = Visibility.Visible;
    }

    private void OnRestartClicked(object sender, RoutedEventArgs e)
    {
        try
        {
            var executable = Environment.ProcessPath;
            if (executable is not null)
                Process.Start(new ProcessStartInfo
                {
                    FileName = executable,
                    UseShellExecute = true
                });
        }
        catch
        {
            // Best effort; if the restart cannot be spawned the user can restart manually
        }

        Application.Current.Exit();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private async Task ShowMessageAsync(string message)
    {
        var dialog = new ContentDialog
        {
            Content = message,
            CloseButtonText = _strings.Get("done"),
            XamlRoot = XamlRoot
        };

        await dialog.ShowAsync();
    }
}