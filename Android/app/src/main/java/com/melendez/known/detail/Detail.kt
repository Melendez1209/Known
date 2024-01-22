package com.melendez.known.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Detail(navTotalController: NavHostController) {

    var isFavorite by remember { mutableStateOf(false) }

    Scaffold(
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
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.share)
                        )
                    }
                    IconButton(onClick = { navTotalController.navigate(Screens.DRP.router) }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                    IconButton(onClick = { isFavorite = !isFavorite }) {
                        Icon(
                            imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                            contentDescription = stringResource(R.string.favourite)
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Rounded.Print,
                            contentDescription = stringResource(R.string.print)
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = { navTotalController.navigate(Screens.Prophets.router) }) {
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

        var course = 0

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding())
        ) {
            courseList.forEachIndexed { index, s ->
                item {
                    var selected by rememberSaveable { mutableStateOf(index == course) }

                    FilterChip(
                        selected = selected,
                        onClick = {
                            course = index
                            selected = !selected
                        },
                        label = { Text(text = s) },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }
        }
        LazyColumn {

        }
    }
}

@Preview(device = "id:pixel_8_pro")
@Composable
fun Detail_Preview() {
    Detail(navTotalController = rememberNavController())
}