package com.melendez.known.data.repository

import com.melendez.known.data.dao.SettingsDao
import com.melendez.known.data.entity.Settings
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

    suspend fun updatePredictiveBackAnimation(enabled: Boolean) {
        settingsDao.updatePredictiveBackAnimation(enabled)
    }

    suspend fun initializeSettings() {
        settingsDao.updateSettings(Settings())
    }
}