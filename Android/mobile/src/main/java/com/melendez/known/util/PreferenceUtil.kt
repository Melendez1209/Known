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

object Identity {
    const val NONE = 0
    const val STUDENT = 1
    const val TEACHER = 2
    const val PARENT = 3
}

val subjectKeys = listOf(
    "physics",
    "chemistry",
    "biology",
    "political",
    "history",
    "geography",
    "pe"
)

fun identityResourceToConstant(resourceId: Int): Int = when (resourceId) {
    R.string.student -> Identity.STUDENT
    R.string.teacher -> Identity.TEACHER
    R.string.parent -> Identity.PARENT
    else -> Identity.NONE
}

fun subjectKeyToStringResource(key: String): Int = when (key) {
    "physics" -> R.string.physics
    "chemistry" -> R.string.chemistry
    "biology" -> R.string.biology
    "political" -> R.string.political
    "history" -> R.string.history_subject
    "geography" -> R.string.geography
    else -> R.string.pe
}

fun subjectResourceToKey(resourceId: Int): String = when (resourceId) {
    R.string.physics -> "physics"
    R.string.chemistry -> "chemistry"
    R.string.biology -> "biology"
    R.string.political -> "political"
    R.string.history_subject -> "history"
    R.string.geography -> "geography"
    else -> "pe"
}

fun String.toSubjectKeySet(): Set<String> =
    if (isEmpty()) emptySet() else split(',').filter { it.isNotEmpty() }.toSet()

fun Set<String>.toSubjectString(): String = joinToString(",")

class PreferenceUtil(application: Application) : AndroidViewModel(application) {
    val repository: SettingsRepository
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
            if (darkThemeValue != DarkThemePreference.FOLLOW_SYSTEM) {
                repository.updateDarkMode(darkThemeValue)
            }
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

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            repository.updateLanguage(language)
        }
    }

    fun setFirstLogin(isFirstLogin: Boolean) {
        viewModelScope.launch {
            repository.updateFirstLogin(isFirstLogin)
        }
    }

    // Predictive back gesture settings method
    fun setPredictiveBackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updatePredictiveBack(enabled)
        }
    }

    fun updateIdentity(identity: Int) {
        viewModelScope.launch {
            repository.updateIdentity(identity)
        }
    }

    fun updateRegion(region: String) {
        viewModelScope.launch {
            repository.updateRegion(region)
        }
    }

    fun updateSelectedSubjects(subjects: Set<String>) {
        viewModelScope.launch {
            repository.updateSelectedSubjects(subjects.toSubjectString())
        }
    }
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