package com.melendez.caelum.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.melendez.caelum.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.caelum.ui.theme.FixedColorRoles
import com.melendez.caelum.util.settings.DarkThemePreference
import com.melendez.caelum.util.ScreenType

val LocalActivity = compositionLocalOf<ComponentActivity?> { null }
val LocalDarkTheme = compositionLocalOf { DarkThemePreference() }
val LocalSeedColor = compositionLocalOf { DEFAULT_SEED_COLOR }
val LocalDynamicColorSwitch = compositionLocalOf { false }
val LocalPaletteStyleIndex = compositionLocalOf { 0 }
val LocalScreenType = compositionLocalOf<ScreenType> { ScreenType.Compact }
val LocalFixedColorRoles = staticCompositionLocalOf {
    FixedColorRoles.fromColorSchemes(
        lightColors = lightColorScheme(),
        darkColors = darkColorScheme(),
    )
}
