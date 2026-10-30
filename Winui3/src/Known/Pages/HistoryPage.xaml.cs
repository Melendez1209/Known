using System.Collections.ObjectModel;
using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

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

    /// <summary>Creates the page and fills the list from the shared exam store.</summary>
    public HistoryPage()
    {
        InitializeComponent();

        SearchBox.PlaceholderText = _strings.Get("search_hint");
        EmptyText.Text = _strings.Get("no_history");
        AddButton.Content = _strings.Get("add");
        DeleteSelectionButton.Label = _strings.Get("delete");
        FavouriteSelectionButton.Label = _strings.Get("add_favourite");

        BuildFavouriteFilter();
        BuildViewModeFilter();

        ExamList.ItemsSource = _visible;

        AppServices.Exams.Reloaded += OnExamsReloaded;

        Reload();
    }

    /// <inheritdoc />
    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
        AppServices.Exams.Reloaded -= OnExamsReloaded;

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
        ApplyFilter(SearchBox.Text);
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

    private void OnSearchChanged(AutoSuggestBox sender, AutoSuggestBoxTextChangedEventArgs args)
    {
        if (args.Reason != AutoSuggestionBoxTextChangeReason.UserInput) return;

        ApplyFilter(sender.Text);
    }

    /// <summary>Labels the favourite toggle; unchecked means every exam, checked only favourites.</summary>
    private void BuildFavouriteFilter()
    {
        FavouriteFilterText.Text = _strings.Get("favourites");
    }

    private void OnFavouriteFilterChanged(object sender, RoutedEventArgs e)
    {
        ApplyFilter(SearchBox.Text);
    }

    /// <summary>Fills the density switcher and restores whichever view was stored last time.</summary>
    private void BuildViewModeFilter()
    {
        ViewModeFilter.ItemsSource = new[]
        {
            _strings.Get("compact"),
            _strings.Get("standard"),
            _strings.Get("list")
        };

        _restoringViewMode = true;
        ViewModeFilter.SelectedIndex = (int)AppServices.Settings.Current.HistoryViewMode;
        _restoringViewMode = false;

        _viewMode = AppServices.Settings.Current.HistoryViewMode;
        ApplyViewMode();
    }

    private async void OnViewModeChanged(object sender, SelectionChangedEventArgs e)
    {
        if (_restoringViewMode) return;

        var index = ViewModeFilter.SelectedIndex;
        if (index < 0) return;

        var mode = (HistoryViewMode)index;
        if (mode == _viewMode) return;

        _viewMode = mode;
        ApplyViewMode();

        await AppServices.Settings.SetHistoryViewModeAsync(mode);
    }

    /// <summary>
    ///     Assigns the row template and the layout panel that match the stored density: a wrapping
    ///     grid for the two card densities and the plain stack for the list.
    /// </summary>
    private void ApplyViewMode()
    {
        switch (_viewMode)
        {
            case HistoryViewMode.Compact:
                ExamList.ItemTemplate = (DataTemplate)Resources["CompactRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["CompactGridPanel"];
                break;

            case HistoryViewMode.List:
                ExamList.ItemTemplate = (DataTemplate)Resources["ListRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["ListPanel"];
                break;

            default:
                ExamList.ItemTemplate = (DataTemplate)Resources["StandardRowTemplate"];
                ExamList.ItemsPanel = (ItemsPanelTemplate)Resources["StandardGridPanel"];
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

        FavouriteSelectionButton.IsEnabled = count == 1;

        ApplyFavouriteState();
    }

    /// <summary>
    ///     Shows the star and the label that fit the selected exam, so the button says what it is about
    ///     to do rather than always offering to add.
    /// </summary>
    private void ApplyFavouriteState()
    {
        var exam = ExamList.SelectedItems.OfType<ExamWithTotal>().FirstOrDefault();

        var isFavorite = exam?.Exam.IsFavorite ?? false;

        FavouriteSelectionButton.Icon = FavoriteIcons.IconFor(isFavorite);
        FavouriteSelectionButton.Label = _strings.Get(isFavorite ? "remove_favourite" : "add_favourite");
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
        var exam = ExamList.SelectedItems.OfType<ExamWithTotal>().FirstOrDefault();

        if (exam is null) return;

        var target = !exam.Exam.IsFavorite;

        ExamList.SelectedItems.Clear();
        await AppServices.Exams.MutateAsync(repository => repository.SetFavoriteAsync(exam.Exam.Id, target));
    }

    private List<long> SelectedIds()
    {
        return ExamList.SelectedItems.OfType<ExamWithTotal>().Select(exam => exam.Exam.Id).ToList();
    }
}

