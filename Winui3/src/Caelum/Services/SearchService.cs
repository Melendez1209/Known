namespace Caelum.Services;

/// <summary>
///     The exam search query typed into the shell search box. The box lives in the title bar so
///     it is reachable from every page, and the history page reads the query from here whenever
///     it loads or the text changes.
/// </summary>
public sealed class SearchService
{
    private string _query = string.Empty;

    /// <summary>The stored query, trimmed; empty matches every exam.</summary>
    public string Query => _query;

    /// <summary>Raised when the query changes, so the history page can refilter.</summary>
    public event EventHandler? QueryChanged;

    /// <summary>Stores a new query and raises <see cref="QueryChanged" /> when it differs.</summary>
    public void SetQuery(string query)
    {
        query = query.Trim();

        if (string.Equals(query, _query, StringComparison.Ordinal)) return;

        _query = query;
        QueryChanged?.Invoke(this, EventArgs.Empty);
    }
}
