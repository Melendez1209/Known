using Microsoft.UI.Xaml;

namespace Known.Services;

/// <summary>
/// Writes an unhandled exception to a log next to the database.
/// </summary>
/// <remarks>
/// A managed exception escaping through the XAML framework takes the whole process down and leaves
/// only a native status code in the Windows event log, with no managed stack trace. Without this
/// file the only way to find out what went wrong is to attach a debugger, so every startup crash is
/// appended here in full.
/// </remarks>
public static class CrashLog
{
    /// <summary>File the crashes are appended to, inside the application's data directory.</summary>
    public static string Path { get; } = System.IO.Path.Combine(
        Core.Data.KnownDatabase.ResolveDataDirectory(),
        "crash.log");

    /// <summary>
    /// Registers the handlers against the running application. Call once, at the very start of the
    /// application constructor.
    /// </summary>
    public static void Install(Application application)
    {
        ArgumentNullException.ThrowIfNull(application);

        // UnhandledException is an instance event, so the application that owns it has to be passed
        // in; reading Application.Current would not work, because it is not set yet this early
        application.UnhandledException += OnUiUnhandled;

        AppDomain.CurrentDomain.UnhandledException += OnDomainUnhandled;
    }

    private static void OnDomainUnhandled(object sender, System.UnhandledExceptionEventArgs e)
    {
        if (e.ExceptionObject is Exception exception)
        {
            Write(exception);
        }
    }

    private static void OnUiUnhandled(object sender, Microsoft.UI.Xaml.UnhandledExceptionEventArgs e)
    {
        Write(e.Exception);

        // Deliberately not marked handled: a page that failed to initialise would otherwise leave a
        // half built window behind, and a logged crash is easier to act on than a broken shell
    }

    private static void Write(Exception exception)
    {
        try
        {
            var directory = System.IO.Path.GetDirectoryName(Path);

            if (!string.IsNullOrEmpty(directory))
            {
                Directory.CreateDirectory(directory);
            }

            File.AppendAllText(
                Path,
                $"===== {DateTimeOffset.Now:yyyy-MM-dd HH:mm:ss.fff} ====={Environment.NewLine}" +
                $"{exception}{Environment.NewLine}{Environment.NewLine}");
        }
        catch (Exception)
        {
            // Logging must never be the reason the process dies, so any failure here is swallowed
        }
    }
}
