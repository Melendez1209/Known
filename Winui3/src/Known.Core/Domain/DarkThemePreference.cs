namespace Known.Core.Domain;

/// <summary>The three stored values of <see cref="DarkThemePreference.DarkThemeValue" />.</summary>
public static class DarkThemeMode
{
    /// <summary>Defer to the Windows app mode set by the user in Settings.</summary>
    public const int FollowSystem = 1;

    /// <summary>Always use the dark variant.</summary>
    public const int On = 2;

    /// <summary>Always use the light variant.</summary>
    public const int Off = 3;
}

/// <summary>
///     How the app decides between the light and the dark variant of the system theme, plus the
///     high contrast switch applied on top of it.
/// </summary>
public sealed record DarkThemePreference(
    int DarkThemeValue = DarkThemeMode.FollowSystem,
    bool IsHighContrastModeEnabled = false)
{
    /// <summary>
    ///     Resolves the stored preference against the current system setting. Any value other than
    ///     the three constants is treated as <see cref="DarkThemeMode.FollowSystem" />, so a corrupted
    ///     row still produces a usable theme rather than an exception.
    /// </summary>
    public bool IsDarkTheme(bool isSystemInDarkTheme)
    {
        return DarkThemeValue switch
        {
            DarkThemeMode.On => true,
            DarkThemeMode.Off => false,
            _ => isSystemInDarkTheme
        };
    }
}