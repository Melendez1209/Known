package com.melendez.known.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.melendez.known.data.entity.Settings
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: Settings)

    @Query("UPDATE settings SET darkThemeValue = :darkThemeValue WHERE id = 1")
    suspend fun updateDarkMode(darkThemeValue: Int)
    
    @Query("UPDATE settings SET isHighContrastMode = :isHighContrast WHERE id = 1")
    suspend fun updateHighContrastMode(isHighContrast: Boolean)
    
    @Query("UPDATE settings SET isDynamicColorEnabled = :isDynamicColor WHERE id = 1")
    suspend fun updateDynamicColor(isDynamicColor: Boolean)
    
    @Query("UPDATE settings SET selectedLanguage = :language WHERE id = 1")
    suspend fun updateLanguage(language: String)
    
    @Query("UPDATE settings SET themeColor = :color WHERE id = 1")
    suspend fun updateThemeColor(color: Int)
    
    @Query("UPDATE settings SET paletteStyleIndex = :index WHERE id = 1")
    suspend fun updatePaletteStyle(index: Int)
} 