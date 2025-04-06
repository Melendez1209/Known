package com.melendez.known.core

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService


@Suppress("DEPRECATION")
fun getCityName(context: Context): String {
    // Check if the permission has been granted
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // If the permission has not been granted, request it
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            1
        )
        return ""
    } else {
        // If the permission has been granted, get the city name
        // Get an instance of LocationManager
        val locationManager = getSystemService(context, LocationManager::class.java)
        // Get the device's last known location
        val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        // Create an instance of Geocoder
        val geocoder = Geocoder(context)
        // Get the city address information
        val addresses = location?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
        // Return the city name
        val city = addresses?.get(0)?.locality ?: ""
        Log.d("Melendez", "getCityName: city:$city")
        return city
    }
}