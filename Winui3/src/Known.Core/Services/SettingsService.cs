using Known.Core.Data;
using Known.Core.Domain;

namespace Known.Core.Services;

/// <summary>
///     Fronts the single settings row with an in-memory copy, so the shell can read the current theme
///     and language synchronously while every mutation still goes through the database.
/// </summary>
/// <remarks>
///     Reading the row on each access would force the UI thread to block on the database, which is
///     exactly the deadlock the caching copy exists to avoid. The cache is refreshed by
///     <see cref="LoadAsync" /> and by every mutation made through this service.
/// </remarks>
public sealed class SettingsService(ISettingsRepository repository)
{
    private readonly SemaphoreSlim _writeGate = new(1, 1);

    /// <summary>The settings currently in effect. Never null, defaults before the first load.</summary>
    public Settings Current { get; private set; } = new();

    /// <summary>The subjects the student picked during onboarding, as a set.</summary>
    public IReadOnlySet<string> SelectedSubjects => SubjectCatalogue.ToSubjectKeySet(Current.SelectedSubjects);

    /// <summary>Raised on whichever thread completed the mutation.</summary>
    public event EventHandler<Settings>? Changed;

    /// <summary>Loads, or creates on first run, the stored settings into <see cref="Current" />.</summary>
    public async Task LoadAsync(CancellationToken cancellationToken = default)
    {
        Current = await repository.InitialiseAsync(cancellationToken).ConfigureAwait(false);
        Changed?.Invoke(this, Current);
    }

    /// <summary>Chooses between the light and the dark variant of the system theme.</summary>
    public Task SetAppThemeAsync(int appThemeValue, CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.AppThemeValue = appThemeValue, cancellationToken);
    }

    /// <summary>Turns the high contrast request on or off.</summary>
    public Task SetHighContrastAsync(bool enabled, CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.IsHighContrastMode = enabled, cancellationToken);
    }

    /// <summary>Picks the UI language, passing an empty tag to follow the system.</summary>
    public Task SetLanguageAsync(string? languageTag, CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.SelectedLanguage = languageTag ?? "", cancellationToken);
    }

    /// <summary>Records who the student is, for the messages shown across the app.</summary>
    public Task SetIdentityAsync(int identity, CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.Identity = identity, cancellationToken);
    }

    /// <summary>Stores the free text region the student typed during onboarding.</summary>
    public Task SetRegionAsync(string? region, CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.Region = region ?? "", cancellationToken);
    }

    /// <summary>Stores the subjects the student actually sits.</summary>
    public Task SetSelectedSubjectsAsync(
        IEnumerable<string> subjects,
        CancellationToken cancellationToken = default)
    {
        return UpdateAsync(
            settings => settings.SelectedSubjects = SubjectCatalogue.ToSubjectString(subjects),
            cancellationToken);
    }

    /// <summary>Chooses which measure the dashboard trend chart plots.</summary>
    public Task SetTrendMetricAsync(
        TrendMetric metric,
        CancellationToken cancellationToken = default) =>
        UpdateAsync(settings => settings.TrendMetric = metric, cancellationToken);

    /// <summary>Chooses which measure the dashboard subject chart plots.</summary>
    public Task SetSubjectMetricAsync(
        TrendMetric metric,
        CancellationToken cancellationToken = default) =>
        UpdateAsync(settings => settings.SubjectMetric = metric, cancellationToken);

    /// <summary>Chooses the density the history list renders at.</summary>
    public Task SetHistoryViewModeAsync(
        HistoryViewMode mode,
        CancellationToken cancellationToken = default) =>
        UpdateAsync(settings => settings.HistoryViewMode = mode, cancellationToken);

    /// <summary>Marks onboarding as finished, which is what lets the shell open the main window.</summary>
    public Task CompleteOnboardingAsync(CancellationToken cancellationToken = default)
    {
        return UpdateAsync(settings => settings.IsFirstLogin = false, cancellationToken);
    }

    /// <summary>Re-reads the row from the database without going through a mutation.</summary>
    public async Task ReloadAsync(CancellationToken cancellationToken = default)
    {
        Current = await repository.InitialiseAsync(cancellationToken).ConfigureAwait(false);
        Changed?.Invoke(this, Current);
    }

    private async Task UpdateAsync(Action<Settings> mutate, CancellationToken cancellationToken)
    {
        ArgumentNullException.ThrowIfNull(mutate);

        // Writes are serialised so two settings pages touching different fields cannot lose one
        // another's change by saving a stale copy
        await _writeGate.WaitAsync(cancellationToken).ConfigureAwait(false);

        try
        {
            var draft = await repository.GetAsync(cancellationToken).ConfigureAwait(false) ?? new Settings();

            mutate(draft);

            await repository.SaveAsync(draft, cancellationToken).ConfigureAwait(false);

            Current = draft;
            Changed?.Invoke(this, Current);
        }
        finally
        {
            _writeGate.Release();
        }
    }
}