using System.Collections.ObjectModel;
using Caelum.Core.Data;
using Caelum.Core.Domain;
using Caelum.Core.Localization;
using Caelum.Services;
using Microsoft.UI.Input;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Automation;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Controls.Primitives;
using Microsoft.UI.Xaml.Input;
using Microsoft.UI.Xaml.Media;
using Microsoft.UI.Xaml.Navigation;
using Windows.Foundation;
using Windows.System;

namespace Caelum.Pages;

/// <summary>
///     The exam history: a searchable, favourite filterable, multi selectable list of every recorded
///     exam, rendered at one of three view densities. Clicking a row opens its detail, and ticking
///     rows swaps the page footer for an action bar, matching the way the Android build swapped its
///     navigation bar for one.
/// </summary>
public sealed partial class HistoryPage
{
    private readonly StringLocalizer _strings = new();
    private readonly ObservableCollection<ExamWithTotal> _visible = [];

    /// <summary>The density the list currently renders at; restored from the settings row.</summary>
    private HistoryViewMode _viewMode = HistoryViewMode.Standard;

    /// <summary>Guards the view mode box while the stored choice is being restored into it.</summary>
    private bool _restoringViewMode;

    /// <summary>True while a marquee drag is running; pointer moves then drive the rectangle.</summary>
    private bool _marqueeActive;

    /// <summary>True when the marquee was started with Ctrl held, so it adds to the selection.</summary>
    private bool _marqueeAdditive;

    /// <summary>The point the drag started from, in list coordinates.</summary>
    private Point _marqueeOrigin;

    /// <summary>The selection as it stood when the marquee started, kept for Ctrl additions.</summary>
    private List<object>? _marqueeBase;

    /// <summary>The set the last move applied, so the selection is only rewritten when it changes.</summary>
    private List<object> _marqueeLast = [];

    /// <summary>The list's own ScrollViewer, found once and cached for the auto scroll.</summary>
    private ScrollViewer? _listScrollViewer;

    /// <summary>Creates the page and fills the list from the shared exam store.</summary>
    public HistoryPage()
    {
        InitializeComponent();

        EmptyText.Text = _strings.Get("no_history");
        AddButton.Content = _strings.Get("add");
        DeleteSelectionButton.Label = _strings.Get("delete");
        FavouriteSelectionButton.Label = _strings.Get("add_favourite");
        ShareSelectionButton.Label = _strings.Get("share");
        ShareSummaryItem.Text = _strings.Get("share_summary");
        ShareDetailsItem.Text = _strings.Get("share_details");

        // Escape cancels a running marquee. An accelerator, not the list's KeyDown, because mid
        // drag the keyboard focus can sit anywhere in the page and the event would never reach
        // the list
        var escapeAccelerator = new KeyboardAccelerator { Key = VirtualKey.Escape };
        escapeAccelerator.Invoked += OnEscapeInvoked;
        this.KeyboardAccelerators.Add(escapeAccelerator);

        BuildFavouriteFilter();
        BuildViewModeFilter();

        ExamList.ItemsSource = _visible;

        AppServices.Exams.Reloaded += OnExamsReloaded;
        AppServices.Search.QueryChanged += OnSearchQueryChanged;

        Reload();
    }

    /// <inheritdoc />
    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
        AppServices.Exams.Reloaded -= OnExamsReloaded;
        AppServices.Search.QueryChanged -= OnSearchQueryChanged;

        base.OnNavigatedFrom(e);
    }

    private void OnExamsReloaded(object? sender, EventArgs e)
    {
        Reload();

        // Refreshing the rows normally drops the selection, which would clear the star by way of the
        // selection changing. Asking for it here as well keeps it right whichever way the list behaves.
        ApplyFavouriteState();
    }

    /// <summary>Rebuilds the row list, keeping whatever search term is currently typed.</summary>
    private void Reload()
    {
        ApplyFilter(AppServices.Search.Query);
    }

    private void ApplyFilter(string? query)
    {
        // Unchecked shows every exam, checked only the favourites
        var matches = FavouriteFilter.IsChecked == true
            ? AppServices.Exams.Exams.Where(exam => exam.Exam.IsFavorite)
            : AppServices.Exams.Exams.AsEnumerable();

        if (!string.IsNullOrWhiteSpace(query))
            matches = matches.Where(exam => exam.Exam.Name.Contains(query, StringComparison.CurrentCultureIgnoreCase));

        _visible.Clear();

        foreach (var exam in matches) _visible.Add(exam);

        EmptyText.Visibility = _visible.Count == 0 ? Visibility.Visible : Visibility.Collapsed;
        EmptyText.Text = string.IsNullOrWhiteSpace(query)
            ? _strings.Get("no_history")
            : _strings.Get("no_search_result");
    }

    /// <summary>Refilters when the query in the shell search box changes.</summary>
    private void OnSearchQueryChanged(object? sender, EventArgs e)
    {
        ApplyFilter(AppServices.Search.Query);
    }

    /// <summary>Labels the favourite toggle; unchecked means every exam, checked only favourites.</summary>
    private void BuildFavouriteFilter()
    {
        FavouriteFilterText.Text = _strings.Get("favourites");
    }

    private void OnFavouriteFilterChanged(object sender, RoutedEventArgs e)
    {
        ApplyFilter(AppServices.Search.Query);
    }

    /// <summary>
    ///     Labels the view switcher items and restores whichever view was stored last time. The
    ///     icons are the same three the Windows settings app uses for its densities.
    /// </summary>
    private void BuildViewModeFilter()
    {
        ToolTipService.SetToolTip(ListViewModeItem, _strings.Get("list_view"));
        ToolTipService.SetToolTip(GridViewModeItem, _strings.Get("grid_view"));
        ToolTipService.SetToolTip(TileViewModeItem, _strings.Get("tile_view"));

        AutomationProperties.SetName(ListViewModeItem, _strings.Get("list_view"));
        AutomationProperties.SetName(GridViewModeItem, _strings.Get("grid_view"));
        AutomationProperties.SetName(TileViewModeItem, _strings.Get("tile_view"));

        _viewMode = AppServices.Settings.Current.HistoryViewMode;

        _restoringViewMode = true;
        ViewModeSelector.SelectedItem = _viewMode switch
        {
            HistoryViewMode.List => ListViewModeItem,
            HistoryViewMode.Compact => GridViewModeItem,
            _ => TileViewModeItem
        };
        _restoringViewMode = false;

        ApplyViewMode();
    }

    private async void OnViewModeChanged(object sender, SelectorBarSelectionChangedEventArgs e)
    {
        if (_restoringViewMode) return;

        // The switcher order is list, grid, tile, which is not the order of the stored enum
        var mode = ViewModeSelector.SelectedItem switch
        {
            _ when ReferenceEquals(ViewModeSelector.SelectedItem, ListViewModeItem) => HistoryViewMode.List,
            _ when ReferenceEquals(ViewModeSelector.SelectedItem, GridViewModeItem) => HistoryViewMode.Compact,
            _ => HistoryViewMode.Standard
        };

        if (mode == _viewMode) return;

        _viewMode = mode;
        ApplyViewMode();

        await AppServices.Settings.SetHistoryViewModeAsync(mode);
    }

    /// <summary>
    ///     Assigns the row template, the layout panel and the container style that match the
    ///     stored density: a wrapping grid for the two card densities and the plain stack for
    ///     the list. The card densities drop the container padding so the card is exactly the
    ///     clickable cell; the list keeps the default padding that gives its rows an inset.
    /// </summary>
    private void ApplyViewMode()
    {
        switch (_viewMode)
        {
            case HistoryViewMode.Compact:
                ExamList.ItemTemplate = (DataTemplate)Resources["CompactRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["CompactGridPanel"];
                ExamList.ItemContainerStyle = (Style)Resources["CardItemStyle"];
                break;

            case HistoryViewMode.List:
                ExamList.ItemTemplate = (DataTemplate)Resources["ListRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["ListPanel"];
                ExamList.ItemContainerStyle = null;
                break;

            default:
                ExamList.ItemTemplate = (DataTemplate)Resources["StandardRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["StandardGridPanel"];
                ExamList.ItemContainerStyle = (Style)Resources["CardItemStyle"];
                break;
        }
    }

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        // Ticking a row is how multi select starts, so a click only opens the detail while nothing
        // is selected yet
        if (ExamList.SelectedItems.Count > 0) return;

        if (e.ClickedItem is ExamWithTotal exam) this.Frame.Navigate(typeof(DetailPage), exam.Exam.Id);
    }

    private void OnSelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        var count = ExamList.SelectedItems.Count;

        SelectionBar.Visibility = count > 0 ? Visibility.Visible : Visibility.Collapsed;

        // The checkboxes only exist while something is selected, so the cards stay clean and
        // the boxes report whatever the marquee or a click produced
        ExamList.IsMultiSelectCheckBoxEnabled = count > 0;

        ApplyFavouriteState();
    }

    /// <summary>
    ///     Shows the star and the label that fit the selection: every picked exam favourited makes
    ///     the button offer taking them all off, any other combination offers putting them all on.
    /// </summary>
    private void ApplyFavouriteState()
    {
        var exams = ExamList.SelectedItems.OfType<ExamWithTotal>().ToList();

        var allFavorited = exams.Count > 0 && exams.All(exam => exam.Exam.IsFavorite);

        FavouriteSelectionButton.Icon = FavoriteIcons.IconFor(allFavorited);
        FavouriteSelectionButton.Label = _strings.Get(allFavorited ? "remove_favourite" : "add_favourite");
    }

    private void OnClearSelection(object sender, RoutedEventArgs e)
    {
        ExamList.SelectedItems.Clear();
    }

    private void OnAddExam(object sender, RoutedEventArgs e)
    {
        this.Frame.Navigate(typeof(InputtingPage));
    }

    private async void OnDeleteSelected(object sender, RoutedEventArgs e)
    {
        var ids = SelectedIds();

        if (ids.Count == 0) return;

        var dialog = new ContentDialog
        {
            Title = _strings.Get("attention"),
            Content = _strings.Get("delete_exam_message"),
            PrimaryButtonText = _strings.Get("delete"),
            CloseButtonText = _strings.Get("cancel"),
            XamlRoot = XamlRoot
        };

        if (await dialog.ShowAsync() != ContentDialogResult.Primary) return;

        ExamList.SelectedItems.Clear();
        await AppServices.Exams.MutateAsync(repository => repository.DeleteExamsAsync(ids));
    }

    private async void OnFavouriteSelected(object sender, RoutedEventArgs e)
    {
        var exams = ExamList.SelectedItems.OfType<ExamWithTotal>().ToList();

        if (exams.Count == 0) return;

        // Every picked exam goes to the state the button is offering: all favourited means they
        // all come off, any other mix means they all go on
        var target = !exams.All(exam => exam.Exam.IsFavorite);

        ExamList.SelectedItems.Clear();
        await AppServices.Exams.MutateAsync(async repository =>
        {
            foreach (var exam in exams)
                await repository.SetFavoriteAsync(exam.Exam.Id, target);
        });
    }

    // ------------------------------------------------------------------
    // Marquee selection
    // ------------------------------------------------------------------

    /// <summary>
    ///     Starts the marquee when the left mouse button goes down on the empty space between the
    ///     cards. A press that lands on a card belongs to that card: the click opens the detail
    ///     and the checkbox toggles, so the marquee never steals from them.
    /// </summary>
    private void OnListPointerPressed(object sender, PointerRoutedEventArgs e)
    {
        // Touch and pen keep their own gestures; the marquee is a mouse-only affordance
        if (e.Pointer.PointerDeviceType != PointerDeviceType.Mouse) return;

        var point = e.GetCurrentPoint(ExamList);

        if (!point.Properties.IsLeftButtonPressed) return;
        if (IsOverInteractive(e.OriginalSource)) return;

        _marqueeActive = true;
        _marqueeAdditive = IsCtrlDown();
        _marqueeOrigin = point.Position;
        _marqueeBase = ExamList.SelectedItems.OfType<object>().ToList();
        _marqueeLast = [];

        UpdateMarqueeRect(new Rect(_marqueeOrigin.X, _marqueeOrigin.Y, 0, 0));
        MarqueeCanvas.Visibility = Visibility.Visible;

        ExamList.CapturePointer(e.Pointer);
        e.Handled = true;
    }

    private void OnListPointerMoved(object sender, PointerRoutedEventArgs e)
    {
        if (!_marqueeActive) return;

        var position = e.GetCurrentPoint(ExamList).Position;
        var bounds = MarqueeBounds(position);

        UpdateMarqueeRect(bounds);
        AutoScrollTowards(position.Y);
        ApplyMarqueeSelection(bounds);

        e.Handled = true;
    }

    private void OnListPointerReleased(object sender, PointerRoutedEventArgs e)
    {
        if (!_marqueeActive) return;

        var position = e.GetCurrentPoint(ExamList).Position;
        var distance = Math.Abs(position.X - _marqueeOrigin.X) + Math.Abs(position.Y - _marqueeOrigin.Y);

        // A drag that never left the starting block is an empty space click, which clears the
        // selection the way a click between the cards does everywhere else
        if (distance < 4 && !_marqueeAdditive) ExamList.SelectedItems.Clear();

        EndMarquee();
        e.Handled = true;
    }

    private void OnListPointerCanceled(object sender, PointerRoutedEventArgs e)
    {
        if (_marqueeActive) EndMarquee();
    }

    /// <summary>
    ///     Escape during a marquee ends the drag and keeps the rows it selected; Escape afterwards
    ///     clears the selection, marquee or checkboxes alike, the same way the cancel button
    ///     does. The file explorer behaves the same way: one Esc stops the drag, the next one
    ///     drops the selection.
    /// </summary>
    private void OnEscapeInvoked(KeyboardAccelerator sender, KeyboardAcceleratorInvokedEventArgs args)
    {
        if (_marqueeActive)
        {
            EndMarquee();
            args.Handled = true;
            return;
        }

        if (ExamList.SelectedItems.Count == 0) return;

        ExamList.SelectedItems.Clear();
        args.Handled = true;
    }

    /// <summary>
    ///     Selects every row the rectangle touches. Without Ctrl the marquee owns the selection
    ///     and replaces it; with Ctrl the selection the student had before dragging stays and the
    ///     rectangle adds to it.
    /// </summary>
    private void ApplyMarqueeSelection(Rect bounds)
    {
        var inside = new List<object>();

        foreach (var item in _visible)
        {
            // Containers off the viewport are virtualised away, and the rectangle can never
            // reach what is not on screen
            if (ExamList.ContainerFromItem(item) is not ListViewItem container) continue;

            var itemBounds = container.TransformToVisual(ExamList).TransformBounds(
                new Rect(0, 0, container.ActualWidth, container.ActualHeight));

            if (IntersectsWith(bounds, itemBounds)) inside.Add(item);
        }

        if (_marqueeLast.Count == inside.Count && _marqueeLast.SequenceEqual(inside)) return;

        _marqueeLast = inside;

        ExamList.SelectedItems.Clear();

        var selection = _marqueeAdditive && _marqueeBase is not null
            ? _marqueeBase.Union(inside)
            : (IEnumerable<object>)inside;

        foreach (var item in selection) ExamList.SelectedItems.Add(item);
    }

    private Rect MarqueeBounds(Point position)
    {
        return new Rect(
            Math.Min(_marqueeOrigin.X, position.X),
            Math.Min(_marqueeOrigin.Y, position.Y),
            Math.Abs(position.X - _marqueeOrigin.X),
            Math.Abs(position.Y - _marqueeOrigin.Y));
    }

    /// <summary>Rectangles overlap when neither clears the other on either axis.</summary>
    private static bool IntersectsWith(Rect a, Rect b)
    {
        return a.Left < b.Right && a.Right > b.Left && a.Top < b.Bottom && a.Bottom > b.Top;
    }

    private void UpdateMarqueeRect(Rect bounds)
    {
        Canvas.SetLeft(MarqueeRect, bounds.X);
        Canvas.SetTop(MarqueeRect, bounds.Y);
        MarqueeRect.Width = bounds.Width;
        MarqueeRect.Height = bounds.Height;
    }

    /// <summary>
    ///     Drags the viewport when the pointer nears the top or bottom edge, so a marquee can span
    ///     more rows than fit on the screen at once.
    /// </summary>
    private void AutoScrollTowards(double y)
    {
        _listScrollViewer ??= FindScrollViewer(ExamList);

        if (_listScrollViewer is null) return;

        const double edge = 24.0;
        const double step = 16.0;

        double offset = _listScrollViewer.VerticalOffset;

        if (y < edge) offset -= step;
        else if (y > _listScrollViewer.ViewportHeight - edge) offset += step;
        else return;

        _listScrollViewer.ChangeView(null, offset, null, true);
    }

    /// <summary>True when the press landed on a card, a checkbox or the scroll bars themselves.</summary>
    private static bool IsOverInteractive(object? source)
    {
        for (var current = source as DependencyObject; current is not null; current = VisualTreeHelper.GetParent(current))
        {
            if (current is ListViewItem or Thumb or ScrollBar) return true;
        }

        return false;
    }

    private static bool IsCtrlDown()
    {
        // The state arrives as a flags enum over the key asked for, so zero means up
        return (int)InputKeyboardSource.GetKeyStateForCurrentThread(VirtualKey.Control) != 0;
    }

    private static ScrollViewer? FindScrollViewer(DependencyObject start)
    {
        if (start is ScrollViewer viewer) return viewer;

        var count = VisualTreeHelper.GetChildrenCount(start);

        for (var index = 0; index < count; index++)
        {
            var found = FindScrollViewer(VisualTreeHelper.GetChild(start, index));

            if (found is not null) return found;
        }

        return null;
    }

    private void EndMarquee()
    {
        _marqueeActive = false;
        _marqueeBase = null;
        _marqueeLast = [];
        MarqueeCanvas.Visibility = Visibility.Collapsed;
    }

    // ------------------------------------------------------------------
    // Sharing
    // ------------------------------------------------------------------

    /// <summary>
    ///     Shares the light summary the cards already show: one line per exam with its dates and
    ///     its mark against the full mark.
    /// </summary>
    private void OnShareSummary(object sender, RoutedEventArgs e)
    {
        var exams = ExamList.SelectedItems.OfType<ExamWithTotal>().ToList();

        if (exams.Count == 0) return;

        var lines = exams.Select(exam =>
            $"{exam.Exam.Name} · {ExamDates.FormatDateRange(exam.Exam.StartDate, exam.Exam.EndDate)} · " +
            ScoreDetailLine(exam));

        DesktopShare.ShareText(ShareTitle(exams), string.Join(Environment.NewLine, lines));
    }

    /// <summary>
    ///     Shares the full detail sheet per exam, the same content the detail page puts together:
    ///     the totals, the rank and one line per recorded subject. Every exam has to be read back
    ///     with its scores, so this takes a few round trips for a large selection.
    /// </summary>
    private async void OnShareDetails(object sender, RoutedEventArgs e)
    {
        var exams = ExamList.SelectedItems.OfType<ExamWithTotal>().ToList();

        if (exams.Count == 0) return;

        var blocks = new List<string>();

        foreach (var picked in exams)
        {
            var loaded = await AppServices.Exams.Repository.GetExamWithScoresAsync(picked.Exam.Id);

            if (loaded is null) continue;

            var scores = loaded.Scores;
            var totalMark = ExamScoring.TotalMark(scores);
            var totalFullMark = ExamScoring.TotalFullMark(scores);
            var totalPercentage = ExamScoring.Percentage(totalMark, totalFullMark);
            var rank = ExamScoring.RankOf(loaded.Exam.Id, AppServices.Exams.Exams.ToList());

            var lines = new List<string>
            {
                $"{_strings.Get("exam")}: {loaded.Exam.Name}",
                $"{_strings.Get("exam_dates")}: {ExamDates.FormatDateRange(loaded.Exam.StartDate, loaded.Exam.EndDate)}",
                $"{_strings.Get("total_score")}: {ExamScoring.FormatScoreValue(totalMark)}/{ExamScoring.FormatScoreValue(totalFullMark)}",
                $"{_strings.Get("percentage")}: {totalPercentage:0.#}%",
                $"{_strings.Get("rank")}: {rank}"
            };

            lines.AddRange(ExamScoring.RecordedSubjectKeys(SubjectCatalogue.ExamSubjectKeys, scores)
                .Select(key =>
                {
                    var score = scores.First(item => item.SubjectKey == key);

                    return $"{SubjectLabels.NameOf(_strings, key)}: " +
                           $"{ExamScoring.FormatScoreValue(score.Mark)}/{ExamScoring.FormatScoreValue(score.FullMark)}";
                }));

            blocks.Add(string.Join(Environment.NewLine, lines));
        }

        if (blocks.Count == 0) return;

        DesktopShare.ShareText(ShareTitle(exams), string.Join(Environment.NewLine + Environment.NewLine, blocks));
    }

    /// <summary>The mark against the full mark with the percentage, exactly as the cards show it.</summary>
    private static string ScoreDetailLine(ExamWithTotal exam)
    {
        var mark = ExamScoring.FormatScoreValue(exam.TotalMark);
        if (exam.TotalFullMark <= 0f) return mark;

        var percentage = (int)MathF.Round(
            ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark),
            MidpointRounding.AwayFromZero);
        var fullMark = ExamScoring.FormatScoreValue(exam.TotalFullMark);

        return percentage > 0 ? $"{mark} / {fullMark} · {percentage}%" : $"{mark} / {fullMark}";
    }

    /// <summary>One exam shares under its own name; several share under the app name and a count.</summary>
    private string ShareTitle(IReadOnlyList<ExamWithTotal> exams)
    {
        return exams.Count == 1
            ? exams[0].Exam.Name
            : $"{_strings.Get("app_name")} · {exams.Count}";
    }

    private List<long> SelectedIds()
    {
        return ExamList.SelectedItems.OfType<ExamWithTotal>().Select(exam => exam.Exam.Id).ToList();
    }
}

