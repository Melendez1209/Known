package com.melendez.known.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melendez.known.util.DarkThemePreference
import com.melendez.known.util.HistoryViewMode

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
