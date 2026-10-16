using Known.Core.Data;
using Known.Core.Domain;
using Xunit;

namespace Known.Core.Tests;

/// <summary>
/// Port of the Android <c>ExamCoreKtTest</c>, case for case, so the scoring rules are known to
/// behave identically on the desktop build.
/// </summary>
public class ExamScoringTests
{
    private static ExamScore Score(string subjectKey, float mark, float fullMark = 150f) =>
        new() { ExamId = 7L, SubjectKey = subjectKey, Mark = mark, FullMark = fullMark };

    private static ExamWithTotal ExamWith(long id, float mark, float fullMark) =>
        new()
        {
            Exam = new Exam { Id = id, Name = $"Exam {id}" },
            TotalMark = mark,
            TotalFullMark = fullMark
        };

    /// <summary>Local midnight of a date, matching the local Calendar the Android test used.</summary>
    private static long At(int year, int month, int day) =>
        new DateTimeOffset(new DateTime(year, month, day, 0, 0, 0, DateTimeKind.Local))
            .ToUnixTimeMilliseconds();

    // --- ParseScoreOrNull ---

    [Fact]
    public void ParsesPlainAndHalfScores()
    {
        Assert.Equal(100f, ExamScoring.ParseScoreOrNull("100"));
        Assert.Equal(98.5f, ExamScoring.ParseScoreOrNull("98.5"));
        Assert.Equal(0.5f, ExamScoring.ParseScoreOrNull(".5"));
        Assert.Equal(100f, ExamScoring.ParseScoreOrNull(" 100 "));
    }

    [Fact]
    public void RejectsUnusableScoreText()
    {
        Assert.Null(ExamScoring.ParseScoreOrNull(""));
        Assert.Null(ExamScoring.ParseScoreOrNull("."));
        Assert.Null(ExamScoring.ParseScoreOrNull("-1"));
        Assert.Null(ExamScoring.ParseScoreOrNull("abc"));
    }

    // --- IsValidScoreInput ---

    [Fact]
    public void OnlyIntegersAndHalfPointsAreAccepted()
    {
        Assert.True(ExamScoring.IsValidScoreInput(""));
        Assert.True(ExamScoring.IsValidScoreInput("0"));
        Assert.True(ExamScoring.IsValidScoreInput("100"));
        Assert.True(ExamScoring.IsValidScoreInput("98.5"));
        Assert.False(ExamScoring.IsValidScoreInput("98.4"));
        Assert.False(ExamScoring.IsValidScoreInput("."));
        Assert.False(ExamScoring.IsValidScoreInput("-1"));
    }

    [Fact]
    public void OverlongInputIsRefusedSoTheFieldCannotOverflow()
    {
        Assert.True(ExamScoring.IsValidScoreInput("1234567"));
        Assert.False(ExamScoring.IsValidScoreInput("12345678"));
    }

    // --- FormatScoreInput and ProcessScoreInput ---

    [Fact]
    public void RedundantTrailingZeroIsDropped()
    {
        Assert.Equal("150", ExamScoring.FormatScoreInput("150.0"));
        Assert.Equal("98.5", ExamScoring.FormatScoreInput("98.5"));
        Assert.Equal("", ExamScoring.FormatScoreInput(""));
        Assert.Equal("abc", ExamScoring.FormatScoreInput("abc"));
    }

    [Fact]
    public void TrailingDotIsCompletedToAHalfPoint()
    {
        Assert.Equal("98.5", ExamScoring.ProcessScoreInput("98."));
        Assert.Equal("98", ExamScoring.ProcessScoreInput("98"));
        Assert.Equal("98.4", ExamScoring.ProcessScoreInput("98.4"));
        Assert.Equal("", ExamScoring.ProcessScoreInput(""));
    }

    // --- slider helpers: a blank or half-typed full mark must never throw ---

    [Fact]
    public void SliderStepsStaySafeForBlankAndHalfTypedFullMarks()
    {
        Assert.Equal(0, ExamScoring.SliderSteps(""));
        Assert.Equal(0, ExamScoring.SliderSteps("."));
        Assert.Equal(0, ExamScoring.SliderSteps("0"));
        Assert.Equal(99, ExamScoring.SliderSteps("50"));
        Assert.Equal(299, ExamScoring.SliderSteps("150"));
    }

    [Fact]
    public void SliderValueFallsBackToZeroRatherThanThrowing()
    {
        Assert.Equal(0f, ExamScoring.SliderValue("", "150"));
        Assert.Equal(0f, ExamScoring.SliderValue(".", "150"));
        Assert.Equal(0f, ExamScoring.SliderValue("75", ""));
        Assert.Equal(0f, ExamScoring.SliderValue("75", "0"));
        Assert.Equal(0.5f, ExamScoring.SliderValue("75", "150"));

        // Dragging past the full mark is clamped rather than reported above 1
        Assert.Equal(1f, ExamScoring.SliderValue("200", "150"));
    }

    [Fact]
    public void SliderPositionsSnapBackToHalfPoints()
    {
        Assert.Equal("75", ExamScoring.SnappedScore(0.5f, "150"));
        Assert.Equal("50", ExamScoring.SnappedScore(0.333f, "150"));
        Assert.Equal("50", ExamScoring.SnappedScore(1f, "50"));
        Assert.Equal("0.5", ExamScoring.SnappedScore(0.5f, "1"));
        Assert.Equal("", ExamScoring.SnappedScore(0.5f, "."));
    }

    // --- BuildExamScores ---

    [Fact]
    public void UntickedAndBlankRowsAreNeverPersisted()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "120", FullMark: "150"),
            new ScoreInput("maths", Checked: false, Mark: "140", FullMark: "150"),
            new ScoreInput("physics", Checked: true, Mark: "", FullMark: "100")
        };

        var scores = ExamScoring.BuildExamScores(7L, inputs);

        var score = Assert.Single(scores);
        Assert.Equal("chinese", score.SubjectKey);
        Assert.Equal(7L, score.ExamId);
        Assert.Equal(120f, score.Mark);
    }

    [Fact]
    public void ImpossibleRowsAreReportedInsteadOfSilentlyDropped()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "160", FullMark: "150"),
            new ScoreInput("maths", Checked: true, Mark: "140", FullMark: "150")
        };

        var invalid = new List<ScoreInput>();

        var scores = ExamScoring.BuildExamScores(7L, inputs, invalid.Add);

        Assert.Single(scores);
        Assert.Equal(["chinese"], invalid.Select(input => input.SubjectKey));
    }

    // --- ValidateScoreInputs ---

    [Fact]
    public void AFormWithoutASingleMarkCannotBeSaved()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "", FullMark: "150"),
            new ScoreInput("pe", Checked: false, Mark: "", FullMark: "100")
        };

        Assert.Equal(ScoreValidation.NoMarkEntered, ExamScoring.ValidateScoreInputs(inputs));
    }

    [Fact]
    public void AMarkAboveItsFullMarkBlocksTheSave()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "160", FullMark: "150"),
            new ScoreInput("maths", Checked: true, Mark: "140", FullMark: "150")
        };

        Assert.Equal(ScoreValidation.MarkExceedsFull, ExamScoring.ValidateScoreInputs(inputs));
    }

    [Fact]
    public void AMarkWithoutAUsableFullMarkBlocksTheSave()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "140", FullMark: ".")
        };

        Assert.Equal(ScoreValidation.MarkExceedsFull, ExamScoring.ValidateScoreInputs(inputs));
    }

    [Fact]
    public void OneUsableMarkIsEnoughToSave()
    {
        var inputs = new[]
        {
            new ScoreInput("chinese", Checked: true, Mark: "140", FullMark: "150"),
            new ScoreInput("maths", Checked: true, Mark: "", FullMark: "150")
        };

        Assert.Equal(ScoreValidation.Valid, ExamScoring.ValidateScoreInputs(inputs));
    }

    // --- DefaultScoreInputs ---

    [Fact]
    public void CompulsorySubjectsAreAlwaysTicked()
    {
        var inputs = ExamScoring.DefaultScoreInputs(new HashSet<string> { "physics" });

        Assert.Equal(SubjectCatalogue.ExamSubjectKeys, inputs.Select(input => input.SubjectKey));
        Assert.All(
            inputs.Take(SubjectCatalogue.CompulsorySubjectCount),
            input => Assert.True(input.Checked));
        Assert.False(inputs[^1].Checked);
    }

    [Fact]
    public void OptionalSubjectsFollowTheStoredSelection()
    {
        var inputs = ExamScoring.DefaultScoreInputs(new HashSet<string> { "physics", "geography" });

        Assert.Equal(
            ["chinese", "maths", "foreign_language", "physics", "geography"],
            inputs.Where(input => input.Checked).Select(input => input.SubjectKey));
    }

    [Fact]
    public void AnEmptySelectionKeepsTheHistoricalDefault()
    {
        var inputs = ExamScoring.DefaultScoreInputs(new HashSet<string>());

        Assert.Equal(
            SubjectCatalogue.ExamSubjectKeys.Where(key => key != "pe"),
            inputs.Where(input => input.Checked).Select(input => input.SubjectKey));

        Assert.All(inputs, input =>
        {
            Assert.Equal("", input.Mark);
            Assert.Equal(SubjectCatalogue.DefaultFullMark, input.FullMark);
        });
    }

    // --- RecordedSubjectKeys ---

    [Fact]
    public void OnlySubjectsCarryingAScoreAreKept()
    {
        var scores = new[]
        {
            Score("geography", 80f),
            Score("chinese", 90f),
            Score("chinese", 88f)
        };

        Assert.Equal(
            ["chinese", "geography"],
            ExamScoring.RecordedSubjectKeys(SubjectCatalogue.ExamSubjectKeys, scores));
    }

    [Fact]
    public void NoScoreAtAllMeansNoSubject()
    {
        Assert.Empty(
            ExamScoring.RecordedSubjectKeys(SubjectCatalogue.ExamSubjectKeys, Array.Empty<ExamScore>()));
    }

    [Fact]
    public void AZeroMarkStillCountsAsRecordedData()
    {
        var scores = new[] { Score("pe", 0f, fullMark: 100f) };

        Assert.Equal(["pe"], ExamScoring.RecordedSubjectKeys(SubjectCatalogue.ExamSubjectKeys, scores));
    }

    // --- totals and percentage ---

    [Fact]
    public void TotalsSumEveryRecordedMark()
    {
        var scores = new[]
        {
            Score("chinese", 120f, 150f),
            Score("maths", 98.5f, 150f)
        };

        Assert.Equal(218.5f, ExamScoring.TotalMark(scores));
        Assert.Equal(300f, ExamScoring.TotalFullMark(scores));
    }

    [Fact]
    public void PercentageNeverDividesByZeroAndNeverExceedsOneHundred()
    {
        Assert.Equal(0f, ExamScoring.Percentage(50f, 0f));
        Assert.Equal(50f, ExamScoring.Percentage(50f, 100f));
        Assert.Equal(100f, ExamScoring.Percentage(200f, 100f));
    }

    // --- RankOf ---

    [Fact]
    public void RankFollowsThePercentageHighestFirst()
    {
        ExamWithTotal[] exams = [ExamWith(1L, 90f, 100f), ExamWith(2L, 80f, 100f), ExamWith(3L, 70f, 100f)];

        Assert.Equal(1, ExamScoring.RankOf(1L, exams));
        Assert.Equal(2, ExamScoring.RankOf(2L, exams));
        Assert.Equal(3, ExamScoring.RankOf(3L, exams));
    }

    [Fact]
    public void EqualPercentagesShareARank()
    {
        ExamWithTotal[] exams = [ExamWith(1L, 80f, 100f), ExamWith(2L, 80f, 100f)];

        Assert.Equal(1, ExamScoring.RankOf(1L, exams));
        Assert.Equal(1, ExamScoring.RankOf(2L, exams));
    }

    [Fact]
    public void UnknownAndEmptyInputsDefaultToFirstPlace()
    {
        Assert.Equal(1, ExamScoring.RankOf(1L, Array.Empty<ExamWithTotal>()));
        Assert.Equal(1, ExamScoring.RankOf(42L, [ExamWith(1L, 80f, 100f)]));
    }

    // --- historical average ---

    [Fact]
    public void HistoricalAverageIsLookedUpBySubjectKey()
    {
        SubjectStat[] stats =
        [
            new() { SubjectKey = "physics", AvgMark = 80f, AvgFullMark = 100f, ExamCount = 3 },
            new() { SubjectKey = "chemistry", AvgMark = 70f, AvgFullMark = 100f, ExamCount = 3 }
        ];

        Assert.Equal("physics", ExamScoring.HistoricalAverage("physics", stats)?.SubjectKey);
        Assert.Equal(80f, ExamScoring.HistoricalAverage("physics", stats)?.AvgMark);
        Assert.Null(ExamScoring.HistoricalAverage("geography", stats));
        Assert.Null(ExamScoring.HistoricalAverage("physics", Array.Empty<SubjectStat>()));
    }

    [Fact]
    public void AverageDeltaComparesAgainstTheRecordedHistory()
    {
        var stat = new SubjectStat
        {
            SubjectKey = "physics",
            AvgMark = 80f,
            AvgFullMark = 100f,
            ExamCount = 3
        };

        // Both operands are exactly representable, so the deltas come out exact
        Assert.Equal(10f, ExamScoring.AverageDelta(90f, 100f, stat)!.Value);
        Assert.Equal(-5f, ExamScoring.AverageDelta(75f, 100f, stat)!.Value);
    }

    [Fact]
    public void AverageDeltaIsUnavailableWithoutHistoryOrASensibleFullMark()
    {
        var stat = new SubjectStat
        {
            SubjectKey = "physics",
            AvgMark = 80f,
            AvgFullMark = 100f,
            ExamCount = 3
        };

        Assert.Null(ExamScoring.AverageDelta(90f, 100f, null));
        Assert.Null(ExamScoring.AverageDelta(90f, 0f, stat));
        Assert.Null(ExamScoring.AverageDelta(
            90f,
            100f,
            new SubjectStat { SubjectKey = "physics", AvgMark = 80f, AvgFullMark = 0f, ExamCount = 3 }));
    }

    // --- dates ---

    [Fact]
    public void ASingleDayFormatsWithoutARange()
    {
        var day = At(2026, 10, 4);

        Assert.Equal("2026-10-04", ExamDates.FormatDateRange(day, day));
        Assert.Equal("2026-10-04", ExamDates.DefaultExamName(day));
    }

    [Fact]
    public void ALongerExamFormatsBothEndsOfTheRange()
    {
        Assert.Equal(
            "2026-10-04 - 2026-10-05",
            ExamDates.FormatDateRange(At(2026, 10, 4), At(2026, 10, 5)));
    }
}
