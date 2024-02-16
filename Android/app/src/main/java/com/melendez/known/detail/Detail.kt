package com.melendez.known.detail

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens
import com.melendez.known.components.Tip

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Detail(navTotalController: NavHostController) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

    var isFavorite by remember { mutableStateOf(false) }
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.exam)) },
                navigationIcon = {
                    IconButton(onClick = { navTotalController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                })
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Tip(text = stringResource(R.string.share)) {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Known"
                                )//TODO: replace Known with detail info
                            }
                            launcher.launch(shareIntent)
                        }) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.share)
                            )
                        }
                    }
                    Tip(text = stringResource(R.string.edit)) {
                        IconButton(onClick = { navTotalController.navigate(Screens.DRP.router) }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }
                    Tip(text = stringResource(R.string.favourite)) {
                        IconButton(onClick = { isFavorite = !isFavorite }) {
                            Icon(
                                imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                contentDescription = stringResource(R.string.favourite)
                            )
                        }
                    }
                    Tip(text = stringResource(R.string.delete)) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                    Tip(text = stringResource(R.string.print)) {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Rounded.Print,
                                contentDescription = stringResource(R.string.print)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navTotalController.navigate(Screens.Prophets.router) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Chat,
                            contentDescription = stringResource(R.string.ai_analyse)
                        )
                    }
                }
            )
        }
    ) {
        val courseList = listOf(
            stringResource(R.string.all),
            stringResource(id = R.string.chinese),
            stringResource(id = R.string.maths),
            stringResource(id = R.string.foreign_language),
            stringResource(R.string.physiotherapy),
            stringResource(R.string.chemotherapy),
            stringResource(R.string.biology),
            stringResource(R.string.political),
            stringResource(R.string.history),
            stringResource(R.string.geography)
        )
        var course by remember { mutableIntStateOf(0) }

        Column {
            ScrollableTabRow(
                selectedTabIndex = course,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = it.calculateTopPadding())
            ) {
                courseList.forEachIndexed { index, title ->
                    Tab(
                        selected = course == index,
                        onClick = { course = index },
                        text = { Text(text = title) }
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = it.calculateBottomPadding())
                    .fillMaxWidth()
            ) {
                items(50) {
                    Text(text = "$it")
                }
            }
        }
    }
}

@Preview(device = "id:pixel_8_pro")
@Composable
fun Detail_Preview() {
    Detail(navTotalController = rememberNavController())
}