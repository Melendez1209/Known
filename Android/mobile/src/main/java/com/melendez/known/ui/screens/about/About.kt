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
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.PreferenceItem
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.screens.Screens

object AboutUrls {
    const val DONATE = "https://bmc.link/markmelendez"
    const val README = "https://github.com/Melendez1209/Known/blob/main/README.md"
    const val RELEASES = "https://github.com/Melendez1209/Known/releases"
    const val ISSUES = "https://github.com/Melendez1209/Known/issues"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val screenType = LocalScreenType.current

    Column {
        SharedTopBar(
            title = stringResource(R.string.about),
            screenType = screenType,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        AboutContent(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@Composable
fun AboutContent(modifier: Modifier = Modifier, navTotalController: NavHostController) {
    val context = LocalContext.current
    var showDonateDialog by remember { mutableStateOf(false) }

    if (showDonateDialog) {
        DonateDialog(
            onDismiss = { showDonateDialog = false },
            onConfirm = { openUrl(AboutUrls.DONATE, context) }
        )
    }

    LazyColumn(modifier = modifier) {
        item {
            PreferenceItem(
                title = stringResource(R.string.readme),
                icon = Icons.Rounded.Description
            ) {
                openUrl(AboutUrls.README, context)
            }
        }

        item {
            PreferenceItem(
                title = stringResource(R.string.release),
                description = stringResource(R.string.release_desc),
                icon = Icons.Rounded.NewReleases
            ) {
                openUrl(AboutUrls.RELEASES, context)
            }
        }

        item {
            PreferenceItem(
                title = stringResource(R.string.github_issue),
                description = stringResource(R.string.github_issue_desc),
                icon = Icons.AutoMirrored.Outlined.ContactSupport
            ) {
                openUrl(AboutUrls.ISSUES, context)
            }
        }

        item {
            PreferenceItem(
                title = stringResource(R.string.credits),
                description = stringResource(R.string.credits_desc),
                icon = Icons.Outlined.AutoAwesome
            ) {
                navTotalController.navigate(Screens.Credits.router)
            }
        }
    }
}

@Composable
private fun DonateDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.padding(4.dp)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
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

fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    context.startActivity(intent)
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun AboutContentPreview() {
    AboutContent(navTotalController = rememberNavController())
}

@Preview(device = "spec:parent=pixel_9_pro")
@Composable
private fun AboutScreenPreview() {
    About(navTotalController = rememberNavController())
}
