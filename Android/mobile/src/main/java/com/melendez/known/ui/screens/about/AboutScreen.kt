package com.melendez.known.ui.screens.about

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.ui.components.PreferenceItem
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.screens.Screens

const val weblate = "https://hosted.weblate.org/engage/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            title = stringResource(R.string.about),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        About_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@Composable
fun About_Content(modifier: Modifier, navTotalController: NavHostController) {

    val context = LocalContext.current
    var showingDialog by remember { mutableStateOf(false) }

    if (showingDialog) {
        AlertDialog(
            onDismissRequest = {
                showingDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://bmc.link/markmelendez".toUri()
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showingDialog = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = stringResource(R.string.attention)
                )
            },
            title = {
                Text(text = stringResource(R.string.attention))
            },
            text = {
                Text(text = stringResource(R.string.disclaimer))
            }
        )
    }

    LazyColumn(modifier = modifier) {
        item {
            PreferenceItem(
                title = stringResource(R.string.readme),
                icon = Icons.Rounded.Description,
            ) {
                openUrl(
                    url = "https://github.com/Melendez1209/Known/blob/main/README.md",
                    context = context
                )
            }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.release),
                description = stringResource(R.string.release_desc),
                icon = Icons.Rounded.NewReleases,
            ) { openUrl("https://github.com/Melendez1209/Known/releases", context = context) }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.github_issue),
                description = stringResource(R.string.github_issue_desc),
                icon = Icons.AutoMirrored.Outlined.ContactSupport,
            ) { openUrl(url = "https://github.com/Melendez1209/Known/issues", context = context) }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.credits),
                description = stringResource(R.string.credits_desc),
                icon = Icons.Outlined.AutoAwesome,
            ) {
                navTotalController.navigate(Screens.Credits.router)
            }
        }
    }
}

fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun About_Content_Preview() {
    About_Content(modifier = Modifier, navTotalController = rememberNavController())
}

@Preview(device = "spec:parent=pixel_9_pro")
@Composable
fun AboutScreen_Preview() {
    AboutScreen(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}
