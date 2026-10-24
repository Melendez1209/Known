using Windows.UI;
using Windows.UI.ViewManagement;
using Known.Core.Domain;
using Known.Core.Services;

namespace Known.Services;

/// <summary>
///     Turns the stored preference into the concrete values the window needs: which
///     <see cref="Microsoft.UI.Xaml.ElementTheme" /> to request and whether the high contrast variants
///     should be layered on top. The service never touches XAML itself.
/// </summary>
public sealed class ThemeService(SettingsService settingsService)
{
    /// <summary>Whether Windows is currently offering the dark variant of the system theme.</summary>
    private bool IsSystemInDarkTheme { get; set; }

    /// <summary>Whether Windows itself is forcing a high contrast theme.</summary>
    private bool IsSystemHighContrast { get; set; }

    /// <summary>The preference currently in effect.</summary>
    private DarkThemePreference Preference { get; set; } = new();

    /// <summary>Whether the window should render dark.</summary>
    public bool ShouldUseDarkTheme => Preference.IsDarkTheme(IsSystemInDarkTheme);

    /// <summary>Whether the high contrast variants should be requested right now.</summary>
    public bool HighContrastActive => Preference.IsHighContrastModeEnabled || IsSystemHighContrast;

    /// <summary>Raised whenever the effective values change, on the UI thread.</summary>
    public event EventHandler? ThemeChanged;

    /// <summary>
    ///     Re-reads the Windows settings, so "follow system" keeps tracking the user flipping the
    ///     system app mode, then notifies listeners with the resolved values.
    /// </summary>
    public void Refresh()
    {
        IsSystemInDarkTheme = QueryIsSystemInDarkTheme();
        IsSystemHighContrast = QueryIsSystemHighContrast();
        Preference = new DarkThemePreference(
            settingsService.Current.DarkThemeValue,
            settingsService.Current.IsHighContrastMode);

        ThemeChanged?.Invoke(this, EventArgs.Empty);
    }

    /// <summary>Whether Windows is currently offering the dark variant.</summary>
    private static bool QueryIsSystemInDarkTheme()
    {
        var settings = new UISettings();
        var background = settings.GetColorValue(UIColorType.Background);

        return IsDark(background);
    }

    /// <summary>Whether Windows is forcing a high contrast theme.</summary>
    private static bool QueryIsSystemHighContrast()
    {
        try
        {
            return new AccessibilitySettings().HighContrast;
        }
        catch (Exception)
        {
            // The accessibility settings can be unavailable while a session is switching users, and
            // an unknown state must never stop the window from rendering
            return false;
        }
    }

    private static bool IsDark(Color colour)
    {
        // Rec. 709 luma, the same rule the shell uses to decide which of a pair of theme brushes wins
        var luma = (0.2126f * colour.R + 0.7152f * colour.G + 0.0722f * colour.B) / 255f;

        return luma < 0.5f;
    }
}