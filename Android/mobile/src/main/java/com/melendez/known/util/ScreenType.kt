package com.melendez.known.util

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

sealed class ScreenType(val name: String) {
    data object Compact : ScreenType("Compact")
    data object Medium : ScreenType("Medium")
    data object Expanded : ScreenType("Expanded")
}

/**
 * Determine the unified screen size class based on height and width.
 * Rules: Width takes priority, with special handling for Expanded width.
 */
fun getUnifiedSizeClass(
    windowHeightSizeClass: WindowHeightSizeClass,
    windowWidthSizeClass: WindowWidthSizeClass
): ScreenType {
    return when (windowWidthSizeClass) {
        // If width is Compact, always return Compact
        WindowWidthSizeClass.Compact -> ScreenType.Compact

        // If width is Medium, always return Medium
        WindowWidthSizeClass.Medium -> ScreenType.Medium

        // If width is Expanded, return Medium only when height is Compact
        WindowWidthSizeClass.Expanded -> {
            if (windowHeightSizeClass == WindowHeightSizeClass.Compact) {
                ScreenType.Medium
            } else {
                ScreenType.Expanded
            }
        }

        else -> ScreenType.Compact
    }
}