using System.Globalization;
using Known.Core.Localization;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The settings root. The desktop build keeps only the settings that mean anything on Windows:
///     the student profile, the theme and language, and the cache.
/// </summary>
public sealed partial class SettingsPage : Page
{
    private readonly StringLocalizer _strings = new();

    /// <summary>Creates the page and fills in its labels.</summary>
    public SettingsPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("settings");
        GeneralEntry.Content = _strings.Get("general_settings");
        AppearanceEntry.Content = _strings.Get("look_and_feel");
        ClearCacheEntry.Content = _strings.Get("clear_cache");
    }

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        switch (e.ClickedItem)
        {
            case ListViewItem when ReferenceEquals(e.ClickedItem, GeneralEntry):
                this.Frame.Navigate(typeof(GeneralSettingsPage));
                break;

            case ListViewItem when ReferenceEquals(e.ClickedItem, AppearanceEntry):
                this.Frame.Navigate(typeof(AppearancePage));
                break;

            case ListViewItem when ReferenceEquals(e.ClickedItem, ClearCacheEntry):
                _ = ClearCacheAsync();
                break;
        }
    }

    /// <summary>
    ///     Removes the application's cache directory and reports how much was freed, mirroring the
    ///     Android settings entry.
    /// </summary>
    private async Task ClearCacheAsync()
    {
        var directory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Known",
            "Cache");

        long freed = 0;

        try
        {
            if (Directory.Exists(directory))
            {
                foreach (var file in Directory.EnumerateFiles(directory, "*", SearchOption.AllDirectories))
                    try
                    {
                        freed += new FileInfo(file).Length;
                    }
                    catch (IOException)
                    {
                        // A file held open by another process is simply not counted
                    }

                Directory.Delete(directory, true);
            }
        }
        catch (IOException)
        {
            // The cache is disposable, so a failure to clear it is reported rather than fatal
        }

        await ShowMessageAsync(_strings.Get("cache_freed").Replace("%1$s", FormatSize(freed)));
    }

    private async Task ShowMessageAsync(string message)
    {
        var dialog = new ContentDialog
        {
            Content = message,
            CloseButtonText = _strings.Get("done"),
            XamlRoot = XamlRoot
        };

        await dialog.ShowAsync();
    }

    /// <summary>Formats a byte count the same way the Android settings entry does.</summary>
    internal static string FormatSize(long bytes)
    {
        return bytes switch
        {
            >= 1024 * 1024 * 1024 => string.Format(CultureInfo.InvariantCulture, "%.2f GB",
                bytes / (1024.0 * 1024 * 1024)),
            >= 1024 * 1024 => string.Format(CultureInfo.InvariantCulture, "%.2f MB", bytes / (1024.0 * 1024)),
            >= 1024 => string.Format(CultureInfo.InvariantCulture, "%.2f KB", bytes / 1024.0),
            _ => string.Format(CultureInfo.InvariantCulture, "%d B", bytes)
        };
    }
}

