using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Media;
using SkiaSharp;

namespace Known.Services;

/// <summary>
/// Copies colours out of the live XAML theme so the Skia based charts can use them.
/// </summary>
/// <remarks>
/// SkiaSharp has no notion of a theme resource, so a chart either hardcodes a colour and breaks in
/// dark mode, or reads the resolved brush at build time. The brushes declared in App.xaml are
/// declared against theme resources, so they already carry the right colour for the current theme,
/// but a Skia colour is a snapshot, which is why the chart pages rebuild their series whenever
/// <see cref="ThemeService.ThemeChanged"/> fires.
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

    private static SKColor Read(string key, SKColor fallback) =>
        Application.Current?.Resources.TryGetValue(key, out var value) == true
        && value is SolidColorBrush brush
            ? new SKColor(brush.Color.R, brush.Color.G, brush.Color.B, (byte)Math.Round(brush.Opacity * 255))
            : fallback;
}
