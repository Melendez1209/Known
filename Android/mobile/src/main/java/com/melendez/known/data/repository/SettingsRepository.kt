package com.melendez.known.data.repository

import android.util.Log
import com.melendez.known.data.dao.SettingsDao
import com.melendez.known.data.entity.Settings
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.known.util.DarkThemePreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    companion object {
        private const val TAG = "SettingsRepository"
    }

    val settings: Flow<Settings?> = settingsDao.getSettings()

    suspend fun updateDarkMode(isDarkMode: Int) {
        settingsDao.updateDarkMode(isDarkMode)
    }

    suspend fun updateHighContrastMode(isHighContrast: Boolean) {
        settingsDao.updateHighContrastMode(isHighContrast)
    }

    suspend fun updateDynamicColor(isDynamicColor: Boolean) {
        settingsDao.updateDynamicColor(isDynamicColor)
    }

    suspend fun updateThemeColor(color: Int) {
        settingsDao.updateThemeColor(color)
    }

    suspend fun updatePaletteStyle(index: Int) {
        settingsDao.updatePaletteStyle(index)
    }

    suspend fun updatePredictiveBack(enabled: Boolean) {
        settingsDao.updatePredictiveBack(enabled)
    }

    suspend fun updateLanguage(language: String) {
        settingsDao.updateLanguage(language)
    }

    suspend fun updateFirstLogin(isFirstLogin: Boolean) {
        settingsDao.updateFirstLogin(isFirstLogin)
    }

    suspend fun initializeSettings() {
        try {
            Log.d(TAG, "Starting settings initialization")

            // Retry logic for database operations
            var attempts = 0
            val maxAttempts = 3
            var lastException: Exception? = null

            while (attempts < maxAttempts) {
                try {
                    // Check if the setting already exists
                    val currentSettings = settingsDao.getSettingsSync()
                    if (currentSettings == null) {
                        Log.d(TAG, "No existing settings found, creating default settings")
                        // Initialize default settings only if they do not exist
                        val defaultSettings = Settings(
                            id = 1,
                            darkThemeValue = DarkThemePreference.FOLLOW_SYSTEM,
                            isHighContrastMode = false,
                            isDynamicColorEnabled = true,
                            selectedLanguage = "",
                            themeColor = DEFAULT_SEED_COLOR,
                            paletteStyleIndex = 0,
                            predictiveBackEnabled = true,
                            isFirstLogin = true
                        )
                        settingsDao.updateSettings(defaultSettings)
                        Log.d(TAG, "Default settings created successfully")
                    } else {
                        Log.d(TAG, "Existing settings found, initialization complete")
                    }

                    // If we reach here, initialization was successful
                    return

                } catch (e: Exception) {
                    lastException = e
                    attempts++
                    Log.w(TAG, "Settings initialization attempt $attempts failed: ${e.message}")

                    if (attempts < maxAttempts) {
                        // Wait before retrying
                        delay(200L * attempts) // Exponential backoff
                    }
                }
            }

            // If all attempts failed, throw the last exception
            throw lastException
                ?: Exception("Settings initialization failed after $maxAttempts attempts")

        } catch (e: Exception) {
            Log.e(TAG, "Critical error during settings initialization: ${e.message}", e)
            throw e
        }
    }
}