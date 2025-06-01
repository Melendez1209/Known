package com.melendez.known.util

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat.getSystemService


@RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
@Suppress("DEPRECATION")
fun getCityName(context: Context): String {
    // Get an instance of LocationManager
    val locationManager = getSystemService(context, LocationManager::class.java)
    // Get the device's last known location
    val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    // Create an instance of Geocoder
    val geocoder = Geocoder(context)
    // Get the city address information
    val addresses = location?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
    val city = addresses?.get(0)?.locality ?: ""
    Log.d("Melendez", "getCityName: city:$city")
    return city
}