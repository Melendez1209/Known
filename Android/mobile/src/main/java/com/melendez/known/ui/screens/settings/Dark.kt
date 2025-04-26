package com.melendez.known.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.ui.components.LocalDarkTheme
import com.melendez.known.ui.components.PreferenceSingleChoiceItem
import com.melendez.known.ui.components.PreferenceSubtitle
import com.melendez.known.ui.components.PreferenceSwitchVariant
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.util.DarkThemePreference.Companion.FOLLOW_SYSTEM
import com.melendez.known.util.DarkThemePreference.Companion.OFF
import com.melendez.known.util.DarkThemePreference.Companion.ON
import com.melendez.known.util.PreferenceUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dark(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            title = stringResource(R.string.dark_theme),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        Dark_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@Composable
fun Dark_Content(modifier: Modifier) {

    val darkThemePreference = LocalDarkTheme.current
    val isHighContrastModeEnabled = darkThemePreference.isHighContrastModeEnabled
    val preferenceUtil: PreferenceUtil = viewModel()

    Surface {
        LazyColumn(modifier = modifier) {
            item {
                PreferenceSingleChoiceItem(
                    text = stringResource(R.string.follow_system),
                    selected = darkThemePreference.darkThemeValue == FOLLOW_SYSTEM,
                ) {
                    preferenceUtil.modifyDarkThemePreference(FOLLOW_SYSTEM)
                }
            }
            item {
                PreferenceSingleChoiceItem(
                    text = stringResource(R.string.on),
                    selected = darkThemePreference.darkThemeValue == ON,
                ) {
                    preferenceUtil.modifyDarkThemePreference(ON)
                }
            }
            item {
                PreferenceSingleChoiceItem(
                    text = stringResource(R.string.off),
                    selected = darkThemePreference.darkThemeValue == OFF,
                ) {
                    preferenceUtil.modifyDarkThemePreference(OFF)
                }
            }
            item { PreferenceSubtitle(text = stringResource(R.string.additional_settings)) }
            item {
                PreferenceSwitchVariant(
                    enabled = darkThemePreference.isDarkTheme(isSystemInDarkTheme = isSystemInDarkTheme()),
                    title = stringResource(R.string.high_contrast),
                    icon = Icons.Outlined.Contrast,
                    isChecked = isHighContrastModeEnabled,
                    onClick = { preferenceUtil.modifyDarkThemePreference(isHighContrastModeEnabled = !isHighContrastModeEnabled) }
                )
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Dark_Preview() {
    Dark(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}