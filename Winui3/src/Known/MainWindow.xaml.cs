using Known.Core.Localization;
using Known.Pages;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known;

/// <summary>
/// The application shell.
/// </summary>
/// <remarks>
/// A <see cref="TitleBar"/> sits above a <see cref="NavigationView"/>, which is the arrangement the
/// Windows App SDK recommends: the TitleBar control owns the drag region and the space reserved for
/// the caption buttons, while the NavigationView keeps one frame per top level tab so each tab
/// keeps its own back stack, matching the Android build.
/// </remarks>
public sealed partial class MainWindow : Window
{
    private readonly ThemeService _themeService;
    private readonly StringLocalizer _strings = new();

    private ShellNavigator _navigator = null!;

    /// <summary>Creates the window, wires the shell up and opens the landing page.</summary>
    public MainWindow()
    {
        InitializeComponent();

        _themeService = AppServices.Theme;
        _themeService.ThemeChanged += OnThemeChanged;

        _navigator = new ShellNavigator(HomeFrame, HistoryFrame, AccountFrame, RootFrame);
        _navigator.RootFrame.Navigated += OnRootFrameNavigated;

        AppServices.Shell = _navigator;

        // Both of these only take effect from code. Setting ExtendsContentIntoTitleBar from XAML is
        // an error, and doing it late would flash the system title bar before replacing it
        ExtendsContentIntoTitleBar = true;
        SetTitleBar(AppTitleBar);

        ApplyCaptions();

        if (Navigation.MenuItems.FirstOrDefault() is NavigationViewItem firstItem)
        {
            Navigation.SelectedItem = firstItem;
        }

        if (AppServices.Settings.Current.IsFirstLogin)
        {
            _navigator.NavigateRoot(typeof(GuidePage));
        }

        Closed += OnClosed;
        Activated += OnWindowActivated;
        ApplyTheme();
    }

    /// <summary>
    /// Coming back to the window is the moment the system theme can have changed underneath a
    /// "follow system" preference, since nothing raised a notification while the window was idle.
    /// </summary>
    private void OnWindowActivated(object sender, WindowActivatedEventArgs args)
    {
        if (args.WindowActivationState == WindowActivationState.Deactivated)
        {
            return;
        }

        _themeService.Refresh();
    }

    private void ApplyCaptions()
    {
        AppTitleBar.Title = _strings.Get("app_name");

        HomeItem.Content = _strings.Get("home");
        HistoryItem.Content = _strings.Get("history");
        AccountItem.Content = _strings.Get("me");
    }

    private void OnThemeChanged(object? sender, EventArgs e) => ApplyTheme();

    private void ApplyTheme()
    {
        if (Content is not FrameworkElement root)
        {
            return;
        }

        root.RequestedTheme = _themeService.ShouldUseDarkTheme
            ? ElementTheme.Dark
            : ElementTheme.Light;

        UpdateBackButton();
    }

    /// <summary>The pane toggle lives in the title bar, so the handler lives here too.</summary>
    private void OnPaneToggleRequested(TitleBar sender, object args) =>
        Navigation.IsPaneOpen = !Navigation.IsPaneOpen;

    private void OnTitleBarBackRequested(TitleBar sender, object args)
    {
        if (!_navigator.GoBack())
        {
            Close();
        }

        UpdateBackButton();
    }

    /// <summary>
    /// The pane toggle has no meaning once the NavigationView flattens its items into a horizontal
    /// strip at the top, which it does on a narrow window.
    /// </summary>
    private void OnDisplayModeChanged(NavigationView sender, NavigationViewDisplayModeChangedEventArgs args) =>
        AppTitleBar.IsPaneToggleButtonVisible =
            Navigation.PaneDisplayMode != NavigationViewPaneDisplayMode.Top;

    private void OnTabSelectionChanged(NavigationView sender, NavigationViewSelectionChangedEventArgs args)
    {
        if (args.SelectedItem is not NavigationViewItem item)
        {
            return;
        }

        var tab = (item.Tag as string) switch
        {
            "History" => ShellTab.History,
            "Account" => ShellTab.Account,
            _ => ShellTab.Home
        };

        _navigator.GoToTab(tab);
        UpdateBackButton();
    }

    private void OnRootFrameNavigated(object sender, NavigationEventArgs e) => UpdateBackButton();

    private void UpdateBackButton() => AppTitleBar.IsBackButtonEnabled = _navigator.CanGoBack;

    private void OnClosed(object sender, WindowEventArgs args)
    {
        _themeService.ThemeChanged -= OnThemeChanged;

        AppServices.Shutdown();
    }
}
