package com.melendez.known.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.android.material.color.DynamicColors
import com.melendez.known.App.Companion.applicationScope
import com.melendez.known.R
import com.melendez.known.colour.PaletteStyle
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val STYLE_TONAL_SPOT = 0
const val STYLE_MONOCHROME = 4

const val DARK_THEME_VALUE = "dark_theme_value"
const val PALETTE_STYLE = "palette_style"

private const val THEME_COLOR = "theme_color"
private const val DYNAMIC_COLOR = "dynamic_color"
private const val HIGH_CONTRAST = "high_contrast"

val paletteStyles =
    listOf(
        PaletteStyle.TonalSpot,
        PaletteStyle.Spritz,
        PaletteStyle.FruitSalad,
        PaletteStyle.Vibrant,
        PaletteStyle.Monochrome,
    )

object PreferenceUtil {
    private val kv: MMKV = MMKV.defaultMMKV()

    data class AppSettings(
        val darkTheme: DarkThemePreference = DarkThemePreference(),
        val isDynamicColorEnabled: Boolean = false,
        val seedColor: Int = DEFAULT_SEED_COLOR,
        val paletteStyleIndex: Int = 0,
    )

    private val mutableAppSettingsStateFlow =
        MutableStateFlow(
            AppSettings(
                DarkThemePreference(
                    darkThemeValue =
                        kv.decodeInt(
                            DARK_THEME_VALUE,
                            DarkThemePreference.FOLLOW_SYSTEM
                        ),
                    isHighContrastModeEnabled = kv.decodeBool(
                        HIGH_CONTRAST,
                        false
                    ),
                ),
                isDynamicColorEnabled =
                    kv.decodeBool(
                        DYNAMIC_COLOR,
                        DynamicColors.isDynamicColorAvailable()
                    ),
                seedColor = kv.decodeInt(
                    THEME_COLOR,
                    DEFAULT_SEED_COLOR
                ),
                paletteStyleIndex = kv.decodeInt(
                    PALETTE_STYLE,
                    0
                ),
            )
        )

    fun switchDynamicColor(
        enabled: Boolean = !mutableAppSettingsStateFlow.value.isDynamicColorEnabled
    ) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update { it.copy(isDynamicColorEnabled = enabled) }
            kv.encode(DYNAMIC_COLOR, enabled)
        }
    }

    fun modifyThemeSeedColor(colorArgb: Int, paletteStyleIndex: Int) {
        applicationScope.launch(Dispatchers.IO) {
            mutableAppSettingsStateFlow.update {
                it.copy(seedColor = colorArgb, paletteStyleIndex = paletteStyleIndex)
            }
            kv.encode(THEME_COLOR, colorArgb)
            kv.encode(PALETTE_STYLE, paletteStyleIndex)
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

    @Composable
    fun isDarkTheme(): Boolean {
        return if (darkThemeValue == FOLLOW_SYSTEM) isSystemInDarkTheme() else darkThemeValue == ON
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