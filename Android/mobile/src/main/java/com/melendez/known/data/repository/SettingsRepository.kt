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

    suspend fun updateLanguage(language: String) {
        settingsDao.updateLanguage(language)
    }

    suspend fun updateThemeColor(color: Int) {
        settingsDao.updateThemeColor(color)
    }

    suspend fun updatePaletteStyle(index: Int) {
        settingsDao.updatePaletteStyle(index)
    }

    suspend fun initializeSettings() {
        settingsDao.updateSettings(Settings())
    }
} 