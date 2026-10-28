using System.Globalization;
using Known.Core.Data;

namespace Known.Core.Domain;

/// <summary>
///     Pure helpers for the exam feature, covering score-field parsing, slider arithmetic and the
///     aggregates shown on the history and detail screens. Kept free of Android and WinUI types so
///     every function can be unit tested on the host; anything needing a window belongs in a view model.
/// </summary>
public static class ExamScoring
{
    /// <summary>Longest string a score field may hold, which also keeps every value inside Int32 range.</summary>
    private const int MaxScoreLength = 7;

    /// <summary>
    ///     Matches the strict float parsing of Kotlin's <c>toFloatOrNull</c>: leading and trailing
    ///     white space, an optional sign, an optional decimal point and an optional exponent, but no
    ///     group separators and no trailing rubbish.
    /// </summary>
    private const NumberStyles ScoreNumberStyles =
        NumberStyles.AllowLeadingWhite |
        NumberStyles.AllowTrailingWhite |
        NumberStyles.AllowLeadingSign |
        NumberStyles.AllowDecimalPoint |
        NumberStyles.AllowExponent;

    /// <summary>
    ///     The blank form shown when a new exam is created: every subject listed, every field reset.
    /// </summary>
    /// <remarks>
    ///     The compulsory subjects are always ticked because their rows carry no checkbox. The optional
    ///     ones follow <paramref name="selectedSubjects" />, the student's own selection stored in the
    ///     general settings, so the input screen opens with the subjects they actually sit. An empty set
    ///     means no selection has been stored yet, in which case the historical default of every
    ///     subject but PE is kept.
    /// </remarks>
    public static IReadOnlyList<ScoreInput> DefaultScoreInputs(IReadOnlySet<string> selectedSubjects)
    {
        ArgumentNullException.ThrowIfNull(selectedSubjects);

        var inputs = new List<ScoreInput>(SubjectCatalogue.ExamSubjectKeys.Count);
        var lastIndex = SubjectCatalogue.ExamSubjectKeys.Count - 1;

        for (var index = 0; index <= lastIndex; index++)
        {
            var key = SubjectCatalogue.ExamSubjectKeys[index];

            var isChecked = index < SubjectCatalogue.CompulsorySubjectCount || (selectedSubjects.Count == 0
                ? index != lastIndex
                : selectedSubjects.Contains(key));

            inputs.Add(new ScoreInput(key, isChecked));
        }

        return inputs;
    }

    /// <summary>
    ///     Parses a score the user may type, for example <c>100</c>, <c>98.5</c> or <c>.5</c>. Returns
    ///     <see langword="null" /> when the input is not a usable number, so callers never have to guard
    ///     against a parse exception.
    /// </summary>
    public static float? ParseScoreOrNull(string? input)
    {
        if (string.IsNullOrWhiteSpace(input)) return null;

        if (!float.TryParse(input, ScoreNumberStyles, CultureInfo.InvariantCulture, out var value)) return null;

        if (!float.IsFinite(value) || value < 0f) return null;

        return value;
    }

    /// <summary>
    ///     Validates an entry in a score field. Empty is allowed so the field can be cleared; otherwise
    ///     the value must be a non-negative integer or half point no longer than
    ///     <see cref="MaxScoreLength" /> characters, matching the slider's step size.
    /// </summary>
    public static bool IsValidScoreInput(string input)
    {
        ArgumentNullException.ThrowIfNull(input);

        if (input.Length == 0) return true;

        if (input.Length > MaxScoreLength) return false;

        var value = ParseScoreOrNull(input);
        if (value is null) return false;

        return IsWholeNumber(value.Value) || input.EndsWith(".5", StringComparison.Ordinal);
    }

    /// <summary>
    ///     Normalises a score for display, dropping a redundant trailing <c>.0</c> so <c>150.0</c> reads
    ///     as <c>150</c>. Unparsable input is returned unchanged rather than throwing.
    /// </summary>
    public static string FormatScoreInput(string input)
    {
        ArgumentNullException.ThrowIfNull(input);

        if (input.Length == 0) return "";

        if (!TryParseScore(input, out var value)) return input;

        return FormatScoreValue(value);
    }

    /// <summary>
    ///     Formats an already validated score, dropping a redundant trailing <c>.0</c> so
    ///     <c>150.0</c> reads as <c>150</c>. The input side stays a string, but this is what a caller
    ///     holding a number of needs, such as the slider.
    /// </summary>
    public static string FormatScoreValue(float value)
    {
        return IsWholeNumber(value)
            ? ((long)value).ToString(CultureInfo.InvariantCulture)
            : value.ToString("0.0###", CultureInfo.InvariantCulture);
    }

    /// <summary>
    ///     The single pipeline a score text field runs on every change: a trailing <c>.</c> is completed
    ///     to <c>.5</c> so half points can be typed naturally, then the result is normalised. Input that
    ///     would not pass <see cref="IsValidScoreInput" /> is returned untouched so the field can hold it
    ///     while the user keeps typing.
    /// </summary>
    public static string ProcessScoreInput(string input)
    {
        ArgumentNullException.ThrowIfNull(input);

        if (input.Length == 0) return input;

        var completed = input.EndsWith(".", StringComparison.Ordinal) ? input + "5" : input;

        return IsValidScoreInput(completed) ? FormatScoreInput(completed) : input;
    }

    /// <summary>
    ///     Slider position for a row, as <c>0..1</c>. Returns zero when either field is blank or
    ///     unparsable, so a partially filled form can never throw.
    /// </summary>
    public static float SliderValue(string mark, string fullMark)
    {
        var markValue = ParseScoreOrNull(mark);
        if (markValue is null) return 0f;

        var fullValue = ParseScoreOrNull(fullMark);
        if (fullValue is null || fullValue.Value <= 0f) return 0f;

        return Coerce(markValue.Value / fullValue.Value, 0f, 1f);
    }

    /// <summary>
    ///     Number of slider stops for a row. The mark is snapped to half point increments, hence two
    ///     stops per full mark unit. Returns zero when the full mark is blank, unparsable or zero, so a
    ///     cleared or half typed full mark can no longer crash the slider.
    /// </summary>
    public static int SliderSteps(string fullMark)
    {
        var fullValue = ParseScoreOrNull(fullMark);
        if (fullValue is null || fullValue.Value <= 0f) return 0;

        return Math.Max(0, (int)(fullValue.Value * 2f) - 1);
    }

    /// <summary>
    ///     Snaps a fractional slider position back to the nearest half point on a <c>0..fullMark</c>
    ///     scale, so dragging never produces a value the input fields would reject. Returns an empty
    ///     string when the full mark is unusable.
    /// </summary>
    public static string SnappedScore(float fraction, string fullMark)
    {
        var fullValue = ParseScoreOrNull(fullMark);
        if (fullValue is null) return "";

        var rounded = MathF.Round(fullValue.Value * fraction * 2f, MidpointRounding.AwayFromZero) / 2f;

        return FormatScoreValue(rounded);
    }

    /// <summary>
    ///     Turns the input screen's rows into persistable scores. Unticked and blank rows are skipped
    ///     rather than crashing the save; a mark greater than its full mark, or a full mark that does
    ///     not parse, is reported through <paramref name="onInvalidRow" /> so the caller can surface an
    ///     error instead of silently dropping the row.
    /// </summary>
    /// <returns>An empty list when <paramref name="inputs" /> contains no usable score at all.</returns>
    public static IReadOnlyList<ExamScore> BuildExamScores(
        long examId,
        IEnumerable<ScoreInput> inputs,
        Action<ScoreInput>? onInvalidRow = null)
    {
        ArgumentNullException.ThrowIfNull(inputs);

        var scores = new List<ExamScore>();

        foreach (var input in inputs)
        {
            if (!input.Checked || string.IsNullOrWhiteSpace(input.Mark)) continue;

            var mark = ParseScoreOrNull(input.Mark);
            var fullMark = ParseScoreOrNull(input.FullMark);

            if (mark is null || fullMark is null || mark.Value > fullMark.Value)
            {
                onInvalidRow?.Invoke(input);
                continue;
            }

            scores.Add(new ExamScore
            {
                Id = 0,
                ExamId = examId,
                SubjectKey = input.SubjectKey,
                Mark = mark.Value,
                FullMark = fullMark.Value
            });
        }

        return scores;
    }

    /// <summary>
    ///     The subjects of <paramref name="order" /> that actually carry a score, in display order.
    ///     Subjects the student never ticked, or left blank, have no score row at all, so the detail
    ///     screen hides them.
    /// </summary>
    public static IReadOnlyList<string> RecordedSubjectKeys(
        IEnumerable<string> order,
        IEnumerable<ExamScore> scores)
    {
        ArgumentNullException.ThrowIfNull(order);
        ArgumentNullException.ThrowIfNull(scores);

        var recorded = scores.Select(score => score.SubjectKey).ToHashSet(StringComparer.Ordinal);

        return order.Where(recorded.Contains).ToList();
    }

    /// <summary>Whether <paramref name="inputs" /> can be saved, so the caller can block the save and explain why.</summary>
    public static ScoreValidation ValidateScoreInputs(IEnumerable<ScoreInput> inputs)
    {
        ArgumentNullException.ThrowIfNull(inputs);

        var ticked = inputs.Where(input => input.Checked).ToList();

        if (ticked.All(input => string.IsNullOrWhiteSpace(input.Mark))) return ScoreValidation.NoMarkEntered;

        // A ticked row the student left empty is skipped on save, so only rows carrying a mark count
        var hasInvalidRow = ticked.Any(input =>
        {
            if (string.IsNullOrWhiteSpace(input.Mark)) return false;

            var mark = ParseScoreOrNull(input.Mark);
            var fullMark = ParseScoreOrNull(input.FullMark);

            return mark is null || fullMark is null || mark.Value > fullMark.Value;
        });

        return hasInvalidRow ? ScoreValidation.MarkExceedsFull : ScoreValidation.Valid;
    }

    /// <summary>Sum of every recorded mark, used for the totals shown in the list and on the detail screen.</summary>
    public static float TotalMark(IEnumerable<ExamScore> scores)
    {
        ArgumentNullException.ThrowIfNull(scores);

        return (float)scores.Sum(score => (double)score.Mark);
    }

    /// <summary>Sum of every full mark, i.e. the denominator of the overall percentage.</summary>
    public static float TotalFullMark(IEnumerable<ExamScore> scores)
    {
        ArgumentNullException.ThrowIfNull(scores);

        return (float)scores.Sum(score => (double)score.FullMark);
    }

    /// <summary>
    ///     Percentage of <paramref name="mark" /> against <paramref name="fullMark" />, as <c>0..100</c>.
    ///     Returns zero when nothing has been entered, avoiding a division by zero.
    /// </summary>
    public static float Percentage(float mark, float fullMark)
    {
        if (fullMark <= 0f) return 0f;

        return Coerce(mark / fullMark * 100f, 0f, 100f);
    }

    /// <summary>
    /// Mean total mark across every exam that carries a score. An exam with no scores is left out
    /// rather than counted as zero, which would drag the average down for a form the student simply
    /// has not finished filling in.
    /// </summary>
    public static float AverageMarkOf(IEnumerable<ExamWithTotal> exams)
    {
        ArgumentNullException.ThrowIfNull(exams);

        var scored = ScoredExams(exams);

        return scored.Count == 0
            ? 0f
            : (float)scored.Average(exam => (double)exam.TotalMark);
    }

    /// <summary>
    /// Mean percentage across every exam that carries a score, excluding unscoured exams for the
    /// same reason as <see cref="AverageMarkOf"/>.
    /// </summary>
    public static float AverageRateOf(IEnumerable<ExamWithTotal> exams)
    {
        ArgumentNullException.ThrowIfNull(exams);

        var scored = ScoredExams(exams);

        return scored.Count == 0
            ? 0f
            : (float)scored.Average(exam => (double)Percentage(exam.TotalMark, exam.TotalFullMark));
    }

    /// <summary>Keeps only the exams whose totals mean something.</summary>
    private static List<ExamWithTotal> ScoredExams(IEnumerable<ExamWithTotal> exams) =>
        exams.Where(exam => exam.TotalFullMark > 0f).ToList();

    /// <summary>
    ///     One based rank of <paramref name="examId" /> among <paramref name="exams" />, ordered by
    ///     percentage with the highest first. Exams without any score count as zero per cent and sort
    ///     last. Returns <c>1</c> for unknown or empty input rather than throwing.
    /// </summary>
    public static int RankOf(long examId, IReadOnlyCollection<ExamWithTotal> exams)
    {
        ArgumentNullException.ThrowIfNull(exams);

        if (exams.Count == 0) return 1;

        var target = exams.FirstOrDefault(candidate => candidate.Exam.Id == examId);
        if (target is null) return 1;

        var targetPercentage = Percentage(target.TotalMark, target.TotalFullMark);

        // Strictly greater, so tied exams share the same rank
        var ahead = exams.Count(candidate =>
            Percentage(candidate.TotalMark, candidate.TotalFullMark) > targetPercentage);

        return ahead + 1;
    }

    /// <summary>
    ///     Looks up the historical average of <paramref name="subjectKey" /> among <paramref name="stats" />,
    ///     which already exclude the current exam. Returns <see langword="null" /> when there is no
    ///     history to compare against.
    /// </summary>
    public static SubjectStat? HistoricalAverage(string subjectKey, IEnumerable<SubjectStat> stats)
    {
        ArgumentNullException.ThrowIfNull(stats);

        return stats.FirstOrDefault(stat => stat.SubjectKey == subjectKey);
    }

    /// <summary>
    ///     Signed difference, in percentage points, between the current exam's mark for one subject and
    ///     its historical average. Positive means the student improved. Returns <see langword="null" />
    ///     when there is no history or either full mark is meaningless.
    /// </summary>
    public static float? AverageDelta(float mark, float fullMark, SubjectStat? stat)
    {
        if (stat is null || stat.AvgFullMark <= 0f || fullMark <= 0f) return null;

        return Percentage(mark, fullMark) - Percentage(stat.AvgMark, stat.AvgFullMark);
    }

    /// <summary>
    ///     Parses without the non-negative guard, so <see cref="FormatScoreInput" /> can normalise a
    ///     negative or exponent form the same way the Android build did.
    /// </summary>
    private static bool TryParseScore(string input, out float value)
    {
        return float.TryParse(input, ScoreNumberStyles, CultureInfo.InvariantCulture, out value)
               && float.IsFinite(value);
    }

    /// <summary>
    ///     Whether the value carries no fractional part. Comparing against a truncated copy would
    ///     overflow for values outside Int32, which the length guard already excludes, but a modulo
    ///     keeps the check safe for every caller.
    /// </summary>
    private static bool IsWholeNumber(float value)
    {
        return value % 1f == 0f;
    }

    /// <summary>
    ///     Clamps like Kotlin's <c>coerceIn</c>, including its behaviour of letting a NaN through
    ///     untouched rather than throwing.
    /// </summary>
    private static float Coerce(float value, float minimum, float maximum)
    {
        if (value < minimum) return minimum;

        if (value > maximum) return maximum;

        return value;
    }
}