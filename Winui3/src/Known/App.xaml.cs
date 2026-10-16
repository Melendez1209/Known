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
    }
}