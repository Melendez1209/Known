namespace Known.Core.Domain;

/// <summary>
///     The subjects a student can be seated for during onboarding, in the order they are offered.
///     This is the shorter list shown by the onboarding selector; exams additionally cover Chinese,
///     mathematics and the foreign language.
/// </summary>
public static class SubjectCatalogue
{
    /// <summary>The first three subjects are compulsory, so their rows carry no checkbox.</summary>
    public const int CompulsorySubjectCount = 3;

    /// <summary>Full mark every score field starts with while the form is still blank.</summary>
    public const string DefaultFullMark = "150";

    /// <summary>Subjects selectable while onboarding, in display order.</summary>
    public static IReadOnlyList<string> SubjectKeys { get; } =
    [
        "physics",
        "chemistry",
        "biology",
        "political",
        "history",
        "geography",
        "pe"
    ];

    /// <summary>Every subject that can carry a score in an exam, in display order.</summary>
    public static IReadOnlyList<string> ExamSubjectKeys { get; } =
    [
        "chinese",
        "maths",
        "foreign_language",
        "physics",
        "chemistry",
        "biology",
        "political",
        "history",
        "geography",
        "pe"
    ];

    /// <summary>
    /// Position of a subject in <see cref="ExamSubjectKeys"/>. Returns zero for an unknown key, so a
    /// caller using this to pick a stable colour never has to guard against bad data.
    /// </summary>
    public static int IndexOfKey(string? subjectKey)
    {
        for (var index = 0; index < ExamSubjectKeys.Count; index++)
        {
            if (string.Equals(ExamSubjectKeys[index], subjectKey, StringComparison.Ordinal))
            {
                return index;
            }
        }

        return 0;
    }

    /// <summary>Splits the comma separated form used by <see cref="ToSubjectString" />.</summary>
    public static IReadOnlySet<string> ToSubjectKeySet(string? joined)
    {
        return string.IsNullOrEmpty(joined)
            ? new HashSet<string>(StringComparer.Ordinal)
            : new HashSet<string>(
                joined.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries),
                StringComparer.Ordinal);
    }

    /// <summary>Joins a subject selection into the comma separated form.</summary>
    public static string ToSubjectString(IEnumerable<string> subjects)
    {
        return string.Join(',', subjects);
    }
}

/// <summary>Who the student says they are, stored as an integer in the settings table.</summary>
/// <remarks>Named with a prefix so it never collides with the <c>Settings.Identity</c> property.</remarks>
public static class StudentIdentity
{
    public const int None = 0;
    public const int Student = 1;
    public const int Teacher = 2;
    public const int Parent = 3;
}
