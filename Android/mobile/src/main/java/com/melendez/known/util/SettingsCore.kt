package com.melendez.known.util

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService


/**
 * Retrieves the city name based on the device's last known location.
 *
 * This function uses the `LocationManager` to get the last known location from the network provider.
 * It then uses a `Geocoder` to reverse geocode the location's latitude and longitude into a
 * human-readable address, from which it extracts the locality (city name).
 *
 * If the location cannot be determined or the geocoder fails to find an address, an empty string
 * will be returned.
 *
 * @param context The application context, used to access system services like `LocationManager` and `Geocoder`.
 * @return The name of the city as a [String], or an empty string if it cannot be determined.
 * @throws SecurityException if the required location permissions (`ACCESS_FINE_LOCATION` or `ACCESS_COARSE_LOCATION`) are not granted.
 */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Suppress("DEPRECATION")
fun getCityName(context: Context): String {
    // Get an instance of LocationManager
    val locationManager = getSystemService(context, LocationManager::class.java)
    // Get the device's last known location
    val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    // Reverse geocode the location into a city name
    val city = location?.let { getCityNameFromGeocoder(Geocoder(context), it) } ?: ""
    Log.d("Melendez", "getCityName: city:$city")
    return city
}

/**
 * Reverse geocodes the given [location] and extracts the city name from the nearest address.
 *
 * @param geocoder The [Geocoder] used to translate coordinates into an address.
 * @param location The device location to reverse geocode.
 * @return The name of the city as a [String], or an empty string if it cannot be determined.
 */
internal fun getCityNameFromGeocoder(geocoder: Geocoder, location: Location): String {
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return addresses?.getOrNull(0)?.locality ?: ""
}