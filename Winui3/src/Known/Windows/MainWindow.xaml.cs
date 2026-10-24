using Known.Core.Localization;
using Known.Pages;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Windows;

/// <summary>
///     The application shell.
/// </summary>
/// <remarks>
///     A <see cref="TitleBar" /> sits above a <see cref="NavigationView" />, which is the arrangement the
///     Windows App SDK recommends: the TitleBar owns the drag region, the caption buttons and the
///     back button, while the NavigationView drives a single <see cref="Frame" /> for all pages.
///     This matches the WinUI Notes sample and the Fluent Design navigation guidance.
/// </remarks>
public sealed partial class MainWindow
{
    private readonly StringLocalizer _strings = new();
    private readonly ThemeService _themeService;

    /// <summary>Creates the window, wires the shell up and opens the landing page.</summary>
    public MainWindow()
    {
        InitializeComponent();

        _themeService = AppServices.Theme;
        _themeService.ThemeChanged += OnThemeChanged;

        AppTitleBar.Title = _strings.Get("app_name");

        // Both of these only take effect from code. Setting ExtendsContentIntoTitleBar from XAML is
        // an error, and doing it late would flash the system title bar before replacing it
        ExtendsContentIntoTitleBar = true;
        SetTitleBar(AppTitleBar);

        DesktopShare.Initialise(this);

        ApplyCaptions();

        // Start on the dashboard. The navigation item below is selected to match.
        MainFrame.Navigate(typeof(HomePage));

        if (Navigation.MenuItems.FirstOrDefault() is NavigationViewItem firstItem) Navigation.SelectedItem = firstItem;

        // Keep the NavigationView selection in step with the frame, including when the student
        // taps the back button and the frame unwinds to a previous page.
        MainFrame.Navigated += OnMainFrameNavigated;

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

    private void ApplyCaptions()
    {
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
    }

    /// <summary>The pane toggle lives in the title bar, so the handler lives here too.</summary>
    private void OnPaneToggleRequested(TitleBar sender, object args)
    {
        Navigation.IsPaneOpen = !Navigation.IsPaneOpen;
    }

    /// <summary>
    ///     Handles the back button in the TitleBar. Unwinds the main frame; when there is nowhere
    ///     left to go the window closes, which is what the system back gesture does on an app that
    ///     has nowhere left to go.
    /// </summary>
    private void OnBackRequested(TitleBar sender, object args)
    {
        if (MainFrame.CanGoBack)
            MainFrame.GoBack();
        else
            Close();
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
            MainFrame.Navigate(typeof(SettingsPage));
            return;
        }

        if (args.SelectedItem is not NavigationViewItem item) return;

        switch (item.Tag as string)
        {
            case "History":
                MainFrame.Navigate(typeof(HistoryPage));
                break;

            case "Account":
                MainFrame.Navigate(typeof(AccountPage));
                break;

            case "Home":
                MainFrame.Navigate(typeof(HomePage));
                break;

            case "About":
                MainFrame.Navigate(typeof(AboutPage));
                break;
        }
    }

    /// <summary>
    ///     Keeps the NavigationView selection in step with the frame, including when the student
    ///     taps the back button and the frame unwinds to a previous page. Child pages such as
    ///     DetailPage or InputtingPage keep the current selection because they belong to the tab
    ///     that opened them.
    /// </summary>
    private void OnMainFrameNavigated(object sender, NavigationEventArgs e)
    {
        var page = MainFrame.Content;

        Navigation.SelectedItem = page switch
        {
            HomePage => HomeItem,
            HistoryPage => HistoryItem,
            AccountPage => AccountItem,
            AboutPage => AboutItem,
            SettingsPage when Navigation.SettingsItem is NavigationViewItem settingsItem => settingsItem,
            _ => Navigation.SelectedItem
        };
    }

    private void OnClosed(object sender, WindowEventArgs args)
    {
        _themeService.ThemeChanged -= OnThemeChanged;

        AppServices.Shutdown();
    }
}