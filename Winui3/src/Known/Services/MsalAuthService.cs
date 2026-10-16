using Microsoft.Identity.Client;

namespace Known.Services;

/// <summary>Who is currently signed in.</summary>
/// <param name="IsSignedIn">Whether an account is held.</param>
/// <param name="DisplayName">Name shown next to the avatar.</param>
/// <param name="Email">Sign in address.</param>
/// <param name="AvatarUri">Profile picture, when the account published one.</param>
public sealed record AccountState(
    bool IsSignedIn,
    string? DisplayName,
    string? Email,
    string? AvatarUri)
{
    /// <summary>The state before anybody has signed in.</summary>
    public static AccountState SignedOut { get; } = new(false, null, null, null);
}

/// <summary>
///     Signs the student in with a Microsoft account.
/// </summary>
/// <remarks>
///     The Android build signed in against Firebase, with Google and GitHub as extra providers. None
///     of that carries over to a desktop app, so the desktop build talks to Microsoft identity
///     directly. Registration is still required: <see cref="ClientId" /> has to be filled in with the
///     application registered in the tenant before this can sign anybody in.
/// </remarks>
public sealed class MsalAuthService
{
    /// <summary>
    ///     Application id of the Entra registration. Empty until the app is registered, in which case
    ///     <see cref="IsConfigured" /> stays false and the sign in button explains why.
    /// </summary>
    public const string ClientId = "";

    /// <summary>The scopes requested, the minimum that identifies the signed in person.</summary>
    private static readonly string[] Scopes = ["User.Read"];

    private IPublicClientApplication? _application;
    private AccountState _state = AccountState.SignedOut;

    /// <summary>Who is currently signed in.</summary>
    public AccountState State
    {
        get => _state;
        private set
        {
            if (_state == value) return;

            _state = value;
            StateChanged?.Invoke(this, value);
        }
    }

    /// <summary>Whether an Entra registration has been supplied.</summary>
    public bool IsConfigured => !string.IsNullOrWhiteSpace(ClientId);

    private IPublicClientApplication Application =>
        _application ??= PublicClientApplicationBuilder
            .Create(ClientId)
            .WithRedirectUri("https://login.microsoftonline.com/common/oauth2/nativeclient")
            .Build();

    /// <summary>Raised whenever the signed in account changes.</summary>
    public event EventHandler<AccountState>? StateChanged;

    /// <summary>Starts the interactive sign in, returning the account that came back.</summary>
    public async Task<AccountState> SignInAsync(CancellationToken cancellationToken = default)
    {
        if (!IsConfigured)
            throw new InvalidOperationException(
                "No Entra application id has been configured for this build.");

        var result = await Application
            .AcquireTokenInteractive(Scopes)
            .WithPrompt(Prompt.SelectAccount)
            .ExecuteAsync(cancellationToken)
            .ConfigureAwait(true);

        return State = new AccountState(
            true,
            result.Account?.Username,
            result.Account?.Username,
            null);
    }

    /// <summary>
    ///     Drops the held account. The MSAL token cache is left alone on purpose, so signing back in
    ///     with the same account does not prompt the student again.
    /// </summary>
    public void SignOut()
    {
        State = AccountState.SignedOut;
    }
}