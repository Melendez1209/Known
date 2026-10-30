using Known.Core.Data;
using Known.Core.Domain;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Xunit;

namespace Known.Core.Tests;

/// <summary>
///     Exercises the repositories against a real SQLite database, so the LINQ aggregations that stand
///     in for the Android LEFT JOIN queries are verified to translate and produce the same numbers.
/// </summary>
public sealed class ExamRepositoryTests : IAsyncLifetime
{
    private KnownDbContext _context = null!;
    private SqliteConnection _keepAlive = null!;
    private ExamRepository _repository = null!;

    /// <inheritdoc />
    public async Task InitializeAsync()
    {
        // A shared in-memory database lives exactly as long as one connection stays open, so this
        // holder keeps the schema alive while the context opens its own connection alongside it
        _keepAlive = new SqliteConnection(new SqliteConnectionStringBuilder
        {
            DataSource = $"known-test-{Guid.NewGuid():N}",
            Mode = SqliteOpenMode.Memory,
            Cache = SqliteCacheMode.Shared
        }.ToString());

        await _keepAlive.OpenAsync();

        _context = KnownDatabase.CreateContextFor(_keepAlive.ConnectionString);
        await _context.Database.EnsureCreatedAsync();

        _repository = new ExamRepository(_context);
    }

    /// <inheritdoc />
    public async Task DisposeAsync()
    {
        await _context.DisposeAsync();
        await _keepAlive.DisposeAsync();
    }

    private static Exam Exam(string name, long startDate, long endDate)
    {
        return new Exam { Name = name, StartDate = startDate, EndDate = endDate };
    }

    private static ExamScore Score(string subjectKey, float mark, float fullMark = 150f)
    {
        return new ExamScore { SubjectKey = subjectKey, Mark = mark, FullMark = fullMark };
    }

    [Fact]
    public async Task InsertingAnExamAssignsAnIdAndAggregatesItsScores()
    {
        var exam = Exam("Midterm", 1_000, 2_000);

        var examId = await _repository.InsertExamAsync(exam, [Score("chinese", 120f), Score("maths", 98.5f)]);

        Assert.True(examId > 0);

        var rows = await _repository.GetExamsWithTotalAsync();

        var row = Assert.Single(rows);
        Assert.Equal("Midterm", row.Exam.Name);
        Assert.Equal(218.5f, row.TotalMark);
        Assert.Equal(300f, row.TotalFullMark);
    }

    [Fact]
    public async Task AnExamWithoutScoresStillReportsZeroTotals()
    {
        await _repository.InsertExamAsync(Exam("Empty", 1_000, 1_000), []);

        var row = Assert.Single(await _repository.GetExamsWithTotalAsync());

        Assert.Equal(0f, row.TotalMark);
        Assert.Equal(0f, row.TotalFullMark);
    }

    [Fact]
    public async Task ExamsAreListedNewestFirst()
    {
        await _repository.InsertExamAsync(Exam("Oldest", 1_000, 1_000), []);
        await _repository.InsertExamAsync(Exam("Newest", 9_000, 9_000), []);
        await _repository.InsertExamAsync(Exam("Middle", 5_000, 5_000), []);

        var rows = await _repository.GetExamsWithTotalAsync();

        Assert.Equal(["Newest", "Middle", "Oldest"], rows.Select(row => row.Exam.Name));
    }

    [Theory]
    [InlineData("mid", "Midterm")]
    [InlineData("term", "Midterm")]
    [InlineData("MIDTERM", "Midterm")]
    public async Task SearchingMatchesOnASubstringOfTheName(string query, string expected)
    {
        await _repository.InsertExamAsync(Exam("Midterm", 1_000, 1_000), []);
        await _repository.InsertExamAsync(Exam("Final", 9_000, 9_000), []);

        var rows = await _repository.SearchExamsAsync(query);

        Assert.Equal(expected, Assert.Single(rows).Exam.Name);
    }

    [Fact]
    public async Task AnEmptySearchReturnsEveryExam()
    {
        await _repository.InsertExamAsync(Exam("Midterm", 1_000, 1_000), []);
        await _repository.InsertExamAsync(Exam("Final", 9_000, 9_000), []);

        var rows = await _repository.SearchExamsAsync("   ");

        Assert.Equal(["Final", "Midterm"], rows.Select(row => row.Exam.Name));
    }

    [Fact]
    public async Task SearchMatchesNothingForAnUnknownName()
    {
        await _repository.InsertExamAsync(Exam("Midterm", 1_000, 1_000), []);

        Assert.Empty(await _repository.SearchExamsAsync("nonexistent"));
    }

    [Fact]
    public async Task ScoresLoadAlongsideTheirExam()
    {
        var examId = await _repository.InsertExamAsync(
            Exam("Midterm", 1_000, 2_000),
            [Score("chinese", 120f), Score("physics", 88f, 100f)]);

        var loaded = await _repository.GetExamWithScoresAsync(examId);

        Assert.NotNull(loaded);
        Assert.Equal("Midterm", loaded.Exam.Name);
        Assert.Equal(2, loaded.Scores.Count);
        Assert.All(loaded.Scores, score => Assert.Equal(examId, score.ExamId));
    }

    [Fact]
    public async Task AnUnknownExamIdLoadsNothing()
    {
        Assert.Null(await _repository.GetExamAsync(999));
        Assert.Null(await _repository.GetExamWithScoresAsync(999));
    }

    [Fact]
    public async Task SubjectAveragesExcludeTheExamBeingViewed()
    {
        // Two earlier exams give physics a mean of 80, the current one must not count itself
        await _repository.InsertExamAsync(Exam("First", 1_000, 1_000), [Score("physics", 80f, 100f)]);
        await _repository.InsertExamAsync(Exam("Second", 2_000, 2_000), [Score("physics", 80f, 100f)]);
        var current = await _repository.InsertExamAsync(Exam("Third", 3_000, 3_000), [Score("physics", 100f, 100f)]);

        var stats = await _repository.GetSubjectStatsAsync(current);

        var physics = Assert.Single(stats);
        Assert.Equal("physics", physics.SubjectKey);
        Assert.Equal(80f, physics.AvgMark);
        Assert.Equal(100f, physics.AvgFullMark);
        Assert.Equal(2, physics.ExamCount);
    }

    [Fact]
    public async Task UpdatingReplacesEveryScoreOfTheExam()
    {
        var examId = await _repository.InsertExamAsync(
            Exam("Midterm", 1_000, 2_000),
            [Score("chinese", 120f), Score("maths", 98.5f)]);

        var updated = await _repository.GetExamAsync(examId);
        Assert.NotNull(updated);

        await _repository.UpdateExamAsync(updated, [Score("chinese", 130f)]);

        var loaded = await _repository.GetExamWithScoresAsync(examId);
        Assert.NotNull(loaded);

        var score = Assert.Single(loaded.Scores);
        Assert.Equal("chinese", score.SubjectKey);
        Assert.Equal(130f, score.Mark);

        var row = Assert.Single(await _repository.GetExamsWithTotalAsync());
        Assert.Equal(130f, row.TotalMark);
        Assert.Equal(150f, row.TotalFullMark);
    }

    [Fact]
    public async Task UpdatingKeepsTheOriginalIdAndFavouriteFlag()
    {
        var examId = await _repository.InsertExamAsync(Exam("Midterm", 1_000, 2_000), []);
        await _repository.SetFavoriteAsync(examId, true);

        var exam = await _repository.GetExamAsync(examId);
        Assert.NotNull(exam);

        await _repository.UpdateExamAsync(exam, []);

        var reloaded = await _repository.GetExamAsync(examId);
        Assert.NotNull(reloaded);
        Assert.True(reloaded.IsFavorite);
    }

    [Fact]
    public async Task UpdatingAnUnknownExamIsRejected()
    {
        var ghost = Exam("Ghost", 1_000, 1_000);
        ghost.Id = 404;

        await Assert.ThrowsAsync<InvalidOperationException>(() =>
            _repository.UpdateExamAsync(ghost, []));
    }

    [Fact]
    public async Task DeletingAnExamCascadesToItsScores()
    {
        var examId = await _repository.InsertExamAsync(Exam("Midterm", 1_000, 2_000), [Score("chinese", 120f)]);

        await _repository.DeleteExamAsync(examId);

        Assert.Empty(await _repository.GetExamsWithTotalAsync());
        Assert.Empty(await _context.ExamScores.ToListAsync());
    }

    [Fact]
    public async Task DeletingSeveralExamsLeavesTheOthersAlone()
    {
        var keep = await _repository.InsertExamAsync(Exam("Keep", 1_000, 1_000), []);
        var dropA = await _repository.InsertExamAsync(Exam("DropA", 2_000, 2_000), []);
        var dropB = await _repository.InsertExamAsync(Exam("DropB", 3_000, 3_000), []);

        await _repository.DeleteExamsAsync([dropA, dropB]);

        var rows = await _repository.GetExamsWithTotalAsync();
        Assert.Equal(keep, Assert.Single(rows).Exam.Id);
    }

    [Fact]
    public async Task DeletingNothingIsHarmless()
    {
        await _repository.InsertExamAsync(Exam("Keep", 1_000, 1_000), []);

        await _repository.DeleteExamsAsync([]);

        Assert.Single(await _repository.GetExamsWithTotalAsync());
    }

    [Fact]
    public async Task FavouritesRoundTrip()
    {
        var pinned = await _repository.InsertExamAsync(Exam("Pinned", 1_000, 1_000), []);
        await _repository.InsertExamAsync(Exam("Other", 2_000, 2_000), []);

        await _repository.SetFavoriteAsync(pinned, true);

        var favourites = await _repository.GetFavoriteExamsAsync();
        Assert.Equal("Pinned", Assert.Single(favourites).Name);

        await _repository.SetFavoriteAsync(pinned, false);
        Assert.Empty(await _repository.GetFavoriteExamsAsync());
    }

    [Fact]
    public async Task AScoreCannotBeRecordedTwiceForTheSameSubjectAndExam()
    {
        var clash = Exam("Midterm", 1_000, 2_000);

        await Assert.ThrowsAnyAsync<DbUpdateException>(() =>
            _repository.InsertExamAsync(clash, [Score("chinese", 120f), Score("chinese", 90f)]));

        // The rejected insert rolled back as a whole, so the exam was never written either
        Assert.Empty(await _repository.GetExamsWithTotalAsync());
        Assert.Empty(await _context.ExamScores.ToListAsync());
    }

    [Fact]
    public async Task WritingRaisesAChangeNotification()
    {
        var raised = 0;
        _repository.Changed += (_, _) => raised++;

        var examId = await _repository.InsertExamAsync(Exam("Midterm", 1_000, 1_000), []);
        await _repository.SetFavoriteAsync(examId, true);
        await _repository.DeleteExamAsync(examId);

        Assert.Equal(3, raised);
    }
}

/// <summary>Covers the single-row settings table and its defaults.</summary>
public sealed class SettingsRepositoryTests : IAsyncLifetime
{
    private KnownDbContext _context = null!;
    private SqliteConnection _keepAlive = null!;
    private SettingsRepository _repository = null!;

    /// <inheritdoc />
    public async Task InitializeAsync()
    {
        _keepAlive = new SqliteConnection(new SqliteConnectionStringBuilder
        {
            DataSource = $"known-test-{Guid.NewGuid():N}",
            Mode = SqliteOpenMode.Memory,
            Cache = SqliteCacheMode.Shared
        }.ToString());

        await _keepAlive.OpenAsync();

        _context = KnownDatabase.CreateContextFor(_keepAlive.ConnectionString);
        await _context.Database.EnsureCreatedAsync();

        _repository = new SettingsRepository(_context);
    }

    /// <inheritdoc />
    public async Task DisposeAsync()
    {
        await _context.DisposeAsync();
        await _keepAlive.DisposeAsync();
    }

    [Fact]
    public async Task InitialisingCreatesExactlyOneDefaultsRow()
    {
        var settings = await _repository.InitialiseAsync();

        Assert.Equal(1, settings.Id);
        Assert.True(settings.IsFirstLogin);
        Assert.Equal(AppThemeMode.FollowSystem, settings.AppThemeValue);
        Assert.Equal("", settings.SelectedLanguage);
        Assert.Equal(StudentIdentity.None, settings.Identity);
        Assert.Equal(TrendMetric.Marks, settings.TrendMetric);
        Assert.Equal(TrendMetric.Marks, settings.SubjectMetric);
        Assert.Equal(HistoryViewMode.Standard, settings.HistoryViewMode);
    }

    [Fact]
    public async Task InitialisingTwiceDoesNotResetStoredValues()
    {
        await _repository.InitialiseAsync();

        var stored = await _repository.GetAsync();
        Assert.NotNull(stored);
        stored.Region = "Shanghai";
        stored.IsFirstLogin = false;
        await _repository.SaveAsync(stored);

        var reloaded = await _repository.InitialiseAsync();

        Assert.Equal("Shanghai", reloaded.Region);
        Assert.False(reloaded.IsFirstLogin);
    }

    [Fact]
    public async Task SavingUpdatesEveryFieldInPlace()
    {
        await _repository.InitialiseAsync();

        await _repository.SaveAsync(new Settings
        {
            Id = SettingsRepository.SingletonId,
            AppThemeValue = AppThemeMode.Dark,
            IsHighContrastMode = true,
            SelectedLanguage = "zh-Hans",
            IsFirstLogin = false,
            Identity = StudentIdentity.Student,
            Region = "Shanghai",
            SelectedSubjects = "physics,chemistry",
            TrendMetric = TrendMetric.Rate,
            SubjectMetric = TrendMetric.Rate,
            HistoryViewMode = HistoryViewMode.Compact
        });

        var stored = await _repository.GetAsync();

        Assert.NotNull(stored);
        Assert.Equal(AppThemeMode.Dark, stored.AppThemeValue);
        Assert.True(stored.IsHighContrastMode);
        Assert.Equal("zh-Hans", stored.SelectedLanguage);
        Assert.False(stored.IsFirstLogin);
        Assert.Equal(StudentIdentity.Student, stored.Identity);
        Assert.Equal("Shanghai", stored.Region);
        Assert.Equal("physics,chemistry", stored.SelectedSubjects);
        Assert.Equal(TrendMetric.Rate, stored.TrendMetric);
        Assert.Equal(TrendMetric.Rate, stored.SubjectMetric);
        Assert.Equal(HistoryViewMode.Compact, stored.HistoryViewMode);
        Assert.Single(await _context.Settings.ToListAsync());
    }

    [Fact]
    public async Task SavingRaisesAChangeNotification()
    {
        var raised = 0;
        _repository.Changed += (_, _) => raised++;

        await _repository.InitialiseAsync();
        await _repository.SaveAsync(new Settings());

        Assert.Equal(2, raised);
    }
}