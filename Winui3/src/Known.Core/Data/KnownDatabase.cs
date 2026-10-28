using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;

namespace Known.Core.Data;

/// <summary>
///     Locates and opens the SQLite database. Kept out of the repositories so the desktop shell can
///     choose where the file lives while the tests point the same code at an in-memory database.
/// </summary>
public static class KnownDatabase
{
    /// <summary>File name of the database inside the data directory.</summary>
    private const string DatabaseFileName = "known.db";

    /// <summary>Folder the application keeps its local data in, under the per-user local app data root.</summary>
    public static string ResolveDataDirectory()
    {
        return Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Known");
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
    public static KnownDbContext CreateContext()
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
    public static KnownDbContext CreateContextFor(string connectionString)
    {
        ArgumentException.ThrowIfNullOrWhiteSpace(connectionString);

        var options = new DbContextOptionsBuilder<KnownDbContext>()
            .UseSqlite(connectionString)
            .Options;

        return new KnownDbContext(options);
    }

    /// <summary>
    ///     Creates the schema when needed and makes sure the settings row exists, so the rest
    ///     of the app never has to special case a first run.
    /// </summary>
    public static async Task InitialiseAsync(CancellationToken cancellationToken = default)
    {
        await using var context = CreateContext();

        // No migration step yet: the schema is still at v1 and the app has not shipped, so a fresh
        // file is all a new installation needs. Existing databases are the developer's own, and are
        // deleted by hand rather than upgraded.
        await context.Database.EnsureCreatedAsync(cancellationToken).ConfigureAwait(false);

        var settingsRepository = new SettingsRepository(context);
        await settingsRepository.InitialiseAsync(cancellationToken).ConfigureAwait(false);
    }
}