package com.melendez.known.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.melendez.known.colour.TonalPalettes
import com.melendez.known.ui.components.LocalFixedColorRoles

const val DEFAULT_SEED_COLOR = 0xa3d48d

object FixedAccentColors {
    val primaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.primaryFixed

    val onPrimaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.onPrimaryFixed

    val secondaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.secondaryFixed

    val onSecondaryFixed: Color
        @Composable get() = LocalFixedColorRoles.current.onSecondaryFixed

    val tertiaryFixedDim: Color
        @Composable get() = LocalFixedColorRoles.current.tertiaryFixedDim
}


@Immutable
data class FixedColorRoles(
    val primaryFixed: Color,
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
                secondaryFixedDim = darkColors.secondary,
                tertiaryFixedDim = darkColors.tertiary,
            )
        }

        @Stable
        internal fun fromTonalPalettes(palettes: TonalPalettes): FixedColorRoles {
            return with(palettes) {
                FixedColorRoles(
                    primaryFixed = accent1(90.toDouble()),
                    onPrimaryFixed = accent1(10.toDouble()),
                    onPrimaryFixedVariant = accent1(30.toDouble()),
                    secondaryFixed = accent2(90.toDouble()),
                    secondaryFixedDim = accent2(80.toDouble()),
                    onSecondaryFixed = accent2(10.toDouble()),
                    onSecondaryFixedVariant = accent2(30.toDouble()),
                    tertiaryFixed = accent3(90.toDouble()),
                    tertiaryFixedDim = accent3(80.toDouble()),
                    onTertiaryFixed = accent3(10.toDouble()),
                    onTertiaryFixedVariant = accent3(30.toDouble()),
                )
            }
        }
    }
}