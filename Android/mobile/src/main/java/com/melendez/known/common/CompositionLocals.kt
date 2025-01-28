package com.melendez.known.common

import androidx.compose.runtime.compositionLocalOf
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR

val LocalSeedColor = compositionLocalOf { DEFAULT_SEED_COLOR }
val LocalDynamicColorSwitch = compositionLocalOf { false }
val LocalPaletteStyleIndex = compositionLocalOf { 0 }
