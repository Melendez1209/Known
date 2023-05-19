package com.melendez.known.settings.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Abc
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R

@Composable
fun Settings(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Settings_Compact(navTotalController = navTotalController)
        }

        WindowWidthSizeClass.Medium -> {
            Settings_Medium(navTotalController = navTotalController)
        }

        WindowWidthSizeClass.Expanded -> {
            Settings_Expanded(navTotalController = navTotalController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Compact(navTotalController: NavHostController) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text(text = stringResource(R.string.settings))
        }, navigationIcon = {
            IconButton(onClick = { navTotalController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Rounded.NavigateBefore,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, actions = {
            IconButton(onClick = { TODO("Login") }) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(R.string.login)
                )
            }
        })
    }) { Settings_Content() }
}

@Preview(name = "Settings_Compact", device = "id:pixel_7_pro")
@Composable
fun Settings_Compact_Preview() {
    Settings_Compact(navTotalController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Medium(navTotalController: NavHostController) {
    Scaffold(topBar = {

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        MediumTopAppBar(
            title = { Text(text = stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { TODO("Login") }) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = stringResource(R.string.login)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }) { Settings_Content() }
}

@Preview(name = "Settings_Medium", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun Settings_Medium_Preview() {
    Settings_Medium(rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Expanded(navTotalController: NavHostController) {
    Scaffold(topBar = {

        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        LargeTopAppBar(
            title = { Text(text = stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { TODO("Login") }) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = stringResource(R.string.login)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }) { Settings_Content() }
}

@Preview(name = "Settings_Expanded", device = "spec:width=673dp,height=841dp")
@Composable
fun Settings_Expanded_Preview() {
    Settings_Expanded(rememberNavController())
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Settings_Content() {
    Surface {
        LazyColumn {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Icon(
                                imageVector = Icons.Rounded.Abc,
                                contentDescription = stringResource(R.string.navigation_bar_hint)
                            )
                            Column {
                                Text(text = stringResource(id = R.string.navigation_bar_hint))
                                Text(text = stringResource(id = R.string.navigation_bar_hint))
                            }
                        }
                    }
                }
            }
        }
    }
}