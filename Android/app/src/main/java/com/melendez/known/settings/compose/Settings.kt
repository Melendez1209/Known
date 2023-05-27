package com.melendez.known.settings.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
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
        Settings_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@Preview(name = "Settings_Compact", device = "id:pixel_7_pro")
@Composable
fun Settings_Compact_Preview() {
    Settings_Compact(navTotalController = rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Medium(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {

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
        Settings_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@Preview(name = "Settings_Medium", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun Settings_Medium_Preview() {
    Settings_Medium(rememberNavController())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Expanded(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {

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
        Settings_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}


@Preview(name = "Settings_Expanded", device = "spec:width=673dp,height=841dp")
@Composable
fun Settings_Expanded_Preview() {
    Settings_Expanded(rememberNavController())
}

@Composable
fun Settings_Content(modifier: Modifier) {

    val never = stringResource(id = R.string.never)
    val selected = stringResource(id = R.string.selected)
    val unselected = stringResource(id = R.string.unselected)
    val always = stringResource(id = R.string.always)

    var labelDisplayMode by remember { mutableStateOf(never) }
    val labelDisplayOptions = listOf(never, selected, unselected, always)
    var expanded by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(id = R.string.navigation_bar_hint))
                    Column {
                        Button(onClick = { expanded = true }) {
                            Text(text = labelDisplayMode)
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = stringResource(id = R.string.navigation_bar_hint)
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            labelDisplayOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(text = option) },
                                    onClick = {
                                        labelDisplayMode = option
                                        expanded = false
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_7_pro")
@Composable
fun Settings_Content_Preview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Settings_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
}