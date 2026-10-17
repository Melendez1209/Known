namespace Known.Core.Data;

/// <summary>
///     An exam together with the sum of its subject scores, as read by the history list. The totals
///     come from a LEFT JOIN aggregate, so they are zero for an exam that has not been scored yet.
/// </summary>
public sealed class ExamWithTotal
{
    /// <summary>The exam itself.</summary>
    public Exam Exam { get; init; } = new();

    /// <summary>Sum of every recorded mark in the exam.</summary>
    public float TotalMark { get; init; }

    /// <summary>Sum of every recorded full mark, i.e. the denominator of the overall percentage.</summary>
    public float TotalFullMark { get; init; }
}

/// <summary>An exam together with every subject score recorded for it, as read by the detail screen.</summary>
public sealed class ExamWithScores
{
    /// <summary>The exam itself.</summary>
    public Exam Exam { get; init; } = new();

    /// <summary>Every score recorded for the exam, in no particular order.</summary>
    public List<ExamScore> Scores { get; init; } = [];
}

/// <summary>
///     Average score of one subject across every recorded exam, as aggregated by the detail screen.
///     The current exam is excluded by the query so it is never compared against itself.
/// </summary>
public sealed class SubjectStat
{
    /// <summary>Subject identifier the average belongs to.</summary>
    public string SubjectKey { get; init; } = string.Empty;

    /// <summary>Mean mark achieved in this subject.</summary>
    public float AvgMark { get; init; }

    /// <summary>Mean full mark the mean mark above was measured against.</summary>
    public float AvgFullMark { get; init; }

    /// <summary>How many exams contributed to this average.</summary>
    public int ExamCount { get; init; }
}