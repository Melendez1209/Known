package com.melendez.known.add.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.melendez.known.R

@Composable
fun Inputting(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Inputting_Compact(navTotalController = navTotalController)
        }

        WindowWidthSizeClass.Medium -> {
            Inputting_Medium(navTotalController = navTotalController)
        }

        WindowWidthSizeClass.Expanded -> {
            Inputting_Compact(navTotalController = navTotalController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inputting_Medium(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        MediumTopAppBar(
            title = { Text(text = stringResource(R.string.inputting)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inputting_Compact(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(R.string.inputting)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        )
    }
}

@Composable
fun Inputting_Content(modifier: Modifier) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            modifier = modifier,
            columns = StaggeredGridCells.Adaptive(320.dp)
        ) {
            items(6) {
                Subject_Card("Chinese")
            }
        }
    }
}

@Composable
fun Subject_Card(subject: String) {

    val focusManager = LocalFocusManager.current

    var mark by rememberSaveable { mutableStateOf("") }
    var full by rememberSaveable { mutableStateOf("") }

    Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp),
            text = subject,
            style = MaterialTheme.typography.titleLarge
        )
        Row {
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                value = full,
                singleLine = true,
                label = { Text(stringResource(R.string.full_mark)) },
                placeholder = { Text(text = "150") },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                trailingIcon = {
                    IconButton(onClick = { full = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                onValueChange = { full = it })
            OutlinedTextField(
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                value = mark,
                singleLine = true,
                label = { Text(stringResource(R.string.mark)) },
                placeholder = { Text(text = "150") },
                isError = if (mark.isEmpty() || full.isEmpty()) false else mark.toFloat() > full.toFloat(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    onDone = null
                ),
                trailingIcon = {
                    IconButton(onClick = { mark = "" }) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                onValueChange = { mark = it })
        }
    }
}

@Preview
@Composable
fun Subject_Card_Preview() {
    Subject_Card(subject = "Subject")
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_7_pro")
@Composable
fun InputtingContent_Preview() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Inputting_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
}