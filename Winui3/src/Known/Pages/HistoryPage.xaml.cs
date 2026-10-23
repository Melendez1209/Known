using System.Collections.ObjectModel;
using Known.Core.Data;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

/// <summary>
///     The exam history: a searchable, multi selectable list of every recorded exam. Clicking a row
///     opens its detail, and ticking rows swaps the page footer for an action bar, matching the way
///     the Android build swapped its navigation bar for one.
/// </summary>
public sealed partial class HistoryPage : Page
{
    private readonly StringLocalizer _strings = new();
    private readonly ObservableCollection<ExamWithTotal> _visible = [];

    /// <summary>Creates the page and fills the list from the shared exam store.</summary>
    public HistoryPage()
    {
        InitializeComponent();

        SearchBox.PlaceholderText = _strings.Get("search_hint");
        EmptyText.Text = _strings.Get("no_history");
        AddButton.Content = _strings.Get("add");
        DeleteSelectionButton.Label = _strings.Get("delete");
        FavouriteSelectionButton.Label = _strings.Get("add_favourite");

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
        var matches = string.IsNullOrWhiteSpace(query)
            ? AppServices.Exams.Exams.AsEnumerable()
            : AppServices.Exams.Exams.Where(exam => exam.Exam.Name.Contains(query, StringComparison.CurrentCultureIgnoreCase));

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
        this.Frame.Navigate(typeof(DateRangePage));
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

