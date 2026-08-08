package com.melendez.known.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.melendez.known.util.DarkThemePreference

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
    // Predictive back gesture settings
    val predictiveBackEnabled: Boolean = true,
    // First login tracking
    val isFirstLogin: Boolean = true
) 