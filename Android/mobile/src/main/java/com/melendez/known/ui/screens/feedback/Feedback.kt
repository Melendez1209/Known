package com.melendez.known.ui.screens.feedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Flourescent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feedback(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            title = stringResource(id = R.string.feedback),
            scrollBehavior = scrollBehavior
        )
        Feedback_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@Composable
fun Feedback_Content(modifier: Modifier, navTotalController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.bug)) },
                    modifier = Modifier.clickable { navTotalController.navigate(Screens.Bug.router) },
                    supportingContent = { Text(text = stringResource(R.string.bug_report)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = stringResource(id = R.string.bug)
                        )
                    }
                )
                HorizontalDivider()
            }
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.feature)) },
                    modifier = Modifier.clickable { navTotalController.navigate(Screens.Feature.router) },
                    supportingContent = { Text(text = stringResource(R.string.feature_request)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Flourescent,
                            contentDescription = stringResource(id = R.string.feature_request)
                        )
                    }
                )
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Feedback_Preview() {
    Feedback(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}
