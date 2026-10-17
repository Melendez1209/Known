using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Media;

namespace Known.Pages;

/// <summary>
///     The two star glyphs, taken from Segoe Fluent Icons: the filled one means the exam is pinned and
///     the outline one means it is not. They live here so every button offering the same choice looks
///     the same, and so the code points are spelled out as numbers. The characters themselves sit in
///     the private use area, which makes them invisible in an editor and in a diff, and a stray one is
///     indistinguishable from the next code point along: the outline star here was once a digit out
///     from the star that was meant.
/// </summary>
internal static class FavoriteIcons
{
    /// <summary>The font the two glyphs belong to.</summary>
    private static readonly FontFamily IconFont = new("Segoe Fluent Icons");

    /// <summary>Star outline, shown while the exam is not a favourite.</summary>
    internal static readonly string OutlineGlyph = ((char)0xE734).ToString();

    /// <summary>Filled star, shown while the exam is a favourite.</summary>
    internal static readonly string FilledGlyph = ((char)0xE735).ToString();

    /// <summary>The glyph matching a given state.</summary>
    internal static string GlyphFor(bool isFavorite) => isFavorite ? FilledGlyph : OutlineGlyph;

    /// <summary>
    ///     A ready made icon, because an AppBarButton takes an element rather than a bare glyph.
    /// </summary>
    internal static FontIcon IconFor(bool isFavorite) => new()
    {
        Glyph = GlyphFor(isFavorite),
        FontFamily = IconFont
    };
}
