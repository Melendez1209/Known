package com.melendez.known.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.melendez.known.ui.components.LocalFixedColorRoles

const val DEFAULT_SEED_COLOR = 0xa3d48d

object FixedAccentColors {
    val primaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.primaryFixed

    val primaryFixedDim: Color
        @Composable get() = LocalFixedColorRoles.current.primaryFixedDim

    val onPrimaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.onPrimaryFixed

    val onPrimaryFixedVariant: Color
        @Composable get() = LocalFixedColorRoles.current.onPrimaryFixedVariant

    val secondaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.secondaryFixed

    val secondaryFixedDim: Color
        @Composable get() = LocalFixedColorRoles.current.secondaryFixedDim

    val onSecondaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.onSecondaryFixed

    val onSecondaryFixedVariant: Color
        @Composable get() = LocalFixedColorRoles.current.onSecondaryFixedVariant

    val tertiaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.tertiaryFixed

    val tertiaryFixedDim: Color
        @Composable get() = LocalFixedColorRoles.current.tertiaryFixedDim

    val onTertiaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.onTertiaryFixed

    val onTertiaryFixedVariant: Color
        @Composable get() = LocalFixedColorRoles.current.onTertiaryFixedVariant
}


@Immutable
data class FixedColorRoles(
    val primaryFixed: Color,
    val primaryFixedDim: Color,
    val onPrimaryFixed: Color,
    val onPrimaryFixedVariant: Color,
    val secondaryFixed: Color,
    val secondaryFixedDim: Color,
    val onSecondaryFixed: Color,
    val onSecondaryFixedVariant: Color,
    val tertiaryFixed: Color,
    val tertiaryFixedDim: Color,
    val onTertiaryFixed: Color,
    val onTertiaryFixedVariant: Color,
) {
    companion object {
        @Stable
        internal fun fromColorSchemes(
            lightColors: ColorScheme,
            darkColors: ColorScheme,
        ): FixedColorRoles {
            return FixedColorRoles(
                primaryFixed = lightColors.primaryContainer,
                onPrimaryFixed = lightColors.onPrimaryContainer,
                onPrimaryFixedVariant = darkColors.primaryContainer,
                secondaryFixed = lightColors.secondaryContainer,
                onSecondaryFixed = lightColors.onSecondaryContainer,
                onSecondaryFixedVariant = darkColors.secondaryContainer,
                tertiaryFixed = lightColors.tertiaryContainer,
                onTertiaryFixed = lightColors.onTertiaryContainer,
                onTertiaryFixedVariant = darkColors.tertiaryContainer,
                primaryFixedDim = darkColors.primary,
                secondaryFixedDim = darkColors.secondary,
                tertiaryFixedDim = darkColors.tertiary,
            )
        }
    }
}