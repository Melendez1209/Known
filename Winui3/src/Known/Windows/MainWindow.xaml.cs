using Known.Core.Localization;
using Known.Pages;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known;

/// <summary>
///     The application shell.
/// </summary>
/// <remarks>
///     A <see cref="TitleBar" /> sits above a <see cref="NavigationView" />, which is the arrangement the
///     Windows App SDK recommends: the TitleBar control owns the drag region and the space reserved for
///     the caption buttons, while the NavigationView keeps one frame per top level tab so each tab
///     keeps its own back stack, matching the Android build.
/// </remarks>
public sealed partial class MainWindow : Window
{
    private readonly ShellNavigator _navigator = null!;
    private readonly StringLocalizer _strings = new();
    private readonly ThemeService _themeService;

    /// <summary>Creates the window, wires the shell up and opens the landing page.</summary>
    public MainWindow()
    {
        InitializeComponent();

        _themeService = AppServices.Theme;
        _themeService.ThemeChanged += OnThemeChanged;

        _navigator = new ShellNavigator(
            HomeFrame,
            HistoryFrame,
            AccountFrame,
            RootFrame,
            RootPageFor);

        _navigator.StateChanged += (_, _) => UpdateBackButton();
        _navigator.TabChanged += OnTabChanged;

        AppServices.Shell = _navigator;

        // Both of these only take effect from code. Setting ExtendsContentIntoTitleBar from XAML is
        // an error, and doing it late would flash the system title bar before replacing it
        ExtendsContentIntoTitleBar = true;
        SetTitleBar(AppTitleBar);

        DesktopShare.Initialise(this);

        ApplyCaptions();

        if (Navigation.MenuItems.FirstOrDefault() is NavigationViewItem firstItem) Navigation.SelectedItem = firstItem;

        // Stated explicitly rather than left to the selection change above, so the content area is
        // never left empty even if the event does not arrive
        _navigator.GoToTab(ShellTab.Home);

        Closed += OnClosed;
        Activated += OnWindowActivated;
        ApplyTheme();
    }

    /// <summary>
    ///     Coming back to the window is the moment the system theme can have changed underneath a
    ///     "follow system" preference, since nothing raised a notification while the window was idle.
    /// </summary>
    private void OnWindowActivated(object sender, WindowActivatedEventArgs args)
    {
        if (args.WindowActivationState == WindowActivationState.Deactivated) return;

        _themeService.Refresh();
    }

    /// <summary>The page a tab shows when its frame is empty.</summary>
    private static Type RootPageFor(ShellTab tab)
    {
        return tab switch
        {
            ShellTab.History => typeof(HistoryPage),
            ShellTab.Account => typeof(AccountPage),
            _ => typeof(HomePage)
        };
    }

    private void ApplyCaptions()
    {
        AppTitleBar.Title = _strings.Get("app_name");

        HomeItem.Content = _strings.Get("home");
        HistoryItem.Content = _strings.Get("history");
        AccountItem.Content = _strings.Get("account");
        AboutItem.Content = _strings.Get("about");

        // The pane's settings item is built by NavigationView itself and comes with the framework's
        // own caption, which would keep answering in the shell language after the student switches.
        // SettingsItem is typed object here, as opposed to the NavigationViewItem it really is
        if (Navigation.SettingsItem is NavigationViewItem settingsItem)
            settingsItem.Content = _strings.Get("settings");
    }

    private void OnThemeChanged(object? sender, EventArgs e)
    {
        ApplyTheme();
    }

    private void ApplyTheme()
    {
        if (Content is not FrameworkElement root) return;

        root.RequestedTheme = _themeService.ShouldUseDarkTheme
            ? ElementTheme.Dark
            : ElementTheme.Light;

        UpdateBackButton();
    }

    /// <summary>The pane toggle lives in the title bar, so the handler lives here too.</summary>
    private void OnPaneToggleRequested(TitleBar sender, object args)
    {
        Navigation.IsPaneOpen = !Navigation.IsPaneOpen;
    }

    /// <summary>
    ///     Unwinds the shared stack in preference to the active tab's, because its pages sit on top.
    ///     Running out of steps closes the window, which is what the system back gesture does on an
    ///     app that has nowhere left to go.
    /// </summary>
    private void OnNavigationBackRequested(NavigationView sender, NavigationViewBackRequestedEventArgs args)
    {
        if (!_navigator.GoBack()) Close();
    }

    /// <summary>
    ///     The pane toggle has no meaning once the NavigationView flattens its items into a horizontal
    ///     strip at the top, which it does on a narrow window.
    /// </summary>
    private void OnDisplayModeChanged(NavigationView sender, NavigationViewDisplayModeChangedEventArgs args)
    {
        AppTitleBar.IsPaneToggleButtonVisible =
            Navigation.PaneDisplayMode != NavigationViewPaneDisplayMode.Top;
    }

    /// <summary>
    ///     Routes a pane selection. Every tag is matched explicitly: a catch-all would quietly send
    ///     a misspelt or newly added item to the dashboard instead of showing that it is unhandled.
    /// </summary>
    private void OnTabSelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
    {
        // NavigationView contributes a settings item of its own, below the footer items and shown
        // without being asked for, because IsSettingsVisible is true by default. The control builds
        // it, so it belongs to neither collection and carries no Tag, which leaves the switch below
        // unable to see it. IsSettingsSelected is the flag raised for exactly that item, and the one
        // the WinUI Gallery sample tests; it is asked before the cast because the settings item is
        // not one of ours. Without it the pane offers a settings button that does nothing at all.
        if (args.IsSettingsSelected)
        {
            _navigator.NavigateRoot(typeof(SettingsPage));
            return;
        }

        if (args.SelectedItem is not NavigationViewItem item) return;

        switch (item.Tag as string)
        {
            case "History":
                _navigator.GoToTab(ShellTab.History);
                break;

            case "Account":
                _navigator.GoToTab(ShellTab.Account);
                break;

            case "Home":
                _navigator.GoToTab(ShellTab.Home);
                break;

            case "About":
                _navigator.NavigateRoot(typeof(AboutPage));
                break;
        }
    }

    /// <summary>
    ///     Keeps the pane highlighting the tab on screen. Revealing the shared stack over its owner
    ///     tab does not change the selection by itself, so the pane would otherwise still be showing
    ///     whichever footer item was last clicked.
    /// </summary>
    private void OnTabChanged(object? sender, ShellTab tab)
    {
        Navigation.SelectedItem = tab switch
        {
            ShellTab.History => HistoryItem,
            ShellTab.Account => AccountItem,
            _ => HomeItem
        };
    }

    /// <summary>
    ///     NavigationView draws the back button, so the shell only has to say whether there is
    ///     anywhere to go. That answer changes whenever a page navigates or pops, which is what
    ///     ShellNavigator.StateChanged reports, and whenever the theme moves the pane around.
    /// </summary>
    private void UpdateBackButton()
    {
        Navigation.IsBackEnabled = _navigator.CanGoBack;
    }

    private void OnClosed(object sender, WindowEventArgs args)
    {
        _themeService.ThemeChanged -= OnThemeChanged;

        AppServices.Shutdown();
    }
}
