using Known.Core.Services;
using Known.Services;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

/// <summary>
///     Base class for every page in the app. It exposes the shared services and gives pages a single
///     place to release work such as event subscriptions when they are navigated away from.
/// </summary>
public abstract class KnownPage : Page
{
    /// <summary>Creates the page and captures the shared services.</summary>
    protected KnownPage()
    {
        Exams = AppServices.Exams;
        Settings = AppServices.Settings;
    }

    /// <summary>The observable exam store the list and chart pages bind to.</summary>
    public ExamService Exams { get; }

    /// <summary>The cached settings.</summary>
    public SettingsService Settings { get; }

    /// <summary>
    ///     The parameter this page was navigated with, or null. WinUI 3 has no
    ///     <see cref="Page.Parameter" />, so the argument is captured from the navigation event instead.
    /// </summary>
    protected object? NavigationParameter { get; private set; }

    /// <summary>The shell, once the main window has created its frames.</summary>
    protected ShellNavigator Shell =>
        AppServices.Shell ?? throw new InvalidOperationException("The shell is not available yet.");

    /// <summary>
    ///     Called when the page is no longer the visible one. Overrides should unsubscribe anything
    ///     they subscribed to in <see cref="OnPageLoaded" />.
    /// </summary>
    public virtual void OnNavigatedAway()
    {
    }

    /// <summary>
    ///     Called once the page is loaded and its navigation parameters are available. Overrides
    ///     should start loading data here rather than in the constructor, which runs before
    ///     navigation has completed.
    /// </summary>
    protected virtual void OnPageLoaded()
    {
    }

    /// <inheritdoc />
    protected override void OnNavigatedTo(NavigationEventArgs e)
    {
        NavigationParameter = e.Parameter;

        base.OnNavigatedTo(e);
        OnPageLoaded();
    }
}