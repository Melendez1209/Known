package com.melendez.known.settings.compose

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
                    contentDescription = stringResource(R.string.sign_to)
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
                        contentDescription = stringResource(R.string.sign_to)
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
                        contentDescription = stringResource(R.string.sign_to)
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

    var labelMode by rememberSaveable { mutableStateOf(false) }
    var colorMode by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }

    var visibleAppearance by remember { mutableStateOf(true) }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Button(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { TODO("Login") }) {
                        Text(text = stringResource(R.string.sign_to))
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { visibleAppearance = !visibleAppearance }) {
                        Text(
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                            text = stringResource(R.string.appearance),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    AnimatedVisibility(visible = visibleAppearance) {
                        Column {
                            SettingRow(
                                title = stringResource(id = R.string.hint_title),
                                subOn = stringResource(id = R.string.hint_on),
                                subOff = stringResource(id = R.string.hint_off),
                                value = labelMode,
                                onCheckedChange = { labelMode = it }
                            )
                            Divider()
                            SettingRow(
                                title = stringResource(R.string.dynamic_color),
                                subOn = stringResource(R.string.apply_wallpaper),
                                subUnenable = stringResource(R.string.low_version),
                                value = colorMode,
                                enable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                                onCheckedChange = { colorMode = it }
                            )
                            Divider()
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
    Settings_Content(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    )
}

@Composable
fun SettingRow(
    title: String,
    subOn: String,
    subOff: String = subOn,
    subUnenable: String = "",
    value: Boolean,
    enable: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = if (!enable) subUnenable else if (value) subOn else subOff,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Switch(
            enabled = enable,
            checked = value,
            onCheckedChange = { onCheckedChange?.invoke(it) })
    }
}
