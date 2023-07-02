package com.melendez.known.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Flourescent
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MediumTopAppBar
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

@Composable
fun Feedback(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Feedback_CompatExpanded(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium -> Feedback_Medium(navTotalController = navTotalController)
        WindowWidthSizeClass.Expanded -> Feedback_CompatExpanded(navTotalController = navTotalController)
        else -> Feedback_CompatExpanded(navTotalController = navTotalController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feedback_CompatExpanded(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(id = R.string.feedback)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }, scrollBehavior = scrollBehavior
        )
        Feedback_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Feedback_Medium(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        MediumTopAppBar(
            title = { Text(text = stringResource(id = R.string.feedback)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            }, scrollBehavior = scrollBehavior
        )
        Feedback_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@Composable
fun Feedback_Content(modifier: Modifier) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.bug)) },
                    supportingContent = { Text(text = stringResource(R.string.bug_report)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.BugReport,
                            contentDescription = stringResource(id = R.string.bug)
                        )
                    }
                )
                Divider()
            }
            item {
                ListItem(
                    headlineContent = { Text(text = stringResource(R.string.feature)) },
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

@Preview(device = "id:pixel_7_pro")
@Composable
fun Feedback_Medium_Preview() {
    Feedback_Medium(navTotalController = rememberNavController())
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Feedback_CompatExpanded_Preview() {
    Feedback_CompatExpanded(navTotalController = rememberNavController())
}
