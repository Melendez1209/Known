using System.Globalization;
using Known.Core.Data;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using LiveChartsCore;
using LiveChartsCore.SkiaSharpView;
using LiveChartsCore.SkiaSharpView.Painting;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Known.Pages;

/// <summary>
///     A single exam: its totals, how the marks split across subjects, and how it compares with every
///     other exam the student has recorded.
/// </summary>
public sealed partial class DetailPage
{
    private readonly StringLocalizer _strings = new();
    private Exam? _exam;

    private long _examId;
    private string _shareText = string.Empty;

    /// <summary>Creates the page and translates its captions.</summary>
    public DetailPage()
    {
        InitializeComponent();

        OverviewTab.Header = _strings.Get("all");
        ComparisonTab.Header = _strings.Get("comparison");
        TotalScoreCaption.Text = _strings.Get("total_score");
        PercentageCaption.Text = _strings.Get("percentage");
        RankCaption.Text = _strings.Get("rank");
        DistributionCaption.Text = _strings.Get("score_distribution");
        SubjectsCaption.Text = _strings.Get("subjects");

        EditButton.Label = _strings.Get("edit");
        DeleteButton.Label = _strings.Get("delete");
        ShareButton.Label = _strings.Get("share");
        FavouriteButton.Label = _strings.Get("add_favourite");

        Exams.Reloaded += OnExamsReloaded;
    }

    /// <inheritdoc />
    public override void OnNavigatedAway()
    {
        Exams.Reloaded -= OnExamsReloaded;

        base.OnNavigatedAway();
    }

    /// <summary>Keeps the star and its label in step with the stored state.</summary>
    private void ApplyFavouriteState()
    {
        var isFavorite = _exam?.IsFavorite ?? false;

        FavouriteButton.Icon = FavoriteIcons.IconFor(isFavorite);
        FavouriteButton.Label = _strings.Get(isFavorite ? "remove_favourite" : "add_favourite");
    }

    /// <summary>
    ///     Refreshes the star after a write. The exam held here is the instance loaded for this page,
    ///     so only the pinned flag is taken from the shared list rather than reloading the whole page,
    ///     which would blank the charts and flash the progress ring.
    /// </summary>
    private void OnExamsReloaded(object? sender, EventArgs e)
    {
        if (_exam is null) return;

        var current = Exams.Exams.FirstOrDefault(exam => exam.Exam.Id == _exam.Id);

        if (current is null) return;

        _exam.IsFavorite = current.Exam.IsFavorite;

        ApplyFavouriteState();
    }

    /// <inheritdoc />
    protected override void OnPageLoaded()
    {
        _examId = NavigationParameter is long id ? id : 0;

        if (_examId != 0) LoadAsync();
    }

    private async void LoadAsync()
    {
        var loaded = await Exams.Repository.GetExamWithScoresAsync(_examId);

        if (loaded is null)
        {
            BusyRing.IsActive = false;
            return;
        }

        _exam = loaded.Exam;

        ChartPalette.Refresh();

        var scores = loaded.Scores;
        var totalMark = ExamScoring.TotalMark(scores);
        var totalFullMark = ExamScoring.TotalFullMark(scores);
        var totalPercentage = ExamScoring.Percentage(totalMark, totalFullMark);
        var rank = ExamScoring.RankOf(_examId, Exams.Exams.ToList());
        var stats = await Exams.Repository.GetSubjectStatsAsync(_examId);

        TitleText.Text = _exam.Name;
        SubtitleText.Text = ExamDates.FormatDateRange(_exam.StartDate, _exam.EndDate);

        TotalScoreText.Text = totalMark.ToString("0.#", CultureInfo.CurrentCulture);
        PercentageText.Text = totalPercentage.ToString("0.#", CultureInfo.CurrentCulture) + "%";
        RankText.Text = rank.ToString(CultureInfo.CurrentCulture);

        BusyRing.IsActive = false;

        ApplyFavouriteState();

        var subjects = ExamScoring.RecordedSubjectKeys(SubjectCatalogue.ExamSubjectKeys, scores)
            .Select(key =>
            {
                var score = scores.First(item => item.SubjectKey == key);
                var average = ExamScoring.HistoricalAverage(key, stats);

                return new SubjectFigure(
                    key,
                    SubjectLabels.NameOf(_strings, key),
                    score.Mark,
                    score.FullMark,
                    ExamScoring.AverageDelta(score.Mark, score.FullMark, average));
            })
            .ToList();

        // One slice per subject, each in its own colour. The colour is keyed on the subject rather
        // than on its position in the legend, so a subject looks the same in every chart
        DistributionChart.Series = subjects
            .Select(subject => (ISeries)new PieSeries<double>
            {
                Values = [subject.Mark],
                Name = subject.Name,
                Fill = new SolidColorPaint(ChartPalette.CategoricalAt(SubjectCatalogue.IndexOfKey(subject.SubjectKey)))
            })
            .ToArray();

        BuildComparisonChart();
        BuildSubjectRows(subjects);
        BuildShareText(totalMark, totalFullMark, totalPercentage, rank, subjects);
    }

    /// <summary>
    ///     Every exam as a column, with the mean drawn across them, so one exam can be placed in the
    ///     context of the rest.
    /// </summary>
    private void BuildComparisonChart()
    {
        var ordered = Exams.Exams.OrderBy(exam => exam.Exam.StartDate).ToList();

        var labels = ordered
            .Select(exam => ExamDates.FormatDateRange(exam.Exam.StartDate, exam.Exam.EndDate))
            .ToList();

        var percentages = ordered
            .Select(exam => (double)ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark))
            .ToList();

        var average = percentages.Count == 0 ? 0d : percentages.Average();

        ComparisonChart.Series =
        [
            new ColumnSeries<double>
            {
                Name = _strings.Get("percentage"),
                Values = percentages.ToArray(),
                Fill = new SolidColorPaint(ChartPalette.Accent)
            },
            new LineSeries<double>
            {
                Name = _strings.Get("average"),
                Values = percentages.Select(_ => average).ToArray(),
                Fill = null,
                GeometrySize = 0,
                Stroke = new SolidColorPaint(ChartPalette.AccentSecondary)
            }
        ];

        ComparisonChart.XAxes = [BuildIndexAxis(labels)];
        ComparisonChart.YAxes = [BuildValueAxis(_strings.Get("percentage"), 0, 100)];
    }

    /// <summary>
    ///     Builds one row per recorded subject. The rows are created in code because each one needs a
    ///     different mix of values and a history that may not exist.
    /// </summary>
    private void BuildSubjectRows(IReadOnlyList<SubjectFigure> subjects)
    {
        var panel = new StackPanel { Spacing = 12 };

        foreach (var subject in subjects)
        {
            var grid = new Grid { ColumnSpacing = 16 };

            for (var column = 0; column < 4; column++)
                grid.ColumnDefinitions.Add(new ColumnDefinition
                {
                    Width = new GridLength(column == 0 ? 2 : 1, GridUnitType.Star)
                });

            var name = new TextBlock
            {
                Text = subject.Name,
                VerticalAlignment = VerticalAlignment.Center,
                Style = (Style)Application.Current.Resources["BodyStrongTextBlockStyle"]
            };

            Grid.SetColumn(name, 0);
            grid.Children.Add(name);

            Grid.SetColumn(AddCell(grid, subject.Mark.ToString("0.#", CultureInfo.CurrentCulture)), 1);
            Grid.SetColumn(
                AddCell(grid,
                    ExamScoring.Percentage(subject.Mark, subject.FullMark).ToString("0.#", CultureInfo.CurrentCulture) +
                    "%"),
                2);

            // The comparison only makes sense once the subject has been recorded in another exam,
            // so the cell is left blank rather than showing a dash
            Grid.SetColumn(AddCell(grid, subject.DeltaText), 3);

            panel.Children.Add(grid);
        }

        SubjectRows.Children.Clear();
        SubjectRows.Children.Add(panel);
    }

    private static TextBlock AddCell(Grid parent, string text)
    {
        var block = new TextBlock
        {
            Text = text,
            HorizontalAlignment = HorizontalAlignment.Right,
            VerticalAlignment = VerticalAlignment.Center,
            Style = (Style)Application.Current.Resources["BodyTextBlockStyle"]
        };

        parent.Children.Add(block);

        return block;
    }

    private void BuildShareText(
        float totalMark,
        float totalFullMark,
        float totalPercentage,
        int rank,
        IReadOnlyList<SubjectFigure> subjects)
    {
        var lines = new List<string>
        {
            $"{_strings.Get("exam")}: {_exam?.Name}",
            $"{_strings.Get("exam_dates")}: {ExamDates.FormatDateRange(_exam!.StartDate, _exam.EndDate)}",
            $"{_strings.Get("total_score")}: {totalMark:0.#}/{totalFullMark:0.#}",
            $"{_strings.Get("percentage")}: {totalPercentage:0.#}%",
            $"{_strings.Get("rank")}: {rank}"
        };

        lines.AddRange(subjects.Select(s => $"{s.Name}: {s.Mark:0.#}/{s.FullMark:0.#}"));

        _shareText = string.Join(Environment.NewLine, lines);
    }

    private static Axis BuildIndexAxis(IReadOnlyList<string> labels)
    {
        return new Axis
        {
            LabelsPaint = new SolidColorPaint(ChartPalette.Text),
            NamePaint = new SolidColorPaint(ChartPalette.Text),
            SeparatorsPaint = new SolidColorPaint(ChartPalette.GridLine),
            TextSize = 11,
            MinStep = 1,
            Labeler = value =>
            {
                var index = (int)Math.Round(value);

                return index >= 0 && index < labels.Count ? labels[index] : string.Empty;
            }
        };
    }

    private static Axis BuildValueAxis(string name, double? min, double? max)
    {
        return new Axis
        {
            Name = name,
            NamePaint = new SolidColorPaint(ChartPalette.Text),
            LabelsPaint = new SolidColorPaint(ChartPalette.Text),
            SeparatorsPaint = new SolidColorPaint(ChartPalette.GridLine),
            TextSize = 11,
            MinLimit = min,
            MaxLimit = max,
            Labeler = value => ((int)Math.Round(value)).ToString(CultureInfo.CurrentCulture)
        };
    }

    private void OnEdit(object sender, RoutedEventArgs e)
    {
        if (_exam is null) return;

        Shell.NavigateInTab(
            typeof(InputtingPage),
            new InputtingNavigation(_exam.StartDate, _exam.EndDate, _exam.Id));
    }

    private async void OnToggleFavourite(object sender, RoutedEventArgs e)
    {
        if (_exam is null) return;

        await Exams.MutateAsync(repository => repository.SetFavoriteAsync(_exam.Id, !_exam.IsFavorite));
    }

    private async void OnDelete(object sender, RoutedEventArgs e)
    {
        if (_exam is null) return;

        var dialog = new ContentDialog
        {
            Title = _strings.Get("attention"),
            Content = _strings.Get("delete_exam_message"),
            PrimaryButtonText = _strings.Get("delete"),
            CloseButtonText = _strings.Get("cancel"),
            XamlRoot = XamlRoot
        };

        if (await dialog.ShowAsync() != ContentDialogResult.Primary) return;

        await Exams.MutateAsync(repository => repository.DeleteExamAsync(_exam.Id));
        Shell.GoBack();
    }

    private void OnShare(object sender, RoutedEventArgs e) =>
        DesktopShare.ShareText(_exam?.Name ?? _strings.Get("exam"), _shareText);

    /// <summary>One subject's figures on the detail page.</summary>
    /// <param name="SubjectKey">Stable subject key, used to pick a consistent colour.</param>
    /// <param name="Name">Translated subject name.</param>
    /// <param name="Mark">Score achieved in this exam.</param>
    /// <param name="FullMark">Score the mark is measured against.</param>
    /// <param name="Delta">Signed difference against the historical mean, or null without history.</param>
    private sealed record SubjectFigure(
        string SubjectKey,
        string Name,
        float Mark,
        float FullMark,
        float? Delta
    )
    {
        /// <summary>The signed difference, already formatted with an explicit sign.</summary>
        public string DeltaText => Delta is { } value
            ? (value >= 0 ? "+" : "") + value.ToString("0.#", CultureInfo.CurrentCulture)
            : string.Empty;
    }
}