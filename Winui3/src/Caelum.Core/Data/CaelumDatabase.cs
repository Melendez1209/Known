using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace Caelum.Core.Data;

/// <summary>
///     Locates and opens the SQLite database. Kept out of the repositories so the desktop shell can
///     choose where the file lives while the tests point the same code at an in-memory database.
/// </summary>
public static class CaelumDatabase
{
    /// <summary>File name of the database inside the data directory.</summary>
    private const string DatabaseFileName = "caelum.db";

    /// <summary>Folder the application keeps its local data in, under the per-user local app data root.</summary>
    public static string ResolveDataDirectory()
    {
        return Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Caelum");
    }

    /// <summary>Full path of the database file, without creating it.</summary>
    public static string ResolveDatabasePath()
    {
        return Path.Combine(ResolveDataDirectory(), DatabaseFileName);
    }

    /// <summary>
    ///     Opens a context against the on-disk database, creating the folder when needed. Callers own
    ///     the returned context and should dispose it.
    /// </summary>
    /// <remarks>
    ///     No migration step exists yet: the schema is still at v1 and the app has not shipped, so a
    ///     fresh file is all a new installation needs. Existing databases are the developer's own,
    ///     and are deleted by hand rather than upgraded.
    /// </remarks>
    public static CaelumDbContext CreateContext()
    {
        var directory = ResolveDataDirectory();
        Directory.CreateDirectory(directory);

        return CreateContextFor(new SqliteConnectionStringBuilder
        {
            DataSource = Path.Combine(directory, DatabaseFileName),
            Mode = SqliteOpenMode.ReadWriteCreate,
            Cache = SqliteCacheMode.Shared
        }.ToString());
    }

    /// <summary>Opens a context against an arbitrary connection string, mainly for tests.</summary>
    public static CaelumDbContext CreateContextFor(string connectionString)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(connectionString);

        var options = new DbContextOptionsBuilder<CaelumDbContext>()
            .UseSqlite(connectionString)
            .Options;

        return new CaelumDbContext(options);
    }
}