@file:OptIn(ExperimentalMaterial3Api::class)

package com.melendez.known.ui.components

import android.os.Build
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.util.ScreenType

@Composable
fun SharedTopBar(
    screenType: ScreenType,
    navTotalController: NavHostController,
    title: String,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val activity = LocalActivity.current
    val supportsPredictiveBack = Build.VERSION.SDK_INT >= 34

    val onBackPressed: () -> Unit = {
        if (supportsPredictiveBack && activity != null) {
            // Using predictive back API
            activity.onBackPressedDispatcher.onBackPressed()
        } else {
            // Traditional back operation
            navTotalController.popBackStack()
        }
    }

    if (screenType == ScreenType.Medium) {
        MediumTopAppBar(
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
        LargeTopAppBar(
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
        navTotalController = rememberNavController(),
        title = "Title",
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}

@Preview(device = "id:pixel_9_pro", group = "SharedTopBar")
@Composable
fun LargeTopBar_Preview() {
    SharedTopBar(
        screenType = ScreenType.Compact,
        navTotalController = rememberNavController(),
        title = "Title",
        scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    )
}