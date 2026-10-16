using System.Globalization;

namespace Known.Core.Domain;

/// <summary>
///     Date formatting for the exam list and the exam name. Dates are stored as epoch milliseconds
///     and rendered in the machine's local time zone, matching how the Android build formatted them.
/// </summary>
public static class ExamDates
{
    private const string IsoDateFormat = "yyyy-MM-dd";

    /// <summary>Default name for a new exam, derived from its start date, for example <c>2026-10-04</c>.</summary>
    public static string DefaultExamName(long startDate) => FormatDate(startDate);

    /// <summary>
    ///     Date range of an exam for the history list. Both ends collapse into a single date when the
    ///     exam lasts only one day.
    /// </summary>
    public static string FormatDateRange(long startDate, long endDate)
    {
        var start = FormatDate(startDate);
        var end = FormatDate(endDate);

        return start == end ? start : $"{start} - {end}";
    }

    private static string FormatDate(long epochMilliseconds)
    {
        return DateTimeOffset.FromUnixTimeMilliseconds(epochMilliseconds)
            .ToLocalTime()
            .ToString(IsoDateFormat, CultureInfo.InvariantCulture);
    }
}