package com.melendez.known.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens


@Composable
fun AboutScreen(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> AboutScreen_Compat(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> AboutScreen_MediumExpanded(
            navTotalController = navTotalController
        )
        else -> AboutScreen_Compat(navTotalController = navTotalController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen_Compat(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 12.dp)
            )
            About_Content(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                navTotalController = navTotalController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen_MediumExpanded(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .padding(vertical = 12.dp)
                )
                About_Content(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    navTotalController = navTotalController
                )
            }
        }
    }
}

@Composable
fun About_Content(modifier: Modifier, navTotalController: NavHostController) {

    val context = LocalContext.current

    LazyColumn(modifier = modifier) {
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.developers)) },
                modifier = Modifier.clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Melendez1209/known/graphs/contributors")
                    )
                    context.startActivity(intent)
                },
                supportingContent = { Text(text = stringResource(id = R.string.all_contributors)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.PeopleAlt,
                        contentDescription = stringResource(id = R.string.developers)
                    )
                }
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.sponsorships)) },
                modifier = Modifier.clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://bmc.link/markmelendez")
                    )
                    context.startActivity(intent)
                },
                supportingContent = { Text(text = stringResource(R.string.better_experience)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.AttachMoney,
                        contentDescription = stringResource(id = R.string.sponsorships)
                    )
                }
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.privacy)) },
                modifier = Modifier.clickable {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Melendez1209/Known/blob/main/LICENSE")
                    )
                    context.startActivity(intent)
                },
                supportingContent = { Text(text = stringResource(id = R.string.policy)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.PrivacyTip,
                        contentDescription = stringResource(id = R.string.privacy)
                    )
                }
            )
            HorizontalDivider()
        }
        item {
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.feedback)) },
                modifier = Modifier.clickable { navTotalController.navigate(Screens.Feedback.router) },
                supportingContent = { Text(text = stringResource(R.string.feedback_to)) },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Feedback,
                        contentDescription = stringResource(R.string.feedback)
                    )
                }
            )
        }
    }
}

@Preview(device = "id:pixel_8_pro")
@Composable
fun About_Content_Preview() {
    About_Content(modifier = Modifier, navTotalController = rememberNavController())
}

@Preview(device = "spec:parent=pixel_8_pro,orientation=landscape")
@Composable
fun AboutScreen_Preview() {
    AboutScreen(
        widthSizeClass = WindowWidthSizeClass.Medium,
        navTotalController = rememberNavController()
    )
}
