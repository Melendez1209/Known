package com.melendez.known

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.melendez.known.core.AppOpenAdManager
import com.melendez.known.data.AppDatabase
import com.melendez.known.util.AppStartupManager
import com.melendez.known.util.DatabaseDiagnostics
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class App : Application() {
    private val TAG = "KnownApp"
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    // Application-level CoroutineScope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Database instance with improved initialization
    private var _database: AppDatabase? = null
    val database: AppDatabase
        get() = _database ?: synchronized(this) {
            _database ?: AppDatabase.getDatabase(this).also { _database = it }
        }

    // Flag to track database initialization status
    @Volatile
    private var isDatabaseInitialized = false

    override fun onCreate() {
        // Ensure app directories exist before super.onCreate() to prepare all paths
        ensureAppDirectories()

        // Enable strict mode for debugging
        try {
            // Use reflection to check BuildConfig.DEBUG to avoid direct reference to potentially non-existent BuildConfig
            val debugField =
                Class.forName("com.melendez.known.BuildConfig").getDeclaredField("DEBUG")
            val isDebug = debugField.getBoolean(null)

            if (isDebug) {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .detectAll()
                        .penaltyLog()
                        .build()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to set StrictMode: ${e.message}")
        }

        super.onCreate()

        // Initialize database synchronously with timeout to prevent blocking too long
        initializeDatabaseWithTimeout()

        // Initialize ads in background ONLY after database is ready
        if (isDatabaseInitialized) {
            applicationScope.launch {
                try {
                    Log.d(TAG, "Database is ready, initializing ads")
                    MobileAds.initialize(this@App) {}
                    appOpenAdManager = AppOpenAdManager(this@App)
                    appOpenAdManager.loadAd(this@App)
                    Log.d(TAG, "Ad initialization completed")
                    
                    // Notify startup manager that ads are initialized
                    AppStartupManager.markAdsInitialized()
                } catch (e: Exception) {
                    Log.e(TAG, "Ad initialization failed: ${e.message}", e)
                }
            }
        } else {
            Log.w(TAG, "Database not ready, skipping ad initialization")
        }
    }

    private fun initializeDatabaseWithTimeout() {
        try {
            // Perform database environment check before initialization
            DatabaseDiagnostics.performDatabaseEnvironmentCheck(this)

            // Check database integrity
            val isIntegrityOk = DatabaseDiagnostics.checkDatabaseIntegrity(this)
            if (!isIntegrityOk) {
                Log.w(TAG, "Database integrity check failed, attempting cleanup")
                DatabaseDiagnostics.cleanupCorruptedDatabase(this)
            }

            // Use runBlocking with timeout to ensure database is initialized before UI starts
            runBlocking {
                withTimeoutOrNull(3000) { // 3 second timeout
                    withContext(Dispatchers.IO) {
                        try {
                            // Force database instance creation
                            database
                            Log.d(TAG, "Database instance obtained")

                            // Initialize settings to ensure database is fully ready
                            val settingsRepository = PreferenceUtil(this@App).repository
                            settingsRepository.initializeSettings()

                            isDatabaseInitialized = true
                            Log.d(TAG, "Database and settings initialization successful")
                            
                            // Notify startup manager that database is ready
                            AppStartupManager.markDatabaseReady()
                        } catch (e: Exception) {
                            Log.e(TAG, "Database initialization failed: ${e.message}", e)

                            // Perform additional diagnostics on failure
                            DatabaseDiagnostics.performDatabaseEnvironmentCheck(this@App)

                            // Even if initialization fails, mark as attempted to prevent infinite waiting
                            isDatabaseInitialized = true
                            throw e
                        }
                    }
                } ?: run {
                    Log.w(TAG, "Database initialization timed out, continuing with app startup")
                    DatabaseDiagnostics.performDatabaseEnvironmentCheck(this as Context)
                    isDatabaseInitialized = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical error during database initialization: ${e.message}", e)
            isDatabaseInitialized = true // Prevent app from hanging
        }
    }

    private fun ensureAppDirectories() {
        try {
            // Ensure application data directory exists
            val appDir = File(filesDir, "known_app_data")
            if (!appDir.exists()) {
                val created = appDir.mkdirs()
                Log.d(TAG, "App directory created: $created, path: ${appDir.absolutePath}")
            }

            // Ensure database directory exists
            val dbDir = File(filesDir, "databases")
            if (!dbDir.exists()) {
                val created = dbDir.mkdirs()
                Log.d(TAG, "Database directory created: $created, path: ${dbDir.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create app directories: ${e.message}", e)
        }
    }

    // Method to check if database is ready for use
    fun isDatabaseReady(): Boolean = isDatabaseInitialized
} 