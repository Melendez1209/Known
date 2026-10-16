namespace Known.Core.Domain;

/// <summary>
/// One editable row of the input screen: a subject key, whether the row is ticked, and its two
/// text fields. Rows that are not ticked are never persisted.
/// </summary>
/// <param name="SubjectKey">Stable subject identifier, see <see cref="SubjectCatalogue"/>.</param>
/// <param name="Checked">Whether the row takes part in the saved exam.</param>
/// <param name="Mark">The score as typed, kept as text so half-points can be entered naturally.</param>
/// <param name="FullMark">The denominator as typed, keeping the same text discipline as the mark.</param>
public sealed record ScoreInput(
    string SubjectKey,
    bool Checked = true,
    string Mark = "",
    string FullMark = SubjectCatalogue.DefaultFullMark);

/// <summary>Why a set of input rows cannot be saved yet.</summary>
public enum ScoreValidation
{
    /// <summary>Every ticked row is usable and at least one mark has been typed.</summary>
    Valid,

    /// <summary>Nothing has been typed into any ticked row.</summary>
    NoMarkEntered,

    /// <summary>A row carrying a mark that is unparsable or greater than its full mark.</summary>
    MarkExceedsFull
}
