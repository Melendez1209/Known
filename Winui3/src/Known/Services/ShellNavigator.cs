using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known.Services;

/// <summary>The three top level destinations shown in the navigation pane.</summary>
public enum ShellTab
{
    /// <summary>Dashboard with the trend and per subject charts.</summary>
    Home,

    /// <summary>The searchable list of every recorded exam.</summary>
    History,

    /// <summary>Account summary plus the way into the settings.</summary>
    Account
}

/// <summary>
///     Routes between the top level tabs and the pages pushed on top of them.
/// </summary>
/// <remarks>
///     Each tab owns its own <see cref="Frame" />, which is how the Android build kept one back stack
///     per top level route: switching tabs preserves where the student had navigated to, and returning
///     to a tab shows the page they left rather than its root. A fourth frame hosts the pages that
///     belong to no tab, such as onboarding and the settings.
/// </remarks>
public sealed class ShellNavigator
{
    private readonly Func<ShellTab, Type> _rootPageFor;
    private readonly Dictionary<ShellTab, Frame> _tabFrames;

    /// <summary>Creates the navigator over the frames declared by the main window.</summary>
    /// <param name="rootPageFor">
    ///     Supplies the page a tab shows when its frame is still empty. Without this a tab would only
    ///     ever reveal whatever the previous session left in it, and a first run would show an empty
    ///     pane behind the navigation bar.
    /// </param>
    public ShellNavigator(
        Frame homeFrame,
        Frame historyFrame,
        Frame accountFrame,
        Frame rootFrame,
        Func<ShellTab, Type> rootPageFor)
    {
        ArgumentNullException.ThrowIfNull(homeFrame);
        ArgumentNullException.ThrowIfNull(historyFrame);
        ArgumentNullException.ThrowIfNull(accountFrame);
        ArgumentNullException.ThrowIfNull(rootFrame);
        ArgumentNullException.ThrowIfNull(rootPageFor);

        _rootPageFor = rootPageFor;
        _tabFrames = new Dictionary<ShellTab, Frame>
        {
            [ShellTab.Home] = homeFrame,
            [ShellTab.History] = historyFrame,
            [ShellTab.Account] = accountFrame
        };

        RootFrame = rootFrame;
        CurrentTab = ShellTab.Home;

        ShowTabFrame(ShellTab.Home);
    }

    /// <summary>Holds the pages that belong to no tab, for example onboarding and the settings.</summary>
    public Frame RootFrame { get; }

    /// <summary>The tab currently on screen.</summary>
    public ShellTab CurrentTab { get; private set; }

    /// <summary>The frame of the tab currently on screen.</summary>
    public Frame ActiveTabFrame => _tabFrames[CurrentTab];

    /// <summary>Whether the student can navigate further back from the current page.</summary>
    public bool CanGoBack => RootFrame.CanGoBack || ActiveTabFrame.CanGoBack;

    /// <summary>Raised after the visible tab changed.</summary>
    public event EventHandler<ShellTab>? TabChanged;

    /// <summary>
    ///     Raised whenever anything that could change whether a back step is available has happened.
    ///     Pages navigate and pop through this class without going through the window, so without this
    ///     the back button would keep a stale enabled state after a save or a completed wizard.
    /// </summary>
    public event EventHandler? StateChanged;

    /// <summary>Shows a tab, restoring its previous position in its own back stack.</summary>
    public void GoToTab(ShellTab tab)
    {
        if (!Enum.IsDefined(tab)) throw new ArgumentOutOfRangeException(nameof(tab), tab, "Unknown shell tab.");

        // Done before the early return so reselecting the current tab still fills an empty frame
        EnsureRootPage(tab);

        if (tab == CurrentTab) return;

        ShowTabFrame(tab);

        CurrentTab = tab;
        TabChanged?.Invoke(this, tab);
        StateChanged?.Invoke(this, EventArgs.Empty);
    }

    /// <summary>
    ///     Loads the tab's root page when its frame has nothing in it, which is the case on a first run
    ///     and after the frame is reset.
    /// </summary>
    private void EnsureRootPage(ShellTab tab)
    {
        var frame = _tabFrames[tab];

        if (frame.Content is null) frame.Navigate(_rootPageFor(tab));
    }

    /// <summary>Pushes a page onto the current tab's stack.</summary>
    public bool NavigateInTab(Type pageType, object? parameter = null)
    {
        ArgumentNullException.ThrowIfNull(pageType);

        var navigated = ActiveTabFrame.Navigate(pageType, parameter);

        StateChanged?.Invoke(this, EventArgs.Empty);

        return navigated;
    }

    /// <summary>Pushes a page onto the shared stack, showing it above whichever tab is selected.</summary>
    public bool NavigateRoot(Type pageType, object? parameter = null)
    {
        ArgumentNullException.ThrowIfNull(pageType);

        RootFrame.Navigate(pageType, parameter);

        StateChanged?.Invoke(this, EventArgs.Empty);

        return true;
    }

    /// <summary>
    ///     Goes back one step, preferring the shared stack because its pages sit visually on top.
    ///     Returns false when nothing is left to unwind, which tells the window to let the shell handle
    ///     the back gesture itself.
    /// </summary>
    public bool GoBack()
    {
        if (!CanGoBack) return false;

        if (RootFrame.CanGoBack)
            RootFrame.GoBack();
        else
            ActiveTabFrame.GoBack();

        StateChanged?.Invoke(this, EventArgs.Empty);

        return true;
    }

    /// <summary>
    ///     Returns the current tab to its root page, matching the Android behaviour where leaving a
    ///     nested tab with the system back gesture dropped back to Home first.
    /// </summary>
    public void ResetActiveTab()
    {
        var frame = ActiveTabFrame;

        while (frame.CanGoBack) frame.GoBack();

        StateChanged?.Invoke(this, EventArgs.Empty);
    }

    private void ShowTabFrame(ShellTab tab)
    {
        foreach (var pair in _tabFrames)
            pair.Value.Visibility = pair.Key == tab
                ? Visibility.Visible
                : Visibility.Collapsed;
    }
}