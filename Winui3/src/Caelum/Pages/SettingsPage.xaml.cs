using System.Diagnostics;
using System.Globalization;
using Caelum.Core.Data;
using Caelum.Core.Domain;
using Caelum.Core.Localization;
using Caelum.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;
using Windows.ApplicationModel;
using Windows.System;

namespace Caelum.Pages;

/// <summary>
///     The single settings page. All desktop-relevant settings are grouped into cards on this
///     page instead of being spread across child pages: the student profile, identity and region,
///     the selected subjects, the theme and language, the Microsoft account, and an About card
///     carrying the app version and the project links.
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

        DarkCaption.Text = _strings.Get("app_theme");
        LanguageCaption.Text = _strings.Get("language");

        AboutCaption.Text = _strings.Get("about");
        VersionText.Text = _strings.Get("app_name") + " " + ReadAppVersion();
        SloganText.Text = _strings.Get("slogan");
        ReadmeLink.Header = _strings.Get("readme");
        ReleaseLink.Header = _strings.Get("release");
        IssueLink.Header = _strings.Get("github_issue");
        PrivacyLink.Header = _strings.Get("privacy_policy");

        AuthComingSoon.Text = _strings.Get("account_coming_soon");

        // The hint is only meaningful when the chosen language actually needs a restart
        LanguageHint.Visibility = Visibility.Collapsed;

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
        // No Entra registration means the account card has nothing to offer yet, so the button is
        // replaced by a caption rather than left disabled and unexplained.
        var comingSoon = !state.IsSignedIn && !AppServices.Auth.IsConfigured;

        AuthButton.Visibility = comingSoon ? Visibility.Collapsed : Visibility.Visible;
        AuthComingSoon.Visibility = comingSoon ? Visibility.Visible : Visibility.Collapsed;

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
            await ShowMessageAsync(_strings.Get("account_coming_soon"));
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

            ThemeOptions.SelectedIndex = AppServices.Settings.Current.AppThemeValue switch
            {
                AppThemeMode.Dark => 2,
                AppThemeMode.Light => 1,
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
            _strings.Get("use_system_settings"),
            _strings.Get("light"),
            _strings.Get("dark")
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

        var value = ThemeOptions.SelectedIndex switch
        {
            0 => AppThemeMode.FollowSystem,
            1 => AppThemeMode.Light,
            2 => AppThemeMode.Dark,
            _ => AppThemeMode.FollowSystem
        };

        await AppServices.Settings.SetAppThemeAsync(value);
    }

    private async void OnLanguageChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressEvents || LanguageBox.SelectedItem is not LanguageOption option) return;

        await AppServices.Settings.SetLanguageAsync(option.Tag);

        // Switching to the system language (empty tag) or to a language that already matches the
        // system needs no restart; only a genuinely different language requires one.
        var selectedCulture = StringLocalizer.ResolveCulture(option.Tag);
        var systemCulture = CultureInfo.InstalledUICulture;
        var needsRestart = !string.Equals(
            selectedCulture.Name, systemCulture.Name, StringComparison.OrdinalIgnoreCase);

        StringLocalizer.UseLanguage(option.Tag);

        // Only a genuinely different language needs a restart, so the hint and the button share
        // the same condition instead of the hint being shown after every language change.
        LanguageHint.Text = _strings.Get("language_restart_hint");
        LanguageHint.Visibility = needsRestart ? Visibility.Visible : Visibility.Collapsed;
        RestartButton.Visibility = needsRestart ? Visibility.Visible : Visibility.Collapsed;
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
    // About
    // ------------------------------------------------------------------

    private void OnReadmeClick(object sender, RoutedEventArgs e) =>
        OpenUrlAsync("https://github.com/Melendez1209/Caelum");

    private void OnReleaseClick(object sender, RoutedEventArgs e) =>
        OpenUrlAsync("https://github.com/Melendez1209/Caelum/releases");

    private void OnIssueClick(object sender, RoutedEventArgs e) =>
        OpenUrlAsync("https://github.com/Melendez1209/Caelum/issues");

    private void OnPrivacyClick(object sender, RoutedEventArgs e) =>
        OpenUrlAsync("https://melendez1209.github.io/Caelum/privacy.html");

    /// <summary>
    ///     Reads the version from the package identity, which is what the release carries. Runs
    ///     without a package identity have none, so the assembly version stands in for them.
    /// </summary>
    private static string ReadAppVersion()
    {
        try
        {
            var version = Package.Current.Id.Version;
            return $"{version.Major}.{version.Minor}.{version.Build}.{version.Revision}";
        }
        catch (InvalidOperationException)
        {
            var assembly = typeof(SettingsPage).Assembly.GetName().Version;
            return assembly is null ? string.Empty : assembly.ToString();
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /// <summary>
    ///     Opens an external link in the browser. A link is a convenience, so a failure to
    ///     launch it must never take the settings page down.
    /// </summary>
    private static async void OpenUrlAsync(string url)
    {
        try
        {
            await Launcher.LaunchUriAsync(new Uri(url));
        }
        catch (Exception)
        {
            // Nothing left to fall back to
        }
    }

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