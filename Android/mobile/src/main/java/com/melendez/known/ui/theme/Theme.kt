@file:Suppress("DEPRECATION")

package com.melendez.known.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection
import androidx.core.view.ViewCompat
import com.google.android.material.color.MaterialColors
import com.melendez.known.colour.LocalTonalPalettes
import com.melendez.known.colour.dynamicColorScheme
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalFixedColorRoles

fun Color.applyOpacity(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.62f)
}

@Composable
@ReadOnlyComposable
fun Color.harmonizeWith(other: Color) =
    Color(MaterialColors.harmonize(this.toArgb(), other.toArgb()))

@Composable
@ReadOnlyComposable
fun Color.harmonizeWithPrimary(): Color =
    this.harmonizeWith(other = MaterialTheme.colorScheme.primary)

@SuppressLint("MemberExtensionConflict")
@Composable
fun KnownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    isHighContrastModeEnabled: Boolean = false,
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val isDynamicColorEnabled = LocalDynamicColorSwitch.current
    val context = view.context as? Activity

    LaunchedEffect(darkTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (darkTheme) {
                view.windowInsetsController?.setSystemBarsAppearance(
                    0,
                    APPEARANCE_LIGHT_STATUS_BARS,
                )
            } else {
                view.windowInsetsController?.setSystemBarsAppearance(
                    APPEARANCE_LIGHT_STATUS_BARS,
                    APPEARANCE_LIGHT_STATUS_BARS,
                )
            }
        }
    }

    // Select different colour schemes depending on whether dynamic colours are enabled or not
    val colorScheme =
        if (isDynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
            // Use the dynamic colours provided by the system
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            // Using custom colour schemes
            dynamicColorScheme(!darkTheme).run {
                if (isHighContrastModeEnabled && darkTheme)
                    copy(
                        surface = Color.Black,
                        background = Color.Black,
                        surfaceContainerLowest = Color.Black,
                        surfaceContainerLow = surfaceContainerLowest,
                        surfaceContainer = surfaceContainerLow,
                        surfaceContainerHigh = surfaceContainerLow,
                        surfaceContainerHighest = surfaceContainer,
                    )
                else this
            }
        }

    val textStyle =
        LocalTextStyle.current.copy(
            lineBreak = LineBreak.Paragraph,
            textDirection = TextDirection.Content,
        )

    val tonalPalettes = LocalTonalPalettes.current

    if (!view.isInEditMode) {
        SideEffect {
            context?.window?.statusBarColor = colorScheme.surface.hashCode()
            context?.window?.navigationBarColor = colorScheme.surface.hashCode()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars
        }
    }

    CompositionLocalProvider(
        LocalFixedColorRoles provides FixedColorRoles.fromTonalPalettes(tonalPalettes),
        LocalTextStyle provides textStyle
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}