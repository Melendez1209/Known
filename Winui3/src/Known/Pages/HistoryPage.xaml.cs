using System.Collections.ObjectModel;
using Known.Core.Data;
using Known.Core.Localization;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The exam history: a searchable, multi selectable list of every recorded exam. Clicking a row
///     opens its detail, and ticking rows swaps the page footer for an action bar, matching the way
///     the Android build swapped its navigation bar for one.
/// </summary>
public sealed partial class HistoryPage : KnownPage
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

        Exams.Reloaded += OnExamsReloaded;

        Reload();
    }

    /// <inheritdoc />
    public override void OnNavigatedAway()
    {
        Exams.Reloaded -= OnExamsReloaded;
        base.OnNavigatedAway();
    }

    private void OnExamsReloaded(object? sender, EventArgs e)
    {
        Reload();
    }

    /// <summary>Rebuilds the row list, keeping whatever search term is currently typed.</summary>
    private void Reload()
    {
        ApplyFilter(SearchBox.Text);
    }

    private void ApplyFilter(string? query)
    {
        var matches = string.IsNullOrWhiteSpace(query)
            ? Exams.Exams.AsEnumerable()
            : Exams.Exams.Where(exam => exam.Exam.Name.Contains(query, StringComparison.CurrentCultureIgnoreCase));

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

        if (e.ClickedItem is ExamWithTotal exam) Shell.NavigateInTab(typeof(DetailPage), exam.Exam.Id);
    }

    private void OnSelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        var count = ExamList.SelectedItems.Count;

        SelectionBar.Visibility = count > 0 ? Visibility.Visible : Visibility.Collapsed;

        FavouriteSelectionButton.IsEnabled = count == 1;
    }

    private void OnClearSelection(object sender, RoutedEventArgs e)
    {
        ExamList.SelectedItems.Clear();
    }

    private void OnAddExam(object sender, RoutedEventArgs e)
    {
        Shell.NavigateInTab(typeof(DateRangePage));
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
        await Exams.MutateAsync(repository => repository.DeleteExamsAsync(ids));
    }

    private async void OnFavouriteSelected(object sender, RoutedEventArgs e)
    {
        var exam = ExamList.SelectedItems.OfType<ExamWithTotal>().FirstOrDefault();

        if (exam is null) return;

        var target = !exam.Exam.IsFavorite;

        ExamList.SelectedItems.Clear();
        await Exams.MutateAsync(repository => repository.SetFavoriteAsync(exam.Exam.Id, target));
    }

    private List<long> SelectedIds()
    {
        return ExamList.SelectedItems.OfType<ExamWithTotal>().Select(exam => exam.Exam.Id).ToList();
    }
}