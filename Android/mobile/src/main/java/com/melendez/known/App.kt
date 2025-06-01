package com.melendez.known

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.melendez.known.util.AppOpenAdManager
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class App : Application() {
    private val TAG = "KnownApp"
    lateinit var appOpenAdManager: AppOpenAdManager

    // Application-level CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Flag to track database initialization status
    @Volatile
    private var isDatabaseInitialized = false

    override fun onCreate() {

        super.onCreate()

        // Initialize database synchronously with timeout to prevent blocking too long
        initializeDatabase()

        // Initialize ads in background ONLY after database is ready
        applicationScope.launch {
            try {
                Log.d(TAG, "Database is ready, initializing ads")
                MobileAds.initialize(this@App) {}
                appOpenAdManager = AppOpenAdManager(this@App)
                appOpenAdManager.loadAd(this@App)
                Log.d(TAG, "Ad initialization completed")
            } catch (e: Exception) {
                Log.e(TAG, "Ad initialization failed: ${e.message}", e)
            }
        }
    }

    private fun initializeDatabase() {
        try {
            // Use runBlocking with timeout to ensure database is initialized before UI starts
            runBlocking {
                withContext(Dispatchers.IO) {
                    val settingsRepository = PreferenceUtil(this@App).repository
                    settingsRepository.initializeSettings()

                    isDatabaseInitialized = true
                    Log.d(TAG, "Database and settings initialization successful")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during database initialization: ${e.message}", e)
        }
    }
} 