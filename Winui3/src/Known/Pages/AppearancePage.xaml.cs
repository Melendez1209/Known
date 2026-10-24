using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

/// <summary>
///     Theme and language. The Android build also offered a palette picker with nine colour schemes
///     driven by its own HCT engine; the desktop build uses the Windows theme instead, so only the
///     light and dark choice and the language are left.
/// </summary>
public sealed partial class AppearancePage : Page
{
    private readonly StringLocalizer _strings = new();

    private bool _suppressEvents;

    /// <summary>Creates the page and fills it from the stored AppServices.Settings.</summary>
    public AppearancePage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("look_and_feel");
        DarkCaption.Text = _strings.Get("dark_theme");
        HighContrastToggle.Header = _strings.Get("high_contrast");
        LanguageCaption.Text = _strings.Get("language");
        LanguageHint.Text = _strings.Get("language_restart_hint");

        AppServices.Settings.Changed += OnSettingsChanged;

        BuildThemeOptions();
        BuildLanguageOptions();
        ApplyStoredSettings();
    }

    /// <inheritdoc />
    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
        AppServices.Settings.Changed -= OnSettingsChanged;

        base.OnNavigatedFrom(e);
    }

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

    private void ApplyStoredSettings()
    {
        _suppressEvents = true;

        try
        {
            ThemeOptions.SelectedIndex = AppServices.Settings.Current.DarkThemeValue switch
            {
                DarkThemeMode.On => 1,
                DarkThemeMode.Off => 2,
                _ => 0
            };

            HighContrastToggle.IsOn = AppServices.Settings.Current.IsHighContrastMode;

            var selected = StringLocalizer.AvailableLanguages
                .FirstOrDefault(option => option.Tag == AppServices.Settings.Current.SelectedLanguage);

            LanguageBox.SelectedItem = selected ?? StringLocalizer.AvailableLanguages[0];
        }
        finally
        {
            _suppressEvents = false;
        }
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

    private async void OnHighContrastToggled(object sender, RoutedEventArgs e)
    {
        if (_suppressEvents) return;

        await AppServices.Settings.SetHighContrastAsync(HighContrastToggle.IsOn);
    }

    private async void OnLanguageChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_suppressEvents || LanguageBox.SelectedItem is not LanguageOption option) return;

        // The switch takes effect on the next launch, because every already constructed page holds
        // the strings it resolved when it was loaded
        await AppServices.Settings.SetLanguageAsync(option.Tag);
        StringLocalizer.UseLanguage(option.Tag);
    }
}

