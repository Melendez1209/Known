using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
using SkiaSharp;

namespace Known.Services;

/// <summary>
///     Copies colours out of the live XAML theme so the Skia based charts can use them.
/// </summary>
/// <remarks>
///     SkiaSharp has no notion of a theme resource, so a chart either hardcodes a colour and breaks in
///     dark mode, or reads the resolved brush at build time. The brushes declared in App.xaml are
///     declared against theme resources, so they already carry the right colour for the current theme,
///     but a Skia colour is a snapshot, which is why the chart pages rebuild their series whenever
///     <see cref="ThemeService.ThemeChanged" /> fires.
/// </remarks>
public static class ChartPalette
{
    /// <summary>Accent used for the primary series, which is the subject of the page.</summary>
    public static SKColor Accent { get; private set; } = SKColors.DodgerBlue;

    /// <summary>Accent used for a secondary series, such as an average line.</summary>
    public static SKColor AccentSecondary { get; private set; } = SKColors.OrangeRed;

    /// <summary>Accent used for a third series, such as the per subject breakdown.</summary>
    public static SKColor AccentTertiary { get; private set; } = SKColors.MediumSeaGreen;

    /// <summary>Colour for axis labels and the legend.</summary>
    public static SKColor Text { get; private set; } = SKColors.Black;

    /// <summary>Colour for the faint separator lines behind the plot.</summary>
    public static SKColor GridLine { get; private set; } = SKColors.LightGray;

    /// <summary>Colour for the card the chart sits on, so a transparent plot matches its host.</summary>
    public static SKColor Surface { get; private set; } = SKColors.White;

    /// <summary>
    /// Fixed categorical palette, one entry per subject in
    /// <see cref="Core.Domain.SubjectCatalogue.ExamSubjectKeys"/>.
    /// </summary>
    /// <remarks>
    /// These are deliberately mid-tone rather than the usual vivid hues: the same slice has to stay
    /// legible on both the light card and the dark one, and a saturated yellow or lime disappears
    /// against white. Indexing by subject rather than by position in the legend means a subject
    /// keeps its colour across every chart in the app, so a colour can be read at a glance.
    /// </remarks>
    private static readonly SKColor[] CategoricalColours =
    [
        new(0x3B, 0x6E, 0xA5), // blue
        new(0xE0, 0x7B, 0x39), // orange
        new(0x4E, 0x9A, 0x4E), // green
        new(0xD1, 0x49, 0x5B), // red
        new(0x9B, 0x6B, 0xB0), // violet
        new(0xC9, 0x9A, 0x12), // gold
        new(0x3F, 0xA3, 0x96), // teal
        new(0xE0, 0x77, 0x9B), // pink
        new(0x8C, 0x6D, 0x52), // brown
        new(0x6B, 0x7C, 0x8D) // slate
    ];

    /// <summary>The categorical palette, indexed by subject position.</summary>
    public static IReadOnlyList<SKColor> Categorical => CategoricalColours;

    /// <summary>
    /// The palette entry for a subject, wrapping round if an unknown subject is asked for. A
    /// negative index wraps too rather than throwing, so callers never have to guard.
    /// </summary>
    public static SKColor CategoricalAt(int index)
    {
        var count = CategoricalColours.Length;

        if (count == 0)
        {
            return Accent;
        }

        var position = index % count;

        return CategoricalColours[position < 0 ? position + count : position];
    }

    /// <summary>Re-reads every brush from the application resources.</summary>
    public static void Refresh()
    {
        Accent = Read("ChartAccentBrush", SKColors.DodgerBlue);
        AccentSecondary = Read("ChartAccentSecondaryBrush", SKColors.OrangeRed);
        AccentTertiary = Read("ChartAccentTertiaryBrush", SKColors.MediumSeaGreen);
        Text = Read("ChartTextBrush", SKColors.Black);
        GridLine = Read("ChartGridLineBrush", SKColors.LightGray);
        Surface = Read("ChartSurfaceBrush", SKColors.White);
    }

    private static SKColor Read(string key, SKColor fallback)
    {
        return Application.Current?.Resources.TryGetValue(key, out var value) == true
               && value is SolidColorBrush brush
            ? new SKColor(brush.Color.R, brush.Color.G, brush.Color.B, (byte)Math.Round(brush.Opacity * 255))
            : fallback;
    }
}