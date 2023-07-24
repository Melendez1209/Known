package com.melendez.known.add.compose

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

    var showingDialog by remember { mutableStateOf(false) }
    var examName by rememberSaveable { mutableStateOf("") }

    if (showingDialog) {
        AlertDialog(
            onDismissRequest = {
                showingDialog = false
            },
            title = {
                Text(text = stringResource(id = R.string.name_this))
            },
            text = {
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.exam_name)) }
                )
            },
            confirmButton = {
                Button(
                    enabled = examName.isNotEmpty(),
                    onClick = {
                        showingDialog = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.reserve))
                }
            }, dismissButton = {
                OutlinedButton(
                    onClick = {
                        showingDialog = false
                        examName = ""
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.discard))
                }
            }
        )
    }

    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Inputting_Compact(
            navTotalController = navTotalController,
            onShowingChange = { showingDialog = it },
            examName = examName
        )

        WindowWidthSizeClass.Medium -> Inputting_Medium(
            navTotalController = navTotalController,
            onShowingChange = { showingDialog = it },
            examName = examName
        )

        WindowWidthSizeClass.Expanded -> Inputting_Expanded(
            navTotalController = navTotalController,
            onShowingChange = { showingDialog = it },
            examName = examName
        )

        else -> Inputting_Compact(
            navTotalController = navTotalController,
            onShowingChange = { showingDialog = it },
            examName = examName
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inputting_Compact(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String,
) {

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = {
                TextButton(onClick = { onShowingChange(true) }) {
                    Text(
                        text = examName.ifEmpty { stringResource(R.string.exam) + 0 },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
        )
    }) { padding ->
        Inputting_Content(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inputting_Medium(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        MediumTopAppBar(
            title = {
                TextButton(onClick = { onShowingChange(true) }) {
                    Text(
                        text = examName.ifEmpty { stringResource(R.string.exam) + 0 },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
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
fun Inputting_Expanded(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        LargeTopAppBar(
            title = {
                TextButton(onClick = { onShowingChange(true) }) {
                    Text(
                        text = examName.ifEmpty { stringResource(R.string.exam) + 0 },
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
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


    val chinese = stringResource(R.string.chinese)
    val maths = stringResource(R.string.maths)
    val language = stringResource(R.string.foreign_language)
    val physiotherapy = stringResource(R.string.physiotherapy)
    val chemotherapy = stringResource(R.string.chemotherapy)
    val biology = stringResource(R.string.biology)
    val courseList = listOf(chinese, maths, language, physiotherapy, chemotherapy, biology)

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            modifier = modifier.padding(horizontal = 12.dp),
            columns = StaggeredGridCells.Adaptive(320.dp),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            courseList.forEachIndexed { _, s -> item { Subject_Card(subject = s) } }
        }
    }
}

@Composable
fun Subject_Card(subject: String) {

    val focusManager = LocalFocusManager.current

    var mark by rememberSaveable { mutableStateOf("") }
    var full by rememberSaveable { mutableStateOf("") }

    Card {
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
                    .padding(horizontal = 4.dp, vertical = 6.dp)
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
                    IconButton(onClick = { full = "" }, enabled = full.isNotEmpty()) {
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
                    IconButton(onClick = { mark = "" }, enabled = mark.isNotEmpty()) {
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