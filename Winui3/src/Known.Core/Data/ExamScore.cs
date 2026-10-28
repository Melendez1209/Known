namespace Known.Core.Data;

/// <summary>One subject's score inside an exam.</summary>
public sealed class ExamScore
{
    /// <summary>Primary key, assigned by the database on insert.</summary>
    public long Id { get; init; }

    /// <summary>Foreign key onto <see cref="Exam.Id" />, cascading on delete.</summary>
    public long ExamId { get; init; }

    /// <summary>Subject identifier, unique per exam so a subject can only be scored once.</summary>
    public string SubjectKey { get; init; } = string.Empty;

    /// <summary>Score achieved.</summary>
    public float Mark { get; init; }

    /// <summary>Highest achievable score this mark is measured against.</summary>
    public float FullMark { get; init; }

    /// <summary>Navigation to the owning exam when loaded through a relationship.</summary>
    public Exam? Exam { get; init; }
}