package com.melendez.known.data.repository

import com.melendez.known.data.dao.SettingsDao
import com.melendez.known.data.entity.Settings
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.known.util.DarkThemePreference
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
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
        // Check if the setting already exists
        val currentSettings = settingsDao.getSettingsSync()
        if (currentSettings == null) {
            // Initialise default settings only if they do not exist
            val defaultSettings = Settings(
                id = 1,
                darkThemeValue = DarkThemePreference.FOLLOW_SYSTEM,
                isHighContrastMode = false,
                isDynamicColorEnabled = false,
                selectedLanguage = "",
                themeColor = DEFAULT_SEED_COLOR,
                paletteStyleIndex = 0,
                predictiveBackEnabled = true,
                isFirstLogin = true
            )
            settingsDao.updateSettings(defaultSettings)
        }
    }
}