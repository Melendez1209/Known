package com.melendez.known.util

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService
import java.io.File
import java.util.Locale

/**
 * Clears the app's cache directory.
 *
 * @param context The application context.
 * @return The size of freed cache space in bytes.
 */
fun clearCache(context: Context): Long {
    val cacheDir = context.cacheDir
    val externalCacheDir = context.externalCacheDir
    var freedSize = 0L

    freedSize += deleteDir(cacheDir)
    externalCacheDir?.let { freedSize += deleteDir(it) }

    return freedSize
}

/**
 * Returns the total size of the app's cache directory without deleting anything.
 *
 * @param context The application context.
 * @return The total cache size in bytes.
 */
fun getCacheSize(context: Context): Long {
    val cacheDir = context.cacheDir
    val externalCacheDir = context.externalCacheDir
    var size = deleteDirSize(cacheDir)
    externalCacheDir?.let { size += deleteDirSize(it) }
    return size
}

private fun deleteDir(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0L
    var size = 0L
    val files = dir.walkBottomUp().filter { it != dir }.toList()
    for (file in files) {
        if (file.isFile) {
            size += file.length()
            file.delete()
        } else if (file.isDirectory) {
            file.delete()
        }
    }
    dir.delete()
    return size
}

private fun deleteDirSize(dir: File?): Long {
    if (dir == null || !dir.exists()) return 0L
    var size = 0L
    val files = dir.walkBottomUp().filter { it != dir }.toList()
    for (file in files) {
        if (file.isFile) {
            size += file.length()
        }
    }
    return size
}

/**
 * Formats a byte size into a human-readable string using US locale for consistency.
 */
fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format(
            Locale.US,
            "%.2f GB",
            bytes / (1024.0 * 1024 * 1024)
        )

        bytes >= 1024 * 1024 -> String.format(
            Locale.US,
            "%.2f MB",
            bytes / (1024.0 * 1024)
        )

        bytes >= 1024 -> String.format(
            Locale.US,
            "%.2f KB",
            bytes / 1024.0
        )

        else -> String.format(Locale.US, "%d B", bytes)
    }
}


/**
 * Retrieves the city name based on the device's last known location.
 *
 * This function uses the LocationManager to get the last known location from the network provider.
 * It then uses a Geocoder to reverse geocode the location's latitude and longitude into a
 * human-readable address, from which it extracts the locality (city name).
 *
 * If the location cannot be determined or the geocoder fails to find an address, an empty string
 * will be returned.
 *
 * @param context The application context, used to access system services like LocationManager and Geocoder.
 * @return The name of the city as a String, or an empty string if it cannot be determined.
 * @throws SecurityException if the required location permissions (ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION) are not granted.
 */
@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Suppress("DEPRECATION")
fun getCityName(context: Context): String {
    val locationManager = getSystemService(context, LocationManager::class.java)
    val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    val city = location?.let { getCityNameFromGeocoder(Geocoder(context), it) } ?: ""
    Log.d("Melendez", "getCityName: city:$city")
    return city
}

/**
 * Reverse geocodes the given [location] and extracts the city name from the nearest address.
 *
 * @param geocoder The Geocoder used to translate coordinates into an address.
 * @param location The device location to reverse geocode.
 * @return The name of the city as a String, or an empty string if it cannot be determined.
 */
@Suppress("DEPRECATION")
internal fun getCityNameFromGeocoder(geocoder: Geocoder, location: Location): String {
    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
    return addresses?.getOrNull(0)?.locality ?: ""
}
