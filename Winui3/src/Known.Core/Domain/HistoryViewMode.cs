namespace Known.Core.Domain;

/// <summary>
///     Information density of the history list. Stored in the settings row, so the choice survives a
///     restart and the numeric values are part of the stored format.
/// </summary>
public enum HistoryViewMode
{
    /// <summary>Exam name and total mark only, in one tight row.</summary>
    Compact = 0,

    /// <summary>
    ///     A two line card with the date, the mark against the full mark and the favourite state.
    ///     The first run default.
    /// </summary>
    Standard = 1,

    /// <summary>The plain table like row of name, date and mark.</summary>
    List = 2
}
