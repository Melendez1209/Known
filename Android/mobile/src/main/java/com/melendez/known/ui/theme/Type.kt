@file:OptIn(ExperimentalTextApi::class, ExperimentalTextApi::class, ExperimentalTextApi::class)

package com.melendez.known.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextDirection

val Typography =
    Typography().run {
        copy(
            bodyLarge = bodyLarge.applyLinebreak().applyTextDirection(),
            bodyMedium = bodyMedium.applyLinebreak().applyTextDirection(),
            bodySmall = bodySmall.applyLinebreak().applyTextDirection(),
            titleLarge = titleLarge.applyTextDirection(),
            titleMedium = titleMedium.applyTextDirection(),
            titleSmall = titleSmall.applyTextDirection(),
            headlineSmall = headlineSmall.applyTextDirection(),
            headlineMedium = headlineMedium.applyTextDirection(),
            headlineLarge = headlineLarge.applyTextDirection(),
            displaySmall = displaySmall.applyTextDirection(),
            displayMedium = displayMedium.applyTextDirection(),
            displayLarge = displayLarge.applyTextDirection(),
            labelLarge = labelLarge.applyTextDirection(),
            labelMedium = labelMedium.applyTextDirection(),
            labelSmall = labelSmall.applyTextDirection(),
            // Emphasized styles for M3 Expressive
            bodyLargeEmphasized = bodyLargeEmphasized.applyLinebreak().applyTextDirection(),
            bodyMediumEmphasized = bodyMediumEmphasized.applyLinebreak().applyTextDirection(),
            bodySmallEmphasized = bodySmallEmphasized.applyLinebreak().applyTextDirection(),
            titleLargeEmphasized = titleLargeEmphasized.applyTextDirection(),
            titleMediumEmphasized = titleMediumEmphasized.applyTextDirection(),
            titleSmallEmphasized = titleSmallEmphasized.applyTextDirection(),
            headlineSmallEmphasized = headlineSmallEmphasized.applyTextDirection(),
            headlineMediumEmphasized = headlineMediumEmphasized.applyTextDirection(),
            headlineLargeEmphasized = headlineLargeEmphasized.applyTextDirection(),
            displaySmallEmphasized = displaySmallEmphasized.applyTextDirection(),
            displayMediumEmphasized = displayMediumEmphasized.applyTextDirection(),
            displayLargeEmphasized = displayLargeEmphasized.applyTextDirection(),
            labelLargeEmphasized = labelLargeEmphasized.applyTextDirection(),
            labelMediumEmphasized = labelMediumEmphasized.applyTextDirection(),
            labelSmallEmphasized = labelSmallEmphasized.applyTextDirection(),
        )
    }

private fun TextStyle.applyLinebreak(): TextStyle = this.copy(lineBreak = LineBreak.Paragraph)

private fun TextStyle.applyTextDirection(): TextStyle =
    this.copy(textDirection = TextDirection.Content)
