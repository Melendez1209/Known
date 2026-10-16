using Microsoft.EntityFrameworkCore;

namespace Known.Core.Data;

/// <summary>
/// Exam storage and queries. Takes the context rather than the individual tables because compound
/// writes, meaning an exam plus its subject scores, must run inside a single transaction.
/// </summary>
public interface IExamRepository
{
    /// <summary>
    /// Raised after any write so observers can reload, standing in for the reactive queries the
    /// Android build expressed as Kotlin flows.
    /// </summary>
    event EventHandler? Changed;

    /// <summary>Every exam with the sum of its scores, newest first.</summary>
    Task<IReadOnlyList<ExamWithTotal>> GetExamsWithTotalAsync(CancellationToken cancellationToken = default);

    /// <summary>Every exam whose name contains <paramref name="query"/>, newest first.</summary>
    Task<IReadOnlyList<ExamWithTotal>> SearchExamsAsync(string? query, CancellationToken cancellationToken = default);

    /// <summary>A single exam, or <see langword="null"/> when the id is unknown.</summary>
    Task<Exam?> GetExamAsync(long examId, CancellationToken cancellationToken = default);

    /// <summary>A single exam together with all of its scores.</summary>
    Task<ExamWithScores?> GetExamWithScoresAsync(long examId, CancellationToken cancellationToken = default);

    /// <summary>
    /// Per subject averages over every exam except <paramref name="excludeExamId"/>, so the detail
    /// screen can show the current exam against the historical average without counting itself.
    /// </summary>
    Task<IReadOnlyList<SubjectStat>> GetSubjectStatsAsync(
        long excludeExamId,
        CancellationToken cancellationToken = default);

    /// <summary>Every exam the student pinned to their favourites.</summary>
    Task<IReadOnlyList<Exam>> GetFavoriteExamsAsync(CancellationToken cancellationToken = default);

    /// <summary>Inserts a new exam with its subject scores and returns the generated exam id.</summary>
    Task<long> InsertExamAsync(
        Exam exam,
        IReadOnlyCollection<ExamScore> scores,
        CancellationToken cancellationToken = default);

    /// <summary>Replaces an existing exam and all of its subject scores, keeping the original id.</summary>
    Task UpdateExamAsync(
        Exam exam,
        IReadOnlyCollection<ExamScore> scores,
        CancellationToken cancellationToken = default);

    /// <summary>Deletes one exam, cascading to its scores.</summary>
    Task DeleteExamAsync(long examId, CancellationToken cancellationToken = default);

    /// <summary>Deletes several exams in one transaction.</summary>
    Task DeleteExamsAsync(IReadOnlyCollection<long> examIds, CancellationToken cancellationToken = default);

    /// <summary>Pins or unpins an exam.</summary>
    Task SetFavoriteAsync(long examId, bool isFavorite, CancellationToken cancellationToken = default);
}

/// <inheritdoc />
public sealed class ExamRepository(KnownDbContext context) : IExamRepository
{
    /// <inheritdoc />
    public event EventHandler? Changed;

    /// <inheritdoc />
    public async Task<IReadOnlyList<ExamWithTotal>> GetExamsWithTotalAsync(
        CancellationToken cancellationToken = default) =>
        await ProjectTotals(context.Exams.AsNoTracking(), cancellationToken).ConfigureAwait(false);

    /// <inheritdoc />
    public async Task<IReadOnlyList<ExamWithTotal>> SearchExamsAsync(
        string? query,
        CancellationToken cancellationToken = default)
    {
        var filtered = string.IsNullOrWhiteSpace(query)
            ? context.Exams.AsNoTracking()
            : context.Exams.AsNoTracking().Where(exam => EF.Functions.Like(exam.Name, $"%{query}%"));

        return await ProjectTotals(filtered, cancellationToken).ConfigureAwait(false);
    }

    /// <inheritdoc />
    public Task<Exam?> GetExamAsync(long examId, CancellationToken cancellationToken = default) =>
        context.Exams.AsNoTracking().FirstOrDefaultAsync(exam => exam.Id == examId, cancellationToken);

    /// <inheritdoc />
    public async Task<ExamWithScores?> GetExamWithScoresAsync(
        long examId,
        CancellationToken cancellationToken = default)
    {
        var exam = await context.Exams
            .AsNoTracking()
            .Include(candidate => candidate.Scores)
            .FirstOrDefaultAsync(candidate => candidate.Id == examId, cancellationToken)
            .ConfigureAwait(false);

        if (exam is null)
        {
            return null;
        }

        return new ExamWithScores { Exam = exam, Scores = exam.Scores };
    }

    /// <inheritdoc />
    public async Task<IReadOnlyList<SubjectStat>> GetSubjectStatsAsync(
        long excludeExamId,
        CancellationToken cancellationToken = default) =>
        await context.ExamScores
            .AsNoTracking()
            .Where(score => score.ExamId != excludeExamId)
            .GroupBy(score => score.SubjectKey)
            .Select(group => new SubjectStat
            {
                SubjectKey = group.Key,
                AvgMark = (float)(group.Average(score => (double?)score.Mark) ?? 0d),
                AvgFullMark = (float)(group.Average(score => (double?)score.FullMark) ?? 0d),
                ExamCount = group.Count()
            })
            .ToListAsync(cancellationToken)
            .ConfigureAwait(false);

    /// <inheritdoc />
    public async Task<IReadOnlyList<Exam>> GetFavoriteExamsAsync(CancellationToken cancellationToken = default) =>
        await context.Exams
            .AsNoTracking()
            .Where(exam => exam.IsFavorite)
            .ToListAsync(cancellationToken)
            .ConfigureAwait(false);

    /// <inheritdoc />
    public async Task<long> InsertExamAsync(
        Exam exam,
        IReadOnlyCollection<ExamScore> scores,
        CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(exam);
        ArgumentNullException.ThrowIfNull(scores);

        await using var transaction = await context.Database
            .BeginTransactionAsync(cancellationToken)
            .ConfigureAwait(false);

        context.Exams.Add(exam);
        await context.SaveChangesAsync(cancellationToken).ConfigureAwait(false);

        var examId = exam.Id;

        if (scores.Count > 0)
        {
            context.ExamScores.AddRange(AttachToExam(scores, examId));
        }

        await context.SaveChangesAsync(cancellationToken).ConfigureAwait(false);
        await transaction.CommitAsync(cancellationToken).ConfigureAwait(false);

        RaiseChanged();
        return examId;
    }

    /// <inheritdoc />
    public async Task UpdateExamAsync(
        Exam exam,
        IReadOnlyCollection<ExamScore> scores,
        CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(exam);
        ArgumentNullException.ThrowIfNull(scores);

        await using var transaction = await context.Database
            .BeginTransactionAsync(cancellationToken)
            .ConfigureAwait(false);

        var stored = await context.Exams.FirstOrDefaultAsync(
                candidate => candidate.Id == exam.Id,
                cancellationToken)
            .ConfigureAwait(false)
            ?? throw new InvalidOperationException($"No exam exists with id {exam.Id}.");

        stored.Name = exam.Name;
        stored.StartDate = exam.StartDate;
        stored.EndDate = exam.EndDate;
        stored.IsFavorite = exam.IsFavorite;

        await context.ExamScores
            .Where(score => score.ExamId == exam.Id)
            .ExecuteDeleteAsync(cancellationToken)
            .ConfigureAwait(false);

        if (scores.Count > 0)
        {
            context.ExamScores.AddRange(AttachToExam(scores, exam.Id));
        }

        await context.SaveChangesAsync(cancellationToken).ConfigureAwait(false);
        await transaction.CommitAsync(cancellationToken).ConfigureAwait(false);

        RaiseChanged();
    }

    /// <inheritdoc />
    public async Task DeleteExamAsync(long examId, CancellationToken cancellationToken = default)
    {
        // The cascade configured on the relationship removes the scores with the exam
        await context.Exams
            .Where(exam => exam.Id == examId)
            .ExecuteDeleteAsync(cancellationToken)
            .ConfigureAwait(false);

        RaiseChanged();
    }

    /// <inheritdoc />
    public async Task DeleteExamsAsync(
        IReadOnlyCollection<long> examIds,
        CancellationToken cancellationToken = default)
    {
        ArgumentNullException.ThrowIfNull(examIds);

        if (examIds.Count == 0)
        {
            return;
        }

        await context.Exams
            .Where(exam => examIds.Contains(exam.Id))
            .ExecuteDeleteAsync(cancellationToken)
            .ConfigureAwait(false);

        RaiseChanged();
    }

    /// <inheritdoc />
    public async Task SetFavoriteAsync(
        long examId,
        bool isFavorite,
        CancellationToken cancellationToken = default)
    {
        await context.Exams
            .Where(exam => exam.Id == examId)
            .ExecuteUpdateAsync(
                setters => setters.SetProperty(exam => exam.IsFavorite, isFavorite),
                cancellationToken)
            .ConfigureAwait(false);

        RaiseChanged();
    }

    /// <summary>
    /// Aggregates the scores of each exam into a total, translating to the same LEFT JOIN the
    /// Android query used so an exam with no scores still reports zero rather than dropping out.
    /// </summary>
    private static async Task<IReadOnlyList<ExamWithTotal>> ProjectTotals(
        IQueryable<Exam> source,
        CancellationToken cancellationToken)
    {
        var rows = await source
            .OrderByDescending(exam => exam.StartDate)
            .Select(exam => new ExamWithTotal
            {
                Exam = exam,
                TotalMark = (float)(exam.Scores.Sum(score => (double?)score.Mark) ?? 0d),
                TotalFullMark = (float)(exam.Scores.Sum(score => (double?)score.FullMark) ?? 0d)
            })
            .ToListAsync(cancellationToken)
            .ConfigureAwait(false);

        return rows;
    }

    /// <summary>
    /// Clones the incoming scores onto the exam id the database just handed out, so a caller built
    /// the list before the insert and cannot have a stale id baked in.
    /// </summary>
    private static IEnumerable<ExamScore> AttachToExam(IReadOnlyCollection<ExamScore> scores, long examId) =>
        scores.Select(score => new ExamScore
        {
            Id = 0,
            ExamId = examId,
            SubjectKey = score.SubjectKey,
            Mark = score.Mark,
            FullMark = score.FullMark
        });

    private void RaiseChanged() => Changed?.Invoke(this, EventArgs.Empty);
}
