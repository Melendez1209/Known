using System.Collections;
using System.Globalization;
using System.Resources;

namespace Known.Core.Localization;

/// <summary>One language the app can be shown in.</summary>
/// <param name="Tag">BCP 47 tag stored in the settings, empty meaning follow the system.</param>
/// <param name="EnglishName">Name shown in the language list, always written in that language.</param>
public sealed record LanguageOption(string Tag, string EnglishName)
{
    /// <summary>The culture to switch to, or <see langword="null" /> to follow the system.</summary>
    public CultureInfo? Culture => string.IsNullOrEmpty(Tag)
        ? null
        : new CultureInfo(Tag);

    /// <inheritdoc />
    public override string ToString()
    {
        return EnglishName;
    }
}

/// <summary>
///     Resolves a string key against the culture the app is currently running under, falling back to
///     the neutral resource set and finally to the key itself so a missing translation shows up as a
///     recognisable name rather than an empty label.
/// </summary>
public sealed class StringLocalizer
{
    /// <summary>Base name of the resource set, without the culture suffix.</summary>
    public const string ResourceBaseName = "Known.Core.Localization.AppStrings";

    private readonly ResourceManager _resources;

    /// <summary>Creates a localizer over the resources embedded in this assembly.</summary>
    public StringLocalizer()
        : this(new ResourceManager(ResourceBaseName, typeof(StringLocalizer).Assembly))
    {
    }

    /// <summary>Creates a localizer over an explicit resource manager, mainly for tests.</summary>
    public StringLocalizer(ResourceManager resources)
    {
        ArgumentNullException.ThrowIfNull(resources);

        _resources = resources;
    }

    /// <summary>The languages offered on the appearance page, the first one following the system.</summary>
    public static IReadOnlyList<LanguageOption> AvailableLanguages { get; } =
    [
        new(string.Empty, "Follow system"),
        new("en-US", "English (United States)"),
        new("en-GB", "English (United Kingdom)"),
        new("zh-Hans", "中文（简体）"),
        new("zh-Hant", "中文（繁體）")
    ];

    /// <summary>The culture currently in effect.</summary>
    public static CultureInfo CurrentCulture => CultureInfo.CurrentUICulture;

    /// <summary>Looks a key up in the current culture, falling back to neutral and then the key.</summary>
    public string this[string key] => Get(key);

    /// <summary>Looks a key up in the current culture, falling back to neutral and then the key.</summary>
    public string Get(string key)
    {
        if (string.IsNullOrEmpty(key)) return string.Empty;

        try
        {
            return _resources.GetString(key, CultureInfo.CurrentUICulture) ?? key;
        }
        catch (MissingManifestResourceException)
        {
            // The satellite assemblies were not deployed, which must not blank out the interface
            return key;
        }
    }

    /// <summary>Looks a key up in a specific culture, used by the tests and the preview tooling.</summary>
    public string Get(string key, CultureInfo culture)
    {
        ArgumentNullException.ThrowIfNull(culture);

        try
        {
            return _resources.GetString(key, culture) ?? key;
        }
        catch (MissingManifestResourceException)
        {
            return key;
        }
    }

    /// <summary>
    ///     Every key a culture's resource set defines, used to prove the generated satellites stay in
    ///     step with the neutral file.
    /// </summary>
    public IReadOnlyList<string> Keys(CultureInfo culture)
    {
        ArgumentNullException.ThrowIfNull(culture);

        var set = _resources.GetResourceSet(culture, true, true);

        if (set is null) return [];

        var keys = new List<string>();

        foreach (DictionaryEntry entry in set)
            if (entry.Key is string key)
                keys.Add(key);

        keys.Sort(StringComparer.Ordinal);

        return keys;
    }

    /// <summary>
    ///     Applies a stored language tag to the running process. Passing an empty tag clears the
    ///     override so the app follows the Windows display language again.
    /// </summary>
    /// <returns>True when the effective culture changed, which means the interface needs a reload.</returns>
    public static bool UseLanguage(string? tag)
    {
        var before = CultureInfo.CurrentUICulture.Name;

        CultureInfo.CurrentUICulture = ResolveCulture(tag);

        return !string.Equals(before, CultureInfo.CurrentUICulture.Name, StringComparison.Ordinal);
    }

    /// <summary>
    ///     Maps a stored tag onto a culture, falling back to the invariant culture when Windows has
    ///     never heard of the language.
    /// </summary>
    public static CultureInfo ResolveCulture(string? tag)
    {
        if (string.IsNullOrWhiteSpace(tag)) return CultureInfo.InstalledUICulture;

        try
        {
            return new CultureInfo(tag);
        }
        catch (CultureNotFoundException)
        {
            return CultureInfo.InstalledUICulture;
        }
    }
}