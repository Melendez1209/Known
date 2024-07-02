package com.melendez.known.add.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.navigation.compose.rememberNavController
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
            confirmButton = {
                Button(
                    enabled = examName.isNotEmpty(),
                    onClick = {
                        showingDialog = false
                    }
                ) {
                    Text(stringResource(R.string.reserve))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showingDialog = false
                        examName = ""
                    }
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = stringResource(id = R.string.name_this)
                )
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
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
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
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
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
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
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
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
    val political = stringResource(R.string.political)
    val history = stringResource(R.string.history)
    val geography = stringResource(R.string.geography)
    val pe = stringResource(R.string.pe)
    val courseList = listOf(
        chinese,
        maths,
        language,
        physiotherapy,
        chemotherapy,
        biology,
        political,
        history,
        geography,
        pe
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(320.dp),
            modifier = modifier.padding(horizontal = 12.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courseList.forEachIndexed { index, s ->
                item { Subject_Card(subject = s, check = index > 2, isChecked = index != 9) }
            }
        }
    }
}

@Composable
fun Subject_Card(subject: String, check: Boolean = true, isChecked: Boolean = true) {

    val focusManager = LocalFocusManager.current

    var checked by rememberSaveable { mutableStateOf(isChecked) }
    var mark by rememberSaveable { mutableStateOf("") }
    var full by rememberSaveable { mutableStateOf("") }

    Card {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = subject,
                modifier = Modifier
                    .padding(top = 6.dp, start = 10.dp)
                    .align(Alignment.CenterVertically),
                style = MaterialTheme.typography.titleLarge
            )
            if (check) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
        Row(modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)) {
            OutlinedTextField(
                value = full,
                onValueChange = { full = it },
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = checked,
                label = { Text(stringResource(R.string.full_mark)) },
                placeholder = { Text(text = "150") },
                trailingIcon = {
                    IconButton(onClick = { full = "" }, enabled = full.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                singleLine = true
            )
            OutlinedTextField(
                value = mark,
                onValueChange = { mark = it },
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = checked,
                label = { Text(stringResource(R.string.mark)) },
                placeholder = { Text(text = "150") },
                trailingIcon = {
                    IconButton(onClick = { mark = "" }, enabled = mark.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                },
                isError = if (mark.isEmpty() || full.isEmpty()) false else mark.toFloat() > full.toFloat(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    onDone = null
                ),
                singleLine = true
            )
        }
    }
}

@Preview
@Composable
fun Subject_Card_Preview() {
    Subject_Card(subject = "Subject")
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Inputting_Preview() {
    Inputting(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}