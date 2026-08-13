package com.melendez.known.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.Mockito
import java.io.IOException

@Suppress("DEPRECATION")
class SettingsCoreKtTest {

    /**
     * Resolves [Context] system services to the given [locationManager] and swallows [Log] calls
     * while running [block].
     */
    private fun <T> withLocationService(
        context: Context,
        locationManager: LocationManager?,
        block: () -> T
    ): T {
        val log = Mockito.mockStatic(Log::class.java)
        val contextCompat = Mockito.mockStatic(ContextCompat::class.java)
        try {
            log.`when`<Int> { Log.d(Mockito.anyString(), Mockito.anyString()) }.thenReturn(0)
            contextCompat.`when`<LocationManager?> {
                ContextCompat.getSystemService(context, LocationManager::class.java)
            }.thenReturn(locationManager)
            return block()
        } finally {
            contextCompat.close()
            log.close()
        }
    }

    private fun mockLocation(latitude: Double, longitude: Double): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.latitude).thenReturn(latitude)
        Mockito.`when`(location.longitude).thenReturn(longitude)
        return location
    }

    private fun mockAddress(locality: String?): Address {
        val address = Mockito.mock(Address::class.java)
        Mockito.`when`(address.locality).thenReturn(locality)
        return address
    }

    private fun mockGeocoder(
        latitude: Double,
        longitude: Double,
        response: List<Address>?
    ): Geocoder {
        val geocoder = Mockito.mock(Geocoder::class.java)
        Mockito.`when`(geocoder.getFromLocation(latitude, longitude, 1)).thenReturn(response)
        return geocoder
    }

    // --- getCityName: end-to-end ---

    @Test
    fun `Successful city retrieval`() {
        val context = Mockito.mock(Context::class.java)
        val locationManager = Mockito.mock(LocationManager::class.java)
        val location = mockLocation(31.2304, 121.4737)
        Mockito.`when`(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            .thenReturn(location)

        Mockito.mockConstruction(Geocoder::class.java) { geocoder, _ ->
            val address = mockAddress("Shanghai")
            Mockito.`when`(geocoder.getFromLocation(31.2304, 121.4737, 1))
                .thenReturn(listOf(address))
        }.use {
            withLocationService(context, locationManager) {
                assertEquals("Shanghai", getCityName(context))
            }
        }
    }

    @Test
    fun `No location permissions granted`() {
        val context = Mockito.mock(Context::class.java)
        val locationManager = Mockito.mock(LocationManager::class.java)
        Mockito.`when`(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            .thenThrow(SecurityException("location permission denied"))

        withLocationService(context, locationManager) {
            assertThrows(SecurityException::class.java) { getCityName(context) }
        }
    }

    @Test
    fun `LocationManager returns null`() {
        val context = Mockito.mock(Context::class.java)
        withLocationService(context, null) {
            assertEquals("", getCityName(context))
        }
    }

    @Test
    fun `getLastKnownLocation returns null`() {
        val context = Mockito.mock(Context::class.java)
        val locationManager = Mockito.mock(LocationManager::class.java)
        Mockito.`when`(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))
            .thenReturn(null)

        withLocationService(context, locationManager) {
            assertEquals("", getCityName(context))
        }
    }

    // --- getCityNameFromGeocoder: reverse geocoding ---

    @Test
    fun `Geocoder returns no addresses`() {
        assertEquals(
            "",
            getCityNameFromGeocoder(mockGeocoder(1.0, 2.0, emptyList()), mockLocation(1.0, 2.0))
        )
        assertEquals(
            "",
            getCityNameFromGeocoder(mockGeocoder(1.0, 2.0, null), mockLocation(1.0, 2.0))
        )
    }

    @Test
    fun `Address locality is null`() {
        val geocoder = Mockito.mock(Geocoder::class.java)
        val address = mockAddress(null)
        Mockito.`when`(geocoder.getFromLocation(1.0, 2.0, 1)).thenReturn(listOf(address))
        assertEquals("", getCityNameFromGeocoder(geocoder, mockLocation(1.0, 2.0)))
    }

    @Test
    fun `Address locality is an empty string`() {
        val geocoder = Mockito.mock(Geocoder::class.java)
        val address = mockAddress("")
        Mockito.`when`(geocoder.getFromLocation(1.0, 2.0, 1)).thenReturn(listOf(address))
        assertEquals("", getCityNameFromGeocoder(geocoder, mockLocation(1.0, 2.0)))
    }

    @Test
    fun `Geocoder throws IOException`() {
        val geocoder = Mockito.mock(Geocoder::class.java)
        Mockito.`when`(geocoder.getFromLocation(1.0, 2.0, 1))
            .thenThrow(IOException("geocoder backend unavailable"))
        assertThrows(IOException::class.java) {
            getCityNameFromGeocoder(geocoder, mockLocation(1.0, 2.0))
        }
    }

    @Test
    fun `Location at 0 latitude 0 longitude`() {
        val geocoder = Mockito.mock(Geocoder::class.java)
        val address = mockAddress("Null Island")
        Mockito.`when`(geocoder.getFromLocation(0.0, 0.0, 1)).thenReturn(listOf(address))
        assertEquals("Null Island", getCityNameFromGeocoder(geocoder, mockLocation(0.0, 0.0)))
        Mockito.verify(geocoder).getFromLocation(0.0, 0.0, 1)
    }

    @Test
    fun `Location in a remote area`() {
        val geocoder = Mockito.mock(Geocoder::class.java)
        val address = mockAddress(null)
        Mockito.`when`(geocoder.getFromLocation(1.0, 2.0, 1)).thenReturn(listOf(address))
        assertEquals("", getCityNameFromGeocoder(geocoder, mockLocation(1.0, 2.0)))
    }
}
