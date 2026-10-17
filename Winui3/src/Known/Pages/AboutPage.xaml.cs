using System.Diagnostics;
using Known.Core.Localization;
using Microsoft.UI.Xaml.Controls;

namespace Known.Pages;

/// <summary>
///     The about page. Every row opens a link in the system browser, which is the Windows equivalent
///     of the Android build firing an ACTION_VIEW intent.
/// </summary>
public sealed partial class AboutPage : KnownPage
{
    private const string RepositoryUrl = "https://github.com/Melendez1209/Known";
    private const string ReleaseUrl = RepositoryUrl + "/releases";
    private const string IssueUrl = RepositoryUrl + "/issues";
    private readonly StringLocalizer _strings = new();

    /// <summary>Creates the page and fills in its labels.</summary>
    public AboutPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("about");
        ReadmeEntry.Content = _strings.Get("readme");
        ReleaseEntry.Content = _strings.Get("release");
        IssueEntry.Content = _strings.Get("github_issue");
    }

    private void OnItemClick(object sender, ItemClickEventArgs e)
    {
        var url = e.ClickedItem switch
        {
            _ when ReferenceEquals(e.ClickedItem, ReadmeEntry) => RepositoryUrl,
            _ when ReferenceEquals(e.ClickedItem, ReleaseEntry) => ReleaseUrl,
            _ when ReferenceEquals(e.ClickedItem, IssueEntry) => IssueUrl,
            _ => null
        };

        if (url is null) return;

        // Launching must not be allowed to take the app down when no browser is registered
        try
        {
            Process.Start(new ProcessStartInfo(url) { UseShellExecute = true });
        }
        catch (Exception)
        {
        }
    }
}