using Known.Core.Data;
using Known.Core.Services;
using Known.Services;

namespace Known;

/// <summary>
///     Owns the application's long lived services. A WinUI 3 window has no built in container, and
///     the app only ever needs one database context for the whole process, so a small lazily
///     initialised holder is simpler than pulling a dependency injection framework in for it.
/// </summary>
public static class AppServices
{
    private static readonly SemaphoreSlim StartupGate = new(1, 1);

    private static KnownDbContext? _context;
    private static IExamRepository? _examRepository;
    private static ISettingsRepository? _settingsRepository;

    /// <summary>The shared context. Valid only after <see cref="InitialiseAsync" /> has completed.</summary>
    public static KnownDbContext Context =>
        _context ?? throw new InvalidOperationException(
            $"{nameof(AppServices)} has not been initialised yet.");

    /// <summary>Exam storage and queries.</summary>
    public static IExamRepository ExamRepository =>
        _examRepository ?? throw new InvalidOperationException(
            $"{nameof(AppServices)} has not been initialised yet.");

    /// <summary>The single settings row.</summary>
    public static ISettingsRepository SettingsRepository =>
        _settingsRepository ?? throw new InvalidOperationException(
            $"{nameof(AppServices)} has not been initialised yet.");

    /// <summary>The cached settings every page reads its preferences from.</summary>
    public static SettingsService Settings { get; private set; } = null!;

    /// <summary>The observable exam store the list and chart pages bind to.</summary>
    public static ExamService Exams { get; private set; } = null!;

    /// <summary>Resolves the stored preference into concrete theme values.</summary>
    public static ThemeService Theme { get; private set; } = null!;

    /// <summary>
    ///     Turns the device position into a city name, for the region field. Stateless and cheap to
    ///     share, so one instance serves both the wizard and the settings page.
    /// </summary>
    public static LocationService Location { get; } = new();

    /// <summary>Microsoft account sign in.</summary>
    public static MsalAuthService Auth { get; } = new();

    /// <summary>
    ///     Opens the database, creates the schema on first run and makes sure the settings row exists,
    ///     then builds the services that sit on top of it. Repeated calls wait on the gate and return
    ///     immediately once the first one has finished.
    /// </summary>
    public static async Task InitialiseAsync(CancellationToken cancellationToken = default)
    {
        await StartupGate.WaitAsync(cancellationToken).ConfigureAwait(true);

        try
        {
            if (_context is not null) return;

            var context = KnownDatabase.CreateContext();

            await context.Database.EnsureCreatedAsync(cancellationToken).ConfigureAwait(true);

            var settingsRepository = new SettingsRepository(context);
            var settings = new SettingsService(settingsRepository);
            var exams = new ExamService(new ExamRepository(context));

            await settings.LoadAsync(cancellationToken).ConfigureAwait(true);
            await exams.LoadAsync(cancellationToken).ConfigureAwait(true);

            _context = context;
            _settingsRepository = settingsRepository;
            _examRepository = exams.Repository;
            Settings = settings;
            Exams = exams;
            Theme = new ThemeService(settings);
        }
        finally
        {
            StartupGate.Release();
        }
    }

    /// <summary>
    ///     Releases the database context. Called when the last window closes so the SQLite file handle
    ///     is not left open while the process lingers during shutdown.
    /// </summary>
    public static void Shutdown()
    {
        _context?.Dispose();

        _context = null;
        _examRepository = null;
        _settingsRepository = null;
    }
}