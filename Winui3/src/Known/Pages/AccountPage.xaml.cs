using Known.Core.Localization;
using Known.Services;
using Microsoft.UI.Xaml;
using Microsoft.UI.Xaml.Controls;
using Microsoft.UI.Xaml.Input;

namespace Known.Pages;

/// <summary>
///     The account tab: who is signed in, plus the way into the settings, the about page and the
///     favourites list.
/// </summary>
public sealed partial class AccountPage
{
    private readonly StringLocalizer _strings = new();

    /// <summary>Creates the page and fills in the account card.</summary>
    public AccountPage()
    {
        InitializeComponent();

        TitleText.Text = _strings.Get("me");
        SettingsEntry.Content = _strings.Get("settings");
        AboutEntry.Content = _strings.Get("about");
        FavouritesEntry.Content = _strings.Get("favourites");

        ApplyAccountState(AppServices.Auth.State);
        AppServices.Auth.StateChanged += OnAuthStateChanged;
    }

    /// <inheritdoc />
    public override void OnNavigatedAway()
    {
        AppServices.Auth.StateChanged -= OnAuthStateChanged;
        base.OnNavigatedAway();
    }

    private void OnAuthStateChanged(object? sender, AccountState state)
    {
        ApplyAccountState(state);
    }

    private void ApplyAccountState(AccountState state)
    {
        AccountName.Text = state.IsSignedIn
            ? state.DisplayName ?? _strings.Get("sign_to")
            : _strings.Get("account_not_signed_in");

        AccountEmail.Text = state.Email ?? string.Empty;

        // One button serves both directions, because the Windows build only has Microsoft accounts
        // and so has nothing to choose between
        AuthButton.Content = state.IsSignedIn
            ? _strings.Get("sign_out")
            : _strings.Get("microsoft_sign_in");

        AuthButton.IsEnabled = state.IsSignedIn || AppServices.Auth.IsConfigured;

        if (!state.IsSignedIn)
        {
            Avatar.ProfilePicture = null;
            Avatar.DisplayName = string.Empty;
            return;
        }

        // MSAL does not return a photo, so the person control falls back to its own initials
        Avatar.DisplayName = state.DisplayName ?? string.Empty;
    }

    private async void OnAuthTapped(object sender, RoutedEventArgs e)
    {
        if (!AppServices.Auth.IsConfigured)
        {
            await ShowMessageAsync(_strings.Get("attention"));
            return;
        }

        try
        {
            if (AppServices.Auth.State.IsSignedIn)
                AppServices.Auth.SignOut();
            else
                await AppServices.Auth.SignInAsync();
        }
        catch (Exception)
        {
            // A cancelled or failed interactive sign in is an ordinary outcome, not a crash
            await ShowMessageAsync(_strings.Get("attention"));
        }
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

    private void OnSettings(object sender, TappedRoutedEventArgs e)
    {
        Shell.NavigateRoot(typeof(SettingsPage));
    }

    private void OnAbout(object sender, TappedRoutedEventArgs e)
    {
        Shell.NavigateRoot(typeof(AboutPage));
    }

    private void OnFavourites(object sender, TappedRoutedEventArgs e)
    {
        Shell.NavigateInTab(typeof(HistoryPage));
    }
}