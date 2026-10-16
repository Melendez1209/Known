using System.Globalization;
using Known.Core.Localization;
using Xunit;

namespace Known.Core.Tests;

/// <summary>
/// Checks that the .resx files generated from the Android string resources are actually embedded
/// and resolve, so a broken generation step cannot silently fall back to keys in the interface.
/// </summary>
public class StringLocalizerTests
{
    private static readonly StringLocalizer Localizer = new();

    /// <summary>
    /// Languages that have a satellite assembly of their own. en-US is deliberately absent: it is
    /// the neutral resource set, so it has nothing to compare against.
    /// </summary>
    private static readonly string[] SatelliteTags = ["en-GB", "zh-Hans", "zh-Hant"];

    [Fact]
    public void NeutralResourcesResolve()
    {
        Assert.Equal("History", Localizer.Get("history", CultureInfo.InvariantCulture));
        Assert.Equal("Home", Localizer.Get("home", CultureInfo.InvariantCulture));
    }

    [Fact]
    public void SimplifiedChineseSatelliteResolves()
    {
        var chinese = StringLocalizer.ResolveCulture("zh-Hans");

        Assert.Equal("历史", Localizer.Get("history", chinese));
        Assert.Equal("首页", Localizer.Get("home", chinese));
        Assert.Equal("知学情", Localizer.Get("app_name", chinese));
    }

    [Fact]
    public void TraditionalChineseSatelliteResolves()
    {
        var chinese = StringLocalizer.ResolveCulture("zh-Hant");

        Assert.Equal("歷史", Localizer.Get("history", chinese));
        Assert.Equal("首頁", Localizer.Get("home", chinese));
        Assert.Equal("知學情", Localizer.Get("app_name", chinese));
    }

    [Fact]
    public void TheTwoChineseVariantsAreActuallyDifferent()
    {
        var simplified = Localizer.Get("history", StringLocalizer.ResolveCulture("zh-Hans"));
        var traditional = Localizer.Get("history", StringLocalizer.ResolveCulture("zh-Hant"));

        Assert.NotEqual(simplified, traditional);
    }

    [Fact]
    public void BritishEnglishSatelliteResolves()
    {
        Assert.Equal("History", Localizer.Get("history", StringLocalizer.ResolveCulture("en-GB")));
    }

    [Fact]
    public void AnUnknownKeyFallsBackToItsOwnName()
    {
        Assert.Equal("no_such_key", Localizer.Get("no_such_key", CultureInfo.InvariantCulture));
    }

    [Fact]
    public void AnEmptyKeyResolvesToAnEmptyString()
    {
        Assert.Equal(string.Empty, Localizer.Get(string.Empty, CultureInfo.InvariantCulture));
    }

    [Theory]
    [InlineData("en-GB")]
    [InlineData("zh-Hans")]
    [InlineData("zh-Hant")]
    public void EverySatelliteCoversEveryNeutralKeyExceptTheUntranslatableOne(string tag)
    {
        var neutral = Localizer.Keys(CultureInfo.InvariantCulture);
        var present = Localizer.Keys(StringLocalizer.ResolveCulture(tag)).ToHashSet(StringComparer.Ordinal);
        var missing = neutral.Where(key => !present.Contains(key)).ToList();

        // README is marked translatable="false" on Android, so it only exists in the neutral file
        // and every satellite falls back to it at lookup time
        Assert.NotEmpty(neutral);
        Assert.Equal(["readme"], missing);
    }

    [Fact]
    public void ASatelliteFallsBackToTheNeutralFileForTheUntranslatableKeys()
    {
        var british = StringLocalizer.ResolveCulture("en-GB");

        Assert.Equal("README", Localizer.Get("readme", british));
    }

    [Fact]
    public void TheOfferedLanguagesAreTheExpectedSet()
    {
        Assert.Equal(
            [string.Empty, "en-US", "en-GB", "zh-Hans", "zh-Hant"],
            StringLocalizer.AvailableLanguages.Select(language => language.Tag));

        Assert.All(
            StringLocalizer.AvailableLanguages,
            language => Assert.False(string.IsNullOrWhiteSpace(language.EnglishName)));
    }

    [Fact]
    public void EveryOfferedTagResolvesToARealCulture()
    {
        Assert.All(
            StringLocalizer.AvailableLanguages.Where(language => language.Tag.Length > 0),
            language => Assert.NotNull(language.Culture));
    }

    [Fact]
    public void AnUnknownLanguageFallsBackToTheInstalledCulture()
    {
        var resolved = StringLocalizer.ResolveCulture("!!not a tag!!");

        Assert.Equal(CultureInfo.InstalledUICulture.Name, resolved.Name);
    }
}
