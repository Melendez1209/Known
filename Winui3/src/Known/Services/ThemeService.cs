using Windows.UI;
using Windows.UI.ViewManagement;
using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Services;
using Microsoft.UI.Xaml;

namespace Known.Services;

/// <summary>
///     Turns the stored preference into the concrete values the window needs: which
///     <see cref="Microsoft.UI.Xaml.ElementTheme" /> to request and whether the high contrast variants
///     should be layered on top. The service never touches XAML itself.
/// </summary>
public sealed class ThemeService
{
    private readonly SettingsService _settingsService;

    /// <summary>Whether Windows is currently offering the dark variant of the system theme.</summary>
    private bool IsSystemInDarkTheme { get; set; }

    /// <summary>The preference currently in effect.</summary>
    private AppThemePreference Preference { get; set; } = new();

    /// <summary>Whether the window should render dark.</summary>
    public bool ShouldUseAppTheme => Preference.IsAppTheme(IsSystemInDarkTheme);

    /// <summary>
    ///     The concrete theme the window is rendering with. Code that needs to copy a theme brush
    ///     into another format, such as the Skia based charts, uses this to pick the right entry.
    /// </summary>
    public ElementTheme CurrentTheme => ShouldUseAppTheme ? ElementTheme.Dark : ElementTheme.Light;

    /// <summary>Raised whenever the effective values change, on the UI thread.</summary>
    public event EventHandler? ThemeChanged;

    /// <summary>Creates the service and listens for stored preference changes so a theme switch
    ///     takes effect immediately instead of waiting for the window to be reactivated.</summary>
    public ThemeService(SettingsService settingsService)
    {
        _settingsService = settingsService;
        settingsService.Changed += OnSettingsChanged;
    }

    /// <summary>
    ///     Re-reads the Windows settings, so "follow system" keeps tracking the user flipping the
    ///     system app mode, then notifies listeners with the resolved values.
    /// </summary>
    public void Refresh()
    {
        IsSystemInDarkTheme = QueryIsSystemInDarkTheme();
        Preference = new AppThemePreference(
            _settingsService.Current.AppThemeValue,
            _settingsService.Current.IsHighContrastMode);

        ThemeChanged?.Invoke(this, EventArgs.Empty);
    }

    private void OnSettingsChanged(object? sender, Settings settings)
    {
        Refresh();
    }

    /// <summary>Whether Windows is currently offering the dark variant.</summary>
    private static bool QueryIsSystemInDarkTheme()
    {
        var settings = new UISettings();
        var background = settings.GetColorValue(UIColorType.Background);

        return IsDark(background);
    }

    private static bool IsDark(Color colour)
    {
        // Rec. 709 luma, the same rule the shell uses to decide which of a pair of theme brushes wins
        var luma = (0.2126f * colour.R + 0.7152f * colour.G + 0.0722f * colour.B) / 255f;

        return luma < 0.5f;
    }
}
