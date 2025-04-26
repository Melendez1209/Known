package com.melendez.known.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.SettingsApplications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.ui.components.SettingItem
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column {
        SharedTopBar(
            title = stringResource(R.string.settings),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        SettingsContent(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}


@Composable
private fun SettingsContent(modifier: Modifier, navTotalController: NavHostController) {
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
                navTotalController.navigate(Screens.Appearance.router)
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun SettingsPreview() {
    Settings(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}