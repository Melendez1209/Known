using Known.Core.Domain;

namespace Known.Core.Data;

/// <summary>
///     The single settings row. Only one record is ever kept, identified by <see cref="Id" />.
/// </summary>
public sealed class Settings
{
    /// <summary>Always <c>1</c>; the table holds exactly one row.</summary>
    public int Id { get; set; } = 1;

    /// <summary>One of the <see cref="AppThemeMode" /> constants.</summary>
    public int AppThemeValue { get; set; } = AppThemeMode.FollowSystem;

    /// <summary>Whether the high contrast variants of the system theme are requested.</summary>
    public bool IsHighContrastMode { get; set; }

    /// <summary>BCP 47 tag of the chosen UI language, empty to follow the system.</summary>
    public string SelectedLanguage { get; set; } = string.Empty;

    /// <summary>Whether the onboarding wizard still has to run before the main window opens.</summary>
    public bool IsFirstLogin { get; set; } = true;

    /// <summary>One of the <see cref="StudentIdentity" /> constants.</summary>
    public int Identity { get; set; } = StudentIdentity.None;

    /// <summary>Free text region the student typed during onboarding.</summary>
    public string Region { get; set; } = string.Empty;

    /// <summary>Comma separated subject keys chosen during onboarding.</summary>
    public string SelectedSubjects { get; set; } = string.Empty;

    /// <summary>
    /// Which measure the dashboard trend chart plots. A first run has never seen the charts, so it
    /// opens on raw marks rather than a percentage the student has no context for yet.
    /// </summary>
    public TrendMetric TrendMetric { get; set; } = TrendMetric.Marks;
}
