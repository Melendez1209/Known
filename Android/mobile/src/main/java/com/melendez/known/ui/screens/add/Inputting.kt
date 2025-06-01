package com.melendez.known.ui.screens.add

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import kotlin.math.roundToInt

private const val DEFAULT_FULL_MARK = "150"
private const val HALF_POINT = "5"
private val NUMBER_PATTERN = Regex("^\\d*\\.?\\d*$")

@Composable
fun Inputting(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    var showingDialog by remember { mutableStateOf(false) }
    var examName by rememberSaveable { mutableStateOf("") }

    if (showingDialog) {
        ExamNameDialog(
            examName = examName,
            onExamNameChange = { examName = it },
            onDismiss = { showingDialog = false }
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

@Composable
private fun ExamNameDialog(
    examName: String,
    onExamNameChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                enabled = examName.isNotEmpty(),
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.reserve))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDismiss()
                    onExamNameChange("")
                }
            ) {
                Text(stringResource(R.string.discard))
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = stringResource(R.string.name_this)
            )
        },
        title = {
            Text(text = stringResource(R.string.name_this))
        },
        text = {
            OutlinedTextField(
                value = examName,
                onValueChange = onExamNameChange,
                singleLine = true,
                label = { Text(text = stringResource(R.string.exam_name)) }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Inputting_Compact(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isUploading by remember { mutableStateOf(false) }
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
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { isUploading = !isUploading }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Inputting_Medium(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isUploading by remember { mutableStateOf(false) }
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
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { isUploading = !isUploading }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Inputting_Expanded(
    navTotalController: NavHostController,
    onShowingChange: (Boolean) -> Unit,
    examName: String
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var isUploading by remember { mutableStateOf(false) }
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
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(onClick = { isUploading = !isUploading }) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.done)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        )
    }
}

@Composable
private fun Inputting_Content(modifier: Modifier) {
    val courseList = listOf(
        stringResource(R.string.chinese),
        stringResource(R.string.maths),
        stringResource(R.string.foreign_language),
        stringResource(R.string.physiotherapy),
        stringResource(R.string.chemotherapy),
        stringResource(R.string.biology),
        stringResource(R.string.political),
        stringResource(R.string.history),
        stringResource(R.string.geography),
        stringResource(R.string.pe)
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(320.dp),
            modifier = modifier.padding(horizontal = 12.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courseList.forEachIndexed { index, subject ->
                item {
                    Subject_Card(
                        subject = subject,
                        check = index > 2,
                        isChecked = index != 9
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Subject_Card(subject: String, check: Boolean = true, isChecked: Boolean = true) {
    val focusManager = LocalFocusManager.current
    var checked by rememberSaveable { mutableStateOf(isChecked) }
    var mark by rememberSaveable { mutableStateOf("") }
    var full by rememberSaveable { mutableStateOf(DEFAULT_FULL_MARK) }

    fun processScoreInput(input: String): String {
        if (input.isEmpty()) return input
        val processedInput = if (input.endsWith(".")) {
            input + HALF_POINT
        } else {
            input
        }
        return if (isValidScore(processedInput)) {
            formatScore(processedInput)
        } else {
            input
        }
    }

    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (check) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }
            Text(
                text = subject,
                style = MaterialTheme.typography.titleLarge
            )
            Slider(
                value = if (mark.isEmpty() || full.isEmpty()) 0f else mark.toFloat() / full.toFloat(),
                onValueChange = {
                    if (full.isNotEmpty()) {
                        val rawScore = full.toFloat() * it
                        val roundedScore = (rawScore * 2).roundToInt() / 2f
                        mark = if (roundedScore == roundedScore.toInt().toFloat()) {
                            roundedScore.toInt().toString()
                        } else {
                            roundedScore.toString()
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = checked && full.isNotEmpty(),
                steps = (full.toFloat() * 2).toInt() - 1
            )
        }
        Row(modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)) {
            ScoreTextField(
                value = full,
                onValueChange = {
                    if (it.isEmpty() || it.matches(NUMBER_PATTERN)) {
                        val isRemovingDecimalFive = mark.endsWith(".5") && it.endsWith(".")
                        val newValue = if (isRemovingDecimalFive) {
                            it.dropLast(1)
                        } else {
                            processScoreInput(it)
                        }
                        mark = newValue
                    }
                },
                label = stringResource(R.string.full_mark),
                placeholder = DEFAULT_FULL_MARK,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = checked,
                onClear = { full = "" },
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
            ScoreTextField(
                value = mark,
                onValueChange = {
                    if (it.isEmpty() || it.matches(NUMBER_PATTERN)) {
                        val isRemovingDecimalFive = mark.endsWith(".5") && it.endsWith(".")
                        val newValue = if (isRemovingDecimalFive) {
                            it.dropLast(1)
                        } else {
                            processScoreInput(it)
                        }
                        mark = newValue
                    }
                },
                label = stringResource(R.string.mark),
                placeholder = DEFAULT_FULL_MARK,
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = checked,
                onClear = { mark = "" },
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                isError = if (mark.isEmpty() || full.isEmpty()) false else {
                    try {
                        mark.toFloat() > full.toFloat()
                    } catch (_: NumberFormatException) {
                        false
                    }
                }
            )
        }
    }
}

@Composable
private fun ScoreTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier,
    enabled: Boolean,
    onClear: () -> Unit,
    onNext: () -> Unit,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        trailingIcon = {
            IconButton(onClick = onClear, enabled = value.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear)
                )
            }
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Next,
            keyboardType = KeyboardType.Decimal
        ),
        keyboardActions = KeyboardActions(onNext = { onNext() }),
        singleLine = true
    )
}

private fun isValidScore(input: String): Boolean {
    if (input.isEmpty()) return true
    return try {
        val value = input.toFloat()
        value >= 0 && (value == value.toInt().toFloat() || input.endsWith(".5"))
    } catch (_: NumberFormatException) {
        false
    }
}

private fun formatScore(input: String): String {
    if (input.isEmpty()) return ""
    return try {
        val value = input.toFloat()
        if (value == value.toInt().toFloat()) {
            value.toInt().toString()
        } else {
            input
        }
    } catch (_: NumberFormatException) {
        input
    }
}

@Preview
@Composable
private fun Subject_Card_Preview() {
    Subject_Card(subject = "Subject")
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun Inputting_Preview() {
    Inputting(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}