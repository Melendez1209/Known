using System.Globalization;
using Known.Core.Data;
using Known.Core.Domain;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Data;

namespace Known.Converters;

/// <summary>
///     Formats the date range of the exam bound to it. The whole row is passed in rather than a single
///     field, because the range needs both ends and WinUI has no multi value binding.
/// </summary>
public sealed partial class ExamDateRangeConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, string language)
    {
        return value is ExamWithTotal exam
            ? ExamDates.FormatDateRange(exam.Exam.StartDate, exam.Exam.EndDate)
            : string.Empty;
    }

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, string language)
    {
        throw new NotSupportedException("The date range is display only.");
    }
}

/// <summary>
///     Formats the overall percentage of the exam bound to it, or an empty string when the exam
///     carries no score yet.
/// </summary>
public sealed partial class ExamPercentageConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, string language)
    {
        if (value is not ExamWithTotal exam) return string.Empty;

        var percentage = ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark);

        return percentage > 0f
            ? percentage.ToString("0.#", CultureInfo.CurrentCulture) + "%"
            : string.Empty;
    }

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, string language)
    {
        throw new NotSupportedException("The percentage is display only.");
    }
}

/// <summary>
///     Formats the total mark of the exam bound to it, normalised the same way the input fields
///     display scores, so 450.0 reads as 450.
/// </summary>
public sealed partial class ExamMarkConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, string language)
    {
        return value is ExamWithTotal exam
            ? ExamScoring.FormatScoreValue(exam.TotalMark)
            : string.Empty;
    }

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, string language)
    {
        throw new NotSupportedException("The mark is display only.");
    }
}

/// <summary>
///     Formats the mark against the full mark together with the percentage, e.g. 450 / 660 · 68%.
///     The percentage is dropped while the exam carries no score, matching the plain percentage
///     converter.
/// </summary>
public sealed partial class ExamScoreDetailConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, string language)
    {
        if (value is not ExamWithTotal exam) return string.Empty;

        var mark = ExamScoring.FormatScoreValue(exam.TotalMark);
        if (exam.TotalFullMark <= 0f) return mark;

        var percentage = (int)MathF.Round(
            ExamScoring.Percentage(exam.TotalMark, exam.TotalFullMark),
            MidpointRounding.AwayFromZero);
        var fullMark = ExamScoring.FormatScoreValue(exam.TotalFullMark);

        return percentage > 0 ? $"{mark} / {fullMark} · {percentage}%" : $"{mark} / {fullMark}";
    }

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, string language)
    {
        throw new NotSupportedException("The score detail is display only.");
    }
}

/// <summary>
///     Shows an element only for a pinned exam. WinUI ships no built-in converter for this, because a
///     bool carries no opinion about which visibility to use when it is false.
/// </summary>
public sealed partial class PinnedToVisibilityConverter : IValueConverter
{
    /// <inheritdoc />
    public object Convert(object value, Type targetType, object parameter, string language)
    {
        return value is true ? Visibility.Visible : Visibility.Collapsed;
    }

    /// <inheritdoc />
    public object ConvertBack(object value, Type targetType, object parameter, string language)
    {
        return value is Visibility.Visible;
    }
}