namespace Known.Core.Data;

/// <summary>One recorded exam. Deleting it cascades to every <see cref="ExamScore" /> of that exam.</summary>
public sealed class Exam
{
    /// <summary>Primary key, assigned by the database on insert.</summary>
    public long Id { get; set; }

    /// <summary>Display name, by default the start date as <c>yyyy-MM-dd</c>.</summary>
    public string Name { get; set; } = string.Empty;

    /// <summary>First day of the exam, as epoch milliseconds.</summary>
    public long StartDate { get; set; }

    /// <summary>Last day of the exam, as epoch milliseconds.</summary>
    public long EndDate { get; set; }

    /// <summary>Insertion timestamp in epoch milliseconds, used to break ties on import.</summary>
    public long CreatedAt { get; init; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

    /// <summary>Whether the student pinned this exam to their favourites.</summary>
    public bool IsFavorite { get; set; }

    /// <summary>Navigation to the scores of this exam when loaded through a relationship.</summary>
    public List<ExamScore> Scores { get; init; } = [];
}