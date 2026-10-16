using System.Globalization;
using Known.Core.Domain;
using Known.Core.Localization;
using Known.Services;
using LiveChartsCore.Defaults;
using LiveChartsCore.Kernel.Sketches;
using LiveChartsCore.SkiaSharpView;
using LiveChartsCore.SkiaSharpView.Painting;

namespace Known.Pages;

/// <summary>
/// The dashboard: a row of summary figures followed by the trend and per subject charts.
/// </summary>
/// <remarks>
/// The page owns its own state. The chart series are rebuilt whenever the exam list or the theme
/// changes, because a Skia colour is a snapshot and cannot follow an XAML theme resource the way a
/// brush does.
/// </remarks>
public sealed partial class HomePage : KnownPage
{
    private readonly StringLocalizer _strings = new();

    /// <summary>Creates the page, translates its captions and subscribes to the shared data.</summary>
    public HomePage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("home");
        ExamCountCaption.Text = _strings.Get("exam");
        AverageCaption.Text = _strings.Get("average");
        BestRankCaption.Text = _strings.Get("rank");
        TrendCaption.Text = _strings.Get("score_trend");
        SubjectCaption.Text = _strings.Get("subject_average");

        Exams.Reloaded += OnReloaded;
        AppServices.Theme.ThemeChanged += OnThemeChanged;
    }

    /// <inheritdoc />
    public override void OnNavigatedAway()
    {
        Exams.Reloaded -= OnReloaded;
        AppServices.Theme.ThemeChanged -= OnThemeChanged;

        base.OnNavigatedAway();
    }

    private void OnReloaded(object? sender, EventArgs e) => Reload();

    private void OnThemeChanged(object? sender, EventArgs e) => Reload();

    /// <summary>Rebuilds the figures and both charts from the current exam list and theme.</summary>
    private void Reload()
    {
        if (!IsLoaded)
        {
            return;
        }

        ChartPalette.Refresh();

        var ordered = Exams.Exams.OrderBy(exam => exam.Exam.StartDate).ToList();

        ExamCountValue.Text = ordered.Count.ToString(CultureInfo.CurrentCulture);
        AverageValue.Text = Exams.AveragePercentage.ToString("0.#", CultureInfo.CurrentCulture) + "%";
        BestRankValue.Text = Exams.BestRank.ToString(CultureInfo.CurrentCulture);

        var examLabels = ordered
            .Select(exam => ExamDates.FormatDateRange(exam.Exam.StartDate, exam.Exam.EndDate))
            .ToArray();

        TrendChart.Series =
        [
            new LineSeries<double>
            {
                Name = _strings.Get("percentage"),
                Values = ordered
                    .Select(exam => (double)ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark))
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
        TrendChart.YAxes = [BuildValueAxis(_strings.Get("percentage"), 0, 100)];

        var stats = Exams.SubjectStats.ToList();
        var subjectLabels = stats.Select(stat => SubjectLabels.NameOf(_strings, stat.SubjectKey)).ToArray();

        SubjectChart.Series =
        [
            new ColumnSeries<double>
            {
                Name = _strings.Get("average"),
                Values = stats.Select(stat => (double)stat.AvgMark).ToArray(),
                Fill = new SolidColorPaint(ChartPalette.AccentTertiary)
            }
        ];

        SubjectChart.XAxes = [BuildIndexAxis(subjectLabels)];
        SubjectChart.YAxes = [BuildValueAxis(_strings.Get("mark"), null, null)];
    }

    /// <summary>
    /// The charts plot a positional index, so an axis whose labels are looked up from a list turns
    /// that index back into an exam date or a subject name.
    /// </summary>
    private static Axis BuildIndexAxis(IReadOnlyList<string> labels) => new()
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

    private static Axis BuildValueAxis(string name, double? min, double? max) => new()
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
