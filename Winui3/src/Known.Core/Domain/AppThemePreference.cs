namespace Known.Core.Domain;

/// <summary>The three stored values of <see cref="AppThemePreference.AppThemeValue" />.</summary>
public static class AppThemeMode
{
    /// <summary>Defer to the Windows app mode set by the user in Settings.</summary>
    public const int FollowSystem = 1;

    /// <summary>Always use the dark variant.</summary>
    public const int Dark = 2;

    /// <summary>Always use the light variant.</summary>
    public const int Light = 3;
}

/// <summary>
///     How the app decides between the light and the dark variant of the system theme, plus the
///     high contrast switch applied on top of it.
/// </summary>
public sealed record AppThemePreference(
    int AppThemeValue = AppThemeMode.FollowSystem,
    bool IsHighContrastModeEnabled = false)
{
    /// <summary>
    ///     Resolves the stored preference against the current system setting. Any value other than
    ///     the three constants is treated as <see cref="AppThemeMode.FollowSystem" />, so a corrupted
    ///     row still produces a usable theme rather than an exception.
    /// </summary>
    public bool IsAppTheme(bool isSystemInDarkTheme)
    {
        return AppThemeValue switch
        {
            AppThemeMode.Dark => true,
            AppThemeMode.Light => false,
            _ => isSystemInDarkTheme
        };
    }
}
