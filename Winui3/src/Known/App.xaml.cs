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
    private GuideWindow? _guide;

    /// <summary>Creates the application.</summary>
    public App()
    {
        CrashLog.Install(this);
        InitializeComponent();
    }

    /// <summary>Opens the shell once startup has finished.</summary>
    protected override async void OnLaunched(LaunchActivatedEventArgs args)
    {
        await AppServices.InitialiseAsync();

        _window = new MainWindow();
        _window.Activate();

        if (!AppServices.Settings.Current.IsFirstLogin) return;

        // A first run is asked its three questions by a window of its own, activated after the shell
        // so it lands in front. Keeping it out of the shell's frames means the wizard cannot disturb
        // the tab back stacks, and closing it is the only thing needed to get back to the dashboard.
        // The field is what keeps it alive for as long as it is on screen
        _guide = new GuideWindow();
        _guide.Closed += (_, _) => _guide = null;
        _guide.Activate();
    }
}