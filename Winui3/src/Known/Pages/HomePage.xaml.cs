using System.Globalization;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using LiveChartsCore.SkiaSharpView;
using LiveChartsCore.SkiaSharpView.Painting;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Navigation;

namespace Known.Pages;

/// <summary>
///     The dashboard: a row of summary figures followed by the trend and per subject charts.
/// </summary>
/// <remarks>
///     The page owns its own state. The chart series are rebuilt whenever the exam list or the theme
///     changes, because a Skia colour is a snapshot and cannot follow an XAML theme resource the way a
///     brush does.
/// </remarks>
public sealed partial class HomePage
{
    private readonly StringLocalizer _strings = new();

    /// <summary>Which measure the trend chart plots. Survives a restart through the settings row.</summary>
    private TrendMetric _trendMetric;

    /// <summary>Which measure the subject chart plots. Survives a restart through the settings row.</summary>
    private TrendMetric _subjectMetric;

    /// <summary>Creates the page, translates its captions and subscribes to the shared data.</summary>
    public HomePage()
    {
        InitializeComponent();

        _trendMetric = AppServices.Settings.Current.TrendMetric;
        _subjectMetric = AppServices.Settings.Current.SubjectMetric;

        // The captions never change: the toggle itself carries the on/off state. Setting IsChecked
        // here fires the Checked/Unchecked events before the page is loaded, so the handlers guard
        // on IsLoaded and only perform real work once the page is ready.
        TrendMetricLabel.Text = _strings.Get("show_in_rate");
        SubjectMetricLabel.Text = _strings.Get("show_in_rate");
        TrendMetricButton.IsChecked = _trendMetric == TrendMetric.Rate;
        SubjectMetricButton.IsChecked = _subjectMetric == TrendMetric.Rate;

        TitleText.Text = _strings.Get("home");
        TrendCaption.Text = _strings.Get("score_trend");
        SubjectCaption.Text = _strings.Get("subject_average");
        ToolTipService.SetToolTip(TrendMetricButton, _strings.Get("trend_metric_hint"));
        ToolTipService.SetToolTip(SubjectMetricButton, _strings.Get("subject_metric_hint"));

        AppServices.Exams.Reloaded += OnReloaded;
        AppServices.Theme.ThemeChanged += OnThemeChanged;

        // The shared exam store finishes its first load before this page exists, so no Reloaded
        // event is waiting to be picked up. The charts would otherwise stay empty until the first
        // write, and Loaded is the earliest point the controls can safely be filled in.
        Loaded += (_, _) => Reload();
    }

    /// <inheritdoc />
    protected override void OnNavigatedFrom(NavigationEventArgs e)
    {
        AppServices.Exams.Reloaded -= OnReloaded;
        AppServices.Theme.ThemeChanged -= OnThemeChanged;

        base.OnNavigatedFrom(e);
    }

    private void OnReloaded(object? sender, EventArgs e)
    {
        Reload();
    }

    private void OnThemeChanged(object? sender, EventArgs e)
    {
        Reload();
    }

    /// <summary>Switches the trend chart to percentages and remembers the choice.</summary>
    private async void OnTrendMetricChecked(object sender, RoutedEventArgs e)
    {
        if (!IsLoaded) return;

        _trendMetric = TrendMetric.Rate;

        Reload();

        await AppServices.Settings.SetTrendMetricAsync(_trendMetric);
    }

    /// <summary>Switches the trend chart back to raw totals and remembers the choice.</summary>
    private async void OnTrendMetricUnchecked(object sender, RoutedEventArgs e)
    {
        if (!IsLoaded) return;

        _trendMetric = TrendMetric.Marks;

        Reload();

        await AppServices.Settings.SetTrendMetricAsync(_trendMetric);
    }

    /// <summary>Switches the subject chart to percentages and remembers the choice.</summary>
    private async void OnSubjectMetricChecked(object sender, RoutedEventArgs e)
    {
        if (!IsLoaded) return;

        _subjectMetric = TrendMetric.Rate;

        Reload();

        await AppServices.Settings.SetSubjectMetricAsync(_subjectMetric);
    }

    /// <summary>Switches the subject chart back to raw averages and remembers the choice.</summary>
    private async void OnSubjectMetricUnchecked(object sender, RoutedEventArgs e)
    {
        if (!IsLoaded) return;

        _subjectMetric = TrendMetric.Marks;

        Reload();

        await AppServices.Settings.SetSubjectMetricAsync(_subjectMetric);
    }

    /// <summary>Rebuilds the figures and both charts from the current exam list and theme.</summary>
    private void Reload()
    {
        if (!IsLoaded) return;

        ChartPalette.Refresh();

        var ordered = AppServices.Exams.Exams.OrderBy(exam => exam.Exam.StartDate).ToList();

        var trendMetricName =
            _trendMetric == TrendMetric.Marks ? _strings.Get("average_score") : _strings.Get("average_rate");

        StatCards.ItemsSource = new[]
        {
            new StatCard(ordered.Count.ToString(CultureInfo.CurrentCulture), _strings.Get("exam")),
            new StatCard(
                AppServices.Exams.AverageMark.ToString("0.#", CultureInfo.CurrentCulture),
                _strings.Get("average_score")),
            new StatCard(
                AppServices.Exams.AveragePercentage.ToString("0.#", CultureInfo.CurrentCulture) + "%",
                _strings.Get("average_rate")),
            new StatCard(AppServices.Exams.BestRank.ToString(CultureInfo.CurrentCulture), _strings.Get("rank"))
        };

        var examLabels = ordered
            .Select(exam => ExamDates.FormatDateRange(exam.Exam.StartDate, exam.Exam.EndDate))
            .ToArray();

        var trendPlotsMarks = _trendMetric == TrendMetric.Marks;

        TrendChart.Series =
        [
            new LineSeries<double>
            {
                Name = trendMetricName,
                Values = ordered
                    .Select(exam => trendPlotsMarks
                        ? exam.TotalMark
                        : (double)ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark))
                    .ToArray(),
                // A filled area would hide the grid lines behind it and reads as a different measure
                Fill = null,
                GeometrySize = 7,
                Stroke = new SolidColorPaint(ChartPalette.Accent),
                GeometryFill = new SolidColorPaint(ChartPalette.Accent),
                GeometryStroke = new SolidColorPaint(ChartPalette.Surface)
            }
        ];

        TrendChart.XAxes = [BuildIndexAxis(examLabels)];

        // Raw totals run to several hundred and vary far more between exams than a percentage does,
        // so a fixed 0 to 100 would squash the line flat against the bottom of the plot.
        TrendChart.YAxes =
            [BuildValueAxis(trendMetricName, trendPlotsMarks ? null : 0, trendPlotsMarks ? null : 100)];

        var stats = AppServices.Exams.SubjectStats.ToList();
        var subjectLabels = stats.Select(stat => SubjectLabels.NameOf(_strings, stat.SubjectKey)).ToArray();

        var subjectMetricName =
            _subjectMetric == TrendMetric.Marks ? _strings.Get("average_score") : _strings.Get("average_rate");

        var subjectPlotsMarks = _subjectMetric == TrendMetric.Marks;

        SubjectChart.Series =
        [
            new ColumnSeries<double>
            {
                Name = subjectMetricName,
                Values = stats
                    .Select(stat => subjectPlotsMarks
                        ? (double)stat.AvgMark
                        : (double)ExamScoring.Percentage(stat.AvgMark, stat.AvgFullMark))
                    .ToArray(),
                Fill = new SolidColorPaint(ChartPalette.AccentTertiary)
            }
        ];

        SubjectChart.XAxes = [BuildIndexAxis(subjectLabels)];

        // The rate is a 0 to 100 percentage, so it gets the same fixed bounds as the trend chart in
        // rate mode instead of the automatically scaled mark axis.
        SubjectChart.YAxes =
            [BuildValueAxis(subjectMetricName, subjectPlotsMarks ? null : 0, subjectPlotsMarks ? null : 100)];
    }

    /// <summary>
    ///     The charts plot a positional index, so an axis whose labels are looked up from a list turns
    ///     that index back into an exam date or a subject name.
    /// </summary>
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
}

public sealed record StatCard(string Value, string Caption);