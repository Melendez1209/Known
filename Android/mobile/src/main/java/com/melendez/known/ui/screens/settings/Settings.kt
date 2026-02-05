package com.melendez.known.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.melendez.known.R
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.SettingItem
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navigator: Navigator) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val screenType = LocalScreenType.current

    Column {
        SharedTopBar(
            title = stringResource(R.string.settings),
            screenType = screenType,
            navController = navigator,
            scrollBehavior = scrollBehavior
        )
        SettingsContent(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navigator = navigator
        )
    }
}


@Composable
private fun SettingsContent(modifier: Modifier, navigator: Navigator) {
    LazyColumn(modifier = modifier) {
        item {
            SettingItem(
                title = stringResource(id = R.string.general_settings),
                description = stringResource(id = R.string.general_settings_desc),
                icon = Icons.Rounded.SettingsApplications,
            ) {
                TODO("General Settings")
            }
        }
        item {
            SettingItem(
                title = stringResource(id = R.string.look_and_feel),
                description = stringResource(id = R.string.display_settings),
                icon = Icons.Rounded.Palette,
            ) {
                navigator.navigate(Screens.Appearance)
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun SettingsPreview() {
    val navigationState = remember {
        com.melendez.known.ui.navigation.NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    Settings(navigator = Navigator(navigationState))
}