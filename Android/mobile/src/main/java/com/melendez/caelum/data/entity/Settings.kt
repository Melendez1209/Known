package com.melendez.caelum.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melendez.caelum.util.HistoryViewMode
import com.melendez.caelum.util.settings.DarkThemePreference

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    val id: Int = 1, // Only one record needed
    val darkThemeValue: Int = DarkThemePreference.FOLLOW_SYSTEM,
    val isHighContrastMode: Boolean = false,
    val isDynamicColorEnabled: Boolean = true,
    val selectedLanguage: String = "",
    val themeColor: Int = 0,
    val paletteStyleIndex: Int = 0,
    val predictiveBackEnabled: Boolean = true,
    val isFirstLogin: Boolean = true,
    // Onboarding / general settings
    val identity: Int = 0,
    val region: String = "",
    val selectedSubjects: String = "",
    // Density of the history list, one of the HistoryViewMode values
    val historyViewMode: Int = HistoryViewMode.DEFAULT.value
) 