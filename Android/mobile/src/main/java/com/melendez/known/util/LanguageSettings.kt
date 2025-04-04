package com.melendez.known.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.melendez.known.R
import java.util.Locale

// Do not modify
private const val ENGLISH = 1
private const val SIMPLIFIED_CHINESE = 2
private const val TRADITIONAL_CHINESE = 3

@Suppress("DEPRECATION")
val LocaleLanguageCodeMap =
    mapOf(
        Locale.forLanguageTag("zh-Hans") to SIMPLIFIED_CHINESE,
        Locale.forLanguageTag("zh-Hant") to TRADITIONAL_CHINESE,
        Locale("en", "US") to ENGLISH,
        Locale("en", "UK") to ENGLISH
    )

@Composable
fun Locale?.toDisplayName(): String =
    this?.getDisplayName(this) ?: stringResource(id = R.string.follow_system)
