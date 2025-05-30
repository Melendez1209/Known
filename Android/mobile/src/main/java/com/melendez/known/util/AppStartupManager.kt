package com.melendez.known.util

import android.util.Log

/**
 * Manages app startup sequence to avoid race conditions between
 * database initialization, UI loading, and ad display
 */
object AppStartupManager {
    private const val TAG = "AppStartupManager"

    @Volatile
    private var isDatabaseReady = false

    @Volatile
    private var isMainUIReady = false

    @Volatile
    private var areAdsInitialized = false

    /**
     * Mark database as ready
     */
    fun markDatabaseReady() {
        isDatabaseReady = true
        Log.d(TAG, "Database marked as ready")
        checkForAdDisplay()
    }

    /**
     * Mark main UI as ready
     */
    fun markMainUIReady() {
        isMainUIReady = true
        Log.d(TAG, "Main UI marked as ready")
        checkForAdDisplay()
    }

    /**
     * Mark ads as initialized
     */
    fun markAdsInitialized() {
        areAdsInitialized = true
        Log.d(TAG, "Ads marked as initialized")
    }

    /**
     * Check if it's safe to display ads
     */
    fun isSafeToDisplayAds(): Boolean {
        val safe = isDatabaseReady && isMainUIReady && areAdsInitialized
        if (!safe) {
            Log.d(
                TAG,
                "Not safe to display ads yet - DB: $isDatabaseReady, UI: $isMainUIReady, Ads: $areAdsInitialized"
            )
        }
        return safe
    }

    private fun checkForAdDisplay() {
        if (isSafeToDisplayAds()) {
            Log.d(TAG, "All components ready - ads can be displayed safely")
        }
    }
} 