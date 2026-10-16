using System.Collections.ObjectModel;
using Known.Core.Data;
using Known.Core.Domain;

namespace Known.Services;

/// <summary>
/// Holds the exams in memory as observable collections so the list, the charts and the dashboard
/// all react to one write. It stands in for the cold Kotlin flows the Android repositories exposed,
/// which the platform has no direct equivalent of.
/// </summary>
public sealed class ExamService
{
    private readonly SemaphoreSlim _refreshGate = new(1, 1);

    /// <summary>Creates the service over a repository and starts listening for writes.</summary>
    public ExamService(IExamRepository repository)
    {
        ArgumentNullException.ThrowIfNull(repository);

        Repository = repository;
        repository.Changed += OnRepositoryChanged;
    }

    /// <summary>The repository underneath, for the pages that need a one-off query.</summary>
    public IExamRepository Repository { get; }

    /// <summary>Every exam with its totals, newest first.</summary>
    public ObservableCollection<ExamWithTotal> Exams { get; } = [];

    /// <summary>The exams the student pinned to their favourites.</summary>
    public ObservableCollection<Exam> Favourites { get; } = [];

    /// <summary>
    /// Mean score per subject across every exam. The dashboard shows all exams at once, so unlike
    /// the detail screen nothing needs excluding.
    /// </summary>
    public ObservableCollection<SubjectStat> SubjectStats { get; } = [];

    /// <summary>Raised on the UI thread after <see cref="Exams"/> and <see cref="Favourites"/> reload.</summary>
    public event EventHandler? Reloaded;

    /// <summary>Mean percentage across every exam that carries a score, or zero when there is none.</summary>
    public float AveragePercentage =>
        Exams.Count == 0
            ? 0f
            : (float)Exams.Average(exam =>
                (double)ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark));

    /// <summary>The best rank the student reached, or zero when no exam carries a score.</summary>
    public int BestRank
    {
        get
        {
            var scored = Exams
                .Where(exam => exam.TotalFullMark > 0f)
                .Select(exam => ExamScoring.RankOf(exam.Exam.Id, Exams.ToList()))
                .DefaultIfEmpty(0);

            var ranks = scored.ToList();

            return ranks.Count == 0 ? 0 : ranks.Min();
        }
    }

    /// <summary>Reloads both collections from the database.</summary>
    public async Task LoadAsync(CancellationToken cancellationToken = default)
    {
        await _refreshGate.WaitAsync(cancellationToken).ConfigureAwait(true);

        try
        {
            var exams = await Repository.GetExamsWithTotalAsync(cancellationToken).ConfigureAwait(true);
            var favourites = await Repository.GetFavoriteExamsAsync(cancellationToken).ConfigureAwait(true);

            // Zero is never a real exam id, so nothing is excluded from the dashboard averages
            var stats = await Repository.GetSubjectStatsAsync(0, cancellationToken).ConfigureAwait(true);

            Replace(Exams, exams);
            Replace(Favourites, favourites);
            Replace(SubjectStats, OrderStats(stats));

            Reloaded?.Invoke(this, EventArgs.Empty);
        }
        finally
        {
            _refreshGate.Release();
        }
    }

    /// <summary>Runs a write against the repository and refreshes the collections afterwards.</summary>
    public async Task<T> MutateAsync<T>(Func<IExamRepository, Task<T>> write, CancellationToken cancellationToken = default)
    {
        var result = await write(Repository).ConfigureAwait(true);

        await LoadAsync(cancellationToken).ConfigureAwait(true);

        return result;
    }

    /// <summary>Runs a write against the repository and refreshes the collections afterwards.</summary>
    public Task MutateAsync(Func<IExamRepository, Task> write, CancellationToken cancellationToken = default) =>
        MutateAsync<object?>(
            async repository =>
            {
                await write(repository).ConfigureAwait(true);
                return null;
            },
            cancellationToken);

    /// <summary>
    /// Orders the subject averages by the display order of the subject catalogue, so the bar chart
    /// puts the compulsory subjects first instead of sorting them alphabetically by their key.
    /// </summary>
    private static IEnumerable<SubjectStat> OrderStats(IEnumerable<SubjectStat> stats)
    {
        var byKey = stats.ToDictionary(stat => stat.SubjectKey, StringComparer.Ordinal);

        foreach (var key in SubjectCatalogue.ExamSubjectKeys)
        {
            if (byKey.TryGetValue(key, out var stat))
            {
                yield return stat;
            }
        }
    }

    /// <summary>Replaces the contents of a collection in place, so bindings survive the update.</summary>
    private static void Replace<T>(ObservableCollection<T> target, IEnumerable<T> items)
    {
        target.Clear();

        foreach (var item in items)
        {
            target.Add(item);
        }
    }

    private async void OnRepositoryChanged(object? sender, EventArgs e)
    {
        try
        {
            await LoadAsync().ConfigureAwait(true);
        }
        catch (Exception)
        {
            // A refresh failure must not tear the process down; the next write will retry
        }
    }
}
