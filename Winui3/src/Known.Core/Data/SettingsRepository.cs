using Microsoft.EntityFrameworkCore;

namespace Known.Core.Data;

/// <summary>Access to the single settings row that drives theme, language and onboarding state.</summary>
public interface ISettingsRepository
{
    /// <summary>Raised after the stored row changes, so the shell can re-apply the theme.</summary>
    event EventHandler? Changed;

    /// <summary>
    ///     Creates the row with defaults when it is missing and returns whatever is stored afterwards,
    ///     so callers always receive a usable instance.
    /// </summary>
    Task<Settings> InitialiseAsync(CancellationToken cancellationToken = default);

    /// <summary>The stored row, or <see langword="null" /> before <see cref="InitialiseAsync" /> has run.</summary>
    Task<Settings?> GetAsync(CancellationToken cancellationToken = default);

    /// <summary>Writes every field of <paramref name="settings" /> back, inserting the row if needed.</summary>
    Task SaveAsync(Settings settings, CancellationToken cancellationToken = default);
}

/// <inheritdoc />
public sealed class SettingsRepository(KnownDbContext context) : ISettingsRepository
{
    /// <summary>The primary key of the one and only settings row.</summary>
    public const int SingletonId = 1;

    /// <inheritdoc />
    public event EventHandler? Changed;

    /// <inheritdoc />
    public async Task<Settings> InitialiseAsync(CancellationToken cancellationToken = default)
    {
        var stored = await GetAsync(cancellationToken).ConfigureAwait(false);

        if (stored is not null) return stored;

        var defaults = new Settings { Id = SingletonId };

        context.Settings.Add(defaults);
        await context.SaveChangesAsync(cancellationToken).ConfigureAwait(false);

        RaiseChanged();
        return defaults;
    }

    /// <inheritdoc />
    public Task<Settings?> GetAsync(CancellationToken cancellationToken = default)
    {
        return context.Settings
            .AsNoTracking()
            .FirstOrDefaultAsync(settings => settings.Id == SingletonId, cancellationToken);
    }

    /// <inheritdoc />
    public async Task SaveAsync(Settings settings, CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(settings);

        var stored = await context.Settings
            .FirstOrDefaultAsync(candidate => candidate.Id == settings.Id, cancellationToken)
            .ConfigureAwait(false);

        if (stored is null)
        {
            settings.Id = SingletonId;
            context.Settings.Add(settings);
        }
        else
        {
            stored.AppThemeValue = settings.AppThemeValue;
            stored.IsHighContrastMode = settings.IsHighContrastMode;
            stored.SelectedLanguage = settings.SelectedLanguage;
            stored.IsFirstLogin = settings.IsFirstLogin;
            stored.Identity = settings.Identity;
            stored.Region = settings.Region;
            stored.SelectedSubjects = settings.SelectedSubjects;
            stored.TrendMetric = settings.TrendMetric;
            stored.SubjectMetric = settings.SubjectMetric;
        }

        await context.SaveChangesAsync(cancellationToken).ConfigureAwait(false);

        RaiseChanged();
    }

    private void RaiseChanged()
    {
        Changed?.Invoke(this, EventArgs.Empty);
    }
}