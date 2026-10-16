namespace Known.Core.Domain;

/// <summary>
/// Maps the stable subject keys used in the database onto the translatable strings shown in the
/// interface. Keeping the mapping beside the key list stops the resource key from drifting into
/// the screens, where a rename would only show up as a blank label.
/// </summary>
public static class SubjectLabels
{
    /// <summary>
    /// Resource key for the display name of a subject. An unknown key falls back to PE, matching
    /// the historical behaviour of the onboarding selector.
    /// </summary>
    public static string ResourceKeyFor(string? subjectKey) => subjectKey switch
    {
        "chinese" => "chinese",
        "maths" => "maths",
        "foreign_language" => "foreign_language",
        "physics" => "physics",
        "chemistry" => "chemistry",
        "biology" => "biology",
        "political" => "political",
        "history" => "history_subject",
        "geography" => "geography",
        _ => "pe"
    };

    /// <summary>Display name of a subject, resolved through the supplied localiser.</summary>
    public static string NameOf(Localization.StringLocalizer strings, string? subjectKey) =>
        strings.Get(ResourceKeyFor(subjectKey));

    /// <summary>Display name of the identity the student picked during onboarding.</summary>
    public static string IdentityName(Localization.StringLocalizer strings, int identity) => identity switch
    {
        StudentIdentity.Student => strings.Get("student"),
        StudentIdentity.Teacher => strings.Get("teacher"),
        StudentIdentity.Parent => strings.Get("parent"),
        _ => strings.Get("follow_system")
    };
}
