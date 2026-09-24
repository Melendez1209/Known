package com.melendez.known.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.melendez.known.R
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.util.ScreenType

@Composable
fun SharedTopBar(
    screenType: ScreenType,
    navController: Navigator,
    title: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val onBackPressed: () -> Unit = { navController.goBack() }

    if (screenType == ScreenType.Medium) {
        MediumFlexibleTopAppBar(
            title = { Text(text = title) },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = actions,
            scrollBehavior = scrollBehavior
        )
    } else {
        LargeFlexibleTopAppBar(
            title = { Text(text = title) },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = actions,
            scrollBehavior = scrollBehavior
        )
    }
}

@Preview(device = "id:pixel_9_pro", group = "SharedTopBar")
@Composable
fun MediumTopBar_Preview() {
    SharedTopBar(
        screenType = ScreenType.Medium,
        navController = Navigator(remember {
            com.melendez.known.ui.navigation.NavigationState(
                com.melendez.known.ui.screens.Screens.Main,
                mutableStateOf(com.melendez.known.ui.screens.Screens.Main),
                emptyMap()
            )
        }),
        title = "Title",
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}

@Preview(device = "id:pixel_9_pro", group = "SharedTopBar")
@Composable
fun LargeTopBar_Preview() {
    SharedTopBar(
        screenType = ScreenType.Compact,
        navController = Navigator(remember {
            com.melendez.known.ui.navigation.NavigationState(
                com.melendez.known.ui.screens.Screens.Main,
                mutableStateOf(com.melendez.known.ui.screens.Screens.Main),
                emptyMap()
            )
        }),
        title = "Title",
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}