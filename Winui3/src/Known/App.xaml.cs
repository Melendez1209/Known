using Known.Services;
using Microsoft.UI.Xaml;

namespace Known;

/// <summary>
///     Process wide entry point. Owns the startup sequence, because a WinUI 3 window cannot be shown
///     until the asynchronous database work has finished.
/// </summary>
public partial class App
{
    private Window? _window;
    private Windows.GuideWindow? _guide;

    /// <summary>Creates the application.</summary>
    public App()
    {
        CrashLog.Install(this);
        InitializeComponent();
    }

    /// <summary>Opens whichever window the stored state calls for.</summary>
    protected override async void OnLaunched(LaunchActivatedEventArgs args)
    {
        await AppServices.InitialiseAsync();

        // A first run is asked its three questions by a window of its own, and the shell is not built
        // until that window closes. Showing both at once would put an empty dashboard behind the
        // wizard, which reads as a second question rather than as the app being ready
        if (AppServices.Settings.Current.IsFirstLogin)
        {
            await RunOnboardingAsync();
            return;
        }

        OpenShell();
    }

    /// <summary>
    ///     Shows the onboarding wizard and waits for it to go away, whether the student finished it or
    ///     dismissed it. Either way the wizard has cleared the first login flag, so the shell opens
    ///     straight into the dashboard.
    /// </summary>
    private async Task RunOnboardingAsync()
    {
        // The field is what keeps the wizard alive for as long as it is on screen
        _guide = new Windows.GuideWindow();

        _guide.Activate();

        // Dismissed rather than Closed: the wizard writes the first login flag as it goes, and the
        // shell must not start until that write has landed
        await _guide.Dismissed;

        _guide = null;

        OpenShell();
    }

    /// <summary>Creates and shows the main window.</summary>
    private void OpenShell()
    {
        _window = new Windows.MainWindow();
        _window.Activate();
    }
}