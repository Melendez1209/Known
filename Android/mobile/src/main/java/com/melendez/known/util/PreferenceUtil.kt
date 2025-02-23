package com.melendez.known.util

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.melendez.known.R
import com.melendez.known.colour.PaletteStyle
import com.melendez.known.data.AppDatabase
import com.melendez.known.data.entity.Settings
import com.melendez.known.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

const val STYLE_TONAL_SPOT = 0
const val STYLE_MONOCHROME = 4

val paletteStyles = listOf(
    PaletteStyle.TonalSpot,
    PaletteStyle.Spritz,
    PaletteStyle.FruitSalad,
    PaletteStyle.Vibrant,
    PaletteStyle.Monochrome,
)

class PreferenceUtil(application: Application) : AndroidViewModel(application) {
    private val repository: SettingsRepository
    val settings: Flow<Settings?>
    
    init {
        val settingsDao = AppDatabase.getDatabase(application).settingsDao()
        repository = SettingsRepository(settingsDao)
        settings = repository.settings
    }

    fun modifyDarkThemePreference(
        darkThemeValue: Int = DarkThemePreference.FOLLOW_SYSTEM,
        isHighContrastModeEnabled: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateDarkMode(darkThemeValue)
            repository.updateHighContrastMode(isHighContrastModeEnabled)
        }
    }

    fun switchDynamicColor(enabled: Boolean = false) {
        viewModelScope.launch {
            repository.updateDynamicColor(enabled)
        }
    }

    fun modifyThemeSeedColor(color: Int, paletteStyle: Int) {
        viewModelScope.launch {
            repository.updateThemeColor(color)
            repository.updatePaletteStyle(paletteStyle)
        }
    }

    fun initializeSettings() {
        viewModelScope.launch {
            repository.initializeSettings()
        }
    }

    // 其他设置方法...
}

data class DarkThemePreference(
    val darkThemeValue: Int = FOLLOW_SYSTEM,
    val isHighContrastModeEnabled: Boolean = false,
) {
    companion object {
        const val FOLLOW_SYSTEM = 1
        const val ON = 2
        const val OFF = 3
    }

    fun isDarkTheme(isSystemInDarkTheme: Boolean): Boolean {
        return if (darkThemeValue == FOLLOW_SYSTEM) isSystemInDarkTheme else darkThemeValue == ON
    }

    @Composable
    fun getDarkThemeDesc(): String {
        return when (darkThemeValue) {
            FOLLOW_SYSTEM -> stringResource(R.string.follow_system)
            ON -> stringResource(R.string.on)
            else -> stringResource(R.string.off)
        }
    }
}