using System.Globalization;
using System.Text.Json;
using Windows.Devices.Geolocation;

namespace Known.Services;

/// <summary>
///     Works out the city the student is in, for the region field on the onboarding wizard and on the
///     general settings page.
/// </summary>
/// <remarks>
///     <para>
///         Windows has no offline reverse geocoder: <c>Windows.Devices.Geolocation</c> resolves
///         coordinates and stops there, and the <c>Geocoder</c> that used to turn them into an
///         address was a UWP era API that no longer exists on the desktop. A position therefore has to
///         be turned into a place name by a web service.
///     </para>
///     <para>
///         BigDataCloud's client side endpoint is used because it needs no API key and no account,
///         which keeps a secret out of a project whose rule is that secrets never enter the build.
///     </para>
/// </remarks>
public sealed class LocationService
{
    /// <summary>
    ///     How long the geocoding request is given before it is abandoned. The endpoint is normally
    ///     quick, so a long wait helps nobody and leaves the button spinning.
    /// </summary>
    private static readonly TimeSpan ReverseGeocodeTimeout = TimeSpan.FromSeconds(10);

    private static readonly HttpClient Client = new() { Timeout = ReverseGeocodeTimeout };

    /// <summary>
    ///     Asks for the position and resolves it to a city name.
    /// </summary>
    /// <returns>
    ///     A result carrying either the city, or the reason there is none. Failure is a normal
    ///     outcome rather than an exception: the student may decline the prompt, have location
    ///     services switched off, or simply be offline, and in every case they can still type the
    ///     region by hand.
    /// </returns>
    public async Task<LocationResult> DetectCityAsync()
    {
        var access = await Geolocator.RequestAccessAsync();

        if (access != GeolocationAccessStatus.Allowed) return LocationResult.PermissionDenied;

        var position = await new Geolocator().GetGeopositionAsync();
        var point = position.Coordinate.Point.Position;

        var city = await ReverseGeocodeAsync(point.Latitude, point.Longitude);

        // TODO Explain in the about page that the coordinates leave the machine to be resolved into a
        //  city name, and are never stored. The audience is schoolchildren, so it has to be said plainly
        return string.IsNullOrWhiteSpace(city)
            ? LocationResult.NoCity
            : LocationResult.ForCity(city);
    }

    /// <summary>
    ///     Turns a pair of coordinates into the most city-like name the endpoint will give back.
    /// </summary>
    private async Task<string?> ReverseGeocodeAsync(double latitude, double longitude)
    {
        var language = CultureInfo.CurrentUICulture.TwoLetterISOLanguageName;

        var url =
            $"https://api.bigdatacloud.net/data/reverse-geocode-client?latitude={latitude.ToString(CultureInfo.InvariantCulture)}" +
            $"&longitude={longitude.ToString(CultureInfo.InvariantCulture)}" +
            $"&localityLanguage={language}";

        using var response = await Client.GetAsync(url);

        // A non success status is left to throw, so a rate limited or offline endpoint is reported
        // as a failure rather than silently resolving to an empty place name
        response.EnsureSuccessStatusCode();

        var json = await response.Content.ReadAsStringAsync();

        return ReadCityName(json);
    }

    /// <summary>
    ///     Picks the most useful name out of the endpoint's response. It offers several levels of
    ///     administrative area at once, and which one counts as the city varies by country: London
    ///     arrives as <c>city</c>, some Chinese cities as <c>locality</c>, and rural districts often
    ///     only as a subdivision.
    /// </summary>
    private static string? ReadCityName(string json)
    {
        using var document = JsonDocument.Parse(json);

        if (document.RootElement.ValueKind != JsonValueKind.Object) return null;

        return FirstNonBlank(document.RootElement, "city", "locality", "principalSubdivision", "countryName");
    }

    /// <summary>Returns the first of the named properties that holds anything other than whitespace.</summary>
    private static string? FirstNonBlank(JsonElement element, params string[] names)
    {
        foreach (var name in names)
        {
            if (!element.TryGetProperty(name, out var property)) continue;
            if (property.ValueKind != JsonValueKind.String) continue;

            var value = property.GetString();

            if (!string.IsNullOrWhiteSpace(value)) return value;
        }

        return null;
    }
}

/// <summary>What a location lookup produced: a city, or the reason there is not one.</summary>
public sealed record LocationResult
{
    private LocationResult(string? city, string? message)
    {
        City = city;
        Message = message;
    }

    /// <summary>The city name, or null when the lookup did not produce one.</summary>
    public string? City { get; }

    /// <summary>Why there is no city, for the message shown under the button.</summary>
    public string? Message { get; }

    /// <summary>Whether a city was found.</summary>
    public bool HasCity => !string.IsNullOrWhiteSpace(City);

    /// <summary>The student declined the location prompt, or Windows refused it.</summary>
    public static LocationResult PermissionDenied { get; } =
        new(null, "location_permission_denied");

    /// <summary>The position resolved, but the geocoder had no name for it.</summary>
    public static LocationResult NoCity { get; } = new(null, "location_no_data");

    /// <summary>The lookup succeeded.</summary>
    public static LocationResult ForCity(string city)
    {
        return new LocationResult(city, null);
    }
}
