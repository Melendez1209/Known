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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.data.entity.Exam
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.viewmodel.ExamViewModel
import com.melendez.known.util.COMPULSORY_SUBJECT_COUNT
import com.melendez.known.util.DEFAULT_FULL_MARK
import com.melendez.known.util.settings.PreferenceUtil
import com.melendez.known.util.ScoreInput
import com.melendez.known.util.ScoreValidation
import com.melendez.known.util.ScreenType
import com.melendez.known.util.buildExamScores
import com.melendez.known.util.defaultExamName
import com.melendez.known.util.defaultScoreInputs
import com.melendez.known.util.settings.examSubjectKeys
import com.melendez.known.util.formatScoreInput
import com.melendez.known.util.parseScoreOrNull
import com.melendez.known.util.processScoreInput
import com.melendez.known.util.sliderSteps
import com.melendez.known.util.sliderValue
import com.melendez.known.util.snappedScore
import com.melendez.known.util.settings.subjectKeyToStringResource
import com.melendez.known.util.settings.toSubjectKeySet
import com.melendez.known.util.validateScoreInputs
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private val NUMBER_PATTERN = Regex("^\\d*\\.?\\d*$")

/**
 * Persists the ten score rows across rotation and process death. The rows are flattened to
 * `[subjectKey, checked, mark, fullMark]` quarters because a plain `List<ScoreInput>` cannot be
 * written to a `Bundle` by the default saver.
 */
private val scoreInputsSaver = listSaver<List<ScoreInput>, String>(
    save = { inputs ->
        inputs.flatMap { listOf(it.subjectKey, it.checked.toString(), it.mark, it.fullMark) }
    },
    restore = { flat ->
        flat.chunked(4)
            .filter { it.size == 4 }
            .map {
                ScoreInput(
                    subjectKey = it[0],
                    checked = it[1].toBoolean(),
                    mark = it[2],
                    fullMark = it[3]
                )
            }
    }
)

@Composable
fun Inputting(
    navigator: Navigator,
    startDate: Long = 0L,
    endDate: Long = 0L,
    examId: Long = 0L
) {
    val viewModel: ExamViewModel = viewModel()
    val preferenceUtil: PreferenceUtil = viewModel()
    val coroutineScope = rememberCoroutineScope()
    val screenType = LocalScreenType.current

    val settings by preferenceUtil.settings.collectAsStateWithLifecycle(initialValue = null)

    var showingDialog by remember { mutableStateOf(false) }
    var examName by rememberSaveable { mutableStateOf("") }
    var scoreInputs by rememberSaveable(stateSaver = scoreInputsSaver) {
        mutableStateOf(defaultScoreInputs(emptySet()))
    }
    var isSaving by remember { mutableStateOf(false) }
    var validation by remember { mutableStateOf(ScoreValidation.VALID) }
    var subjectsApplied by rememberSaveable { mutableStateOf(false) }

    // Editing reuses this route, so the stored exam pre-fills the form the first time it arrives
    val existingExam by remember(examId) {
        if (examId != 0L) viewModel.examWithScores(examId) else emptyFlow()
    }.collectAsStateWithLifecycle(initialValue = null)
    var prefilled by rememberSaveable { mutableStateOf(false) }

    // Fill the form exactly once: the initial `null` is only the collection placeholder, and a
    // second emission after an edit must not clobber what the user has already typed
    LaunchedEffect(existingExam, examId) {
        val loaded = existingExam
        if (examId == 0L || prefilled || loaded == null) return@LaunchedEffect
        prefilled = true
        examName = loaded.exam.name
        scoreInputs = examSubjectKeys.mapIndexed { index, key ->
            val stored = loaded.scores.firstOrNull { it.subjectKey == key }
            ScoreInput(
                subjectKey = key,
                // The compulsory subjects are always ticked; optional ones are ticked only when
                // they already carry a score
                checked = index < COMPULSORY_SUBJECT_COUNT || stored != null,
                mark = stored?.mark?.let { formatScoreInput(it.toString()) }.orEmpty(),
                fullMark = stored?.fullMark?.let { formatScoreInput(it.toString()) }
                    ?: DEFAULT_FULL_MARK
            )
        }
    }

    // The settings row reaches Room after the first composition, so the student's subject
    // selection is applied the moment it arrives - and only once. An edit is filled from the
    // stored exam instead, and a mark the user has already typed is never overwritten
    LaunchedEffect(settings, examId) {
        val stored = settings ?: return@LaunchedEffect
        if (examId != 0L || subjectsApplied) return@LaunchedEffect
        if (scoreInputs.any { it.mark.isNotBlank() }) return@LaunchedEffect
        subjectsApplied = true
        scoreInputs = defaultScoreInputs(stored.selectedSubjects.toSubjectKeySet())
    }

    fun save() {
        val result = validateScoreInputs(scoreInputs)
        if (result != ScoreValidation.VALID) {
            validation = result
            return
        }
        validation = ScoreValidation.VALID
        isSaving = true
        coroutineScope.launch {
            val exam = Exam(
                id = examId,
                name = examName.ifEmpty { defaultExamName(startDate) },
                startDate = startDate,
                endDate = endDate,
                createdAt = existingExam?.exam?.createdAt ?: System.currentTimeMillis()
            )
            val scores = buildExamScores(examId, scoreInputs)
            if (examId == 0L) {
                viewModel.insertExam(exam, scores)
            } else {
                viewModel.updateExam(exam, scores)
            }
            isSaving = false
            // The date picker sits underneath this screen, so both the creates and the edit flow need two steps to land back where they started
            navigator.goBack()
            navigator.goBack()
        }
    }

    if (showingDialog) {
        ExamNameDialog(
            examName = examName,
            onExamNameChange = { examName = it },
            onDismiss = { showingDialog = false }
        )
    }

    val isBusy = isSaving || (examId != 0L && existingExam == null)
    val onScoreInputsChange: (List<ScoreInput>) -> Unit = { updated ->
        scoreInputs = updated
        validation = ScoreValidation.VALID
    }

    when (screenType) {
        ScreenType.Compact -> Inputting_Compact(
            navigator = navigator,
            onShowingChange = { showingDialog = it },
            examName = examName,
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange,
            onSave = ::save,
            isBusy = isBusy,
            validation = validation
        )

        ScreenType.Medium -> Inputting_Medium(
            navigator = navigator,
            onShowingChange = { showingDialog = it },
            examName = examName,
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange,
            onSave = ::save,
            isBusy = isBusy,
            validation = validation
        )

        ScreenType.Expanded -> Inputting_Expanded(
            navigator = navigator,
            onShowingChange = { showingDialog = it },
            examName = examName,
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange,
            onSave = ::save,
            isBusy = isBusy,
            validation = validation
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

@Composable
private fun ExamNameButton(examName: String, onShowingChange: (Boolean) -> Unit) {
    TextButton(onClick = { onShowingChange(true) }) {
        Text(
            text = examName.ifEmpty { stringResource(R.string.exam) + 0 },
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun BackButton(navigator: Navigator) {
    IconButton(onClick = { navigator.goBack() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}

@Composable
private fun SaveButton(onSave: () -> Unit, isBusy: Boolean) {
    IconButton(onClick = onSave, enabled = !isBusy) {
        Icon(
            imageVector = Icons.Rounded.Done,
            contentDescription = stringResource(R.string.done)
        )
    }
}

/** Loading/saving feedback plus the reason a save was refused, shared by all three layouts. */
@Composable
private fun InputtingStatus(isBusy: Boolean, validation: ScoreValidation) {
    if (isBusy) {
        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
    if (validation != ScoreValidation.VALID) {
        Text(
            text = stringResource(
                if (validation == ScoreValidation.NO_MARK_ENTERED) {
                    R.string.no_mark_entered
                } else {
                    R.string.mark_exceeds_full
                }
            ),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun Inputting_Compact(
    navigator: Navigator,
    onShowingChange: (Boolean) -> Unit,
    examName: String,
    scoreInputs: List<ScoreInput>,
    onScoreInputsChange: (List<ScoreInput>) -> Unit,
    onSave: () -> Unit,
    isBusy: Boolean,
    validation: ScoreValidation
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column {
        CenterAlignedTopAppBar(
            title = { ExamNameButton(examName, onShowingChange) },
            navigationIcon = { BackButton(navigator) },
            actions = { SaveButton(onSave, isBusy) },
            scrollBehavior = scrollBehavior
        )
        InputtingStatus(isBusy, validation)
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange
        )
    }
}

@Composable
private fun Inputting_Medium(
    navigator: Navigator,
    onShowingChange: (Boolean) -> Unit,
    examName: String,
    scoreInputs: List<ScoreInput>,
    onScoreInputsChange: (List<ScoreInput>) -> Unit,
    onSave: () -> Unit,
    isBusy: Boolean,
    validation: ScoreValidation
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column {
        MediumTopAppBar(
            title = { ExamNameButton(examName, onShowingChange) },
            navigationIcon = { BackButton(navigator) },
            actions = { SaveButton(onSave, isBusy) },
            scrollBehavior = scrollBehavior
        )
        InputtingStatus(isBusy, validation)
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange
        )
    }
}

@Composable
private fun Inputting_Expanded(
    navigator: Navigator,
    onShowingChange: (Boolean) -> Unit,
    examName: String,
    scoreInputs: List<ScoreInput>,
    onScoreInputsChange: (List<ScoreInput>) -> Unit,
    onSave: () -> Unit,
    isBusy: Boolean,
    validation: ScoreValidation
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column {
        LargeTopAppBar(
            title = { ExamNameButton(examName, onShowingChange) },
            navigationIcon = { BackButton(navigator) },
            actions = { SaveButton(onSave, isBusy) },
            scrollBehavior = scrollBehavior
        )
        InputtingStatus(isBusy, validation)
        Inputting_Content(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize(),
            scoreInputs = scoreInputs,
            onScoreInputsChange = onScoreInputsChange
        )
    }
}

@Composable
private fun Inputting_Content(
    modifier: Modifier,
    scoreInputs: List<ScoreInput>,
    onScoreInputsChange: (List<ScoreInput>) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(320.dp),
            modifier = modifier.padding(horizontal = 12.dp),
            verticalItemSpacing = 12.dp,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            scoreInputs.forEachIndexed { index, input ->
                item(key = input.subjectKey) {
                    Subject_Card(
                        subject = stringResource(subjectKeyToStringResource(input.subjectKey)),
                        check = index >= COMPULSORY_SUBJECT_COUNT,
                        input = input,
                        onInputChange = { updated ->
                            onScoreInputsChange(
                                scoreInputs.toMutableList().also { it[index] = updated }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Subject_Card(
    subject: String,
    check: Boolean,
    input: ScoreInput,
    onInputChange: (ScoreInput) -> Unit
) {
    val focusManager = LocalFocusManager.current

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
                    checked = input.checked,
                    onCheckedChange = { onInputChange(input.copy(checked = it)) }
                )
            }
            Text(
                text = subject,
                style = MaterialTheme.typography.titleLarge
            )
            Slider(
                value = sliderValue(input.mark, input.fullMark),
                onValueChange = {
                    onInputChange(input.copy(mark = snappedScore(it, input.fullMark)))
                },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = input.checked && input.fullMark.isNotEmpty(),
                steps = sliderSteps(input.fullMark)
            )
        }
        Row(modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)) {
            ScoreTextField(
                value = input.fullMark,
                onValueChange = {
                    if (it.isEmpty() || it.matches(NUMBER_PATTERN)) {
                        // Deleting the auto-completed ".5" must fall back to the bare dot
                        val isRemovingDecimalFive =
                            input.fullMark.endsWith(".5") && it.endsWith(".")
                        val newValue = if (isRemovingDecimalFive) {
                            it.dropLast(1)
                        } else {
                            processScoreInput(it)
                        }
                        onInputChange(input.copy(fullMark = newValue))
                    }
                },
                label = stringResource(R.string.full_mark),
                placeholder = DEFAULT_FULL_MARK,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = input.checked,
                onClear = { onInputChange(input.copy(fullMark = "")) },
                onNext = { focusManager.moveFocus(FocusDirection.Right) }
            )
            ScoreTextField(
                value = input.mark,
                onValueChange = {
                    if (it.isEmpty() || it.matches(NUMBER_PATTERN)) {
                        val isRemovingDecimalFive = input.mark.endsWith(".5") && it.endsWith(".")
                        val newValue = if (isRemovingDecimalFive) {
                            it.dropLast(1)
                        } else {
                            processScoreInput(it)
                        }
                        onInputChange(input.copy(mark = newValue))
                    }
                },
                label = stringResource(R.string.mark),
                placeholder = DEFAULT_FULL_MARK,
                modifier = Modifier
                    .padding(horizontal = 3.dp, vertical = 6.dp)
                    .weight(1f),
                enabled = input.checked,
                onClear = { onInputChange(input.copy(mark = "")) },
                onNext = { focusManager.moveFocus(FocusDirection.Next) },
                isError = isMarkAboveFullMark(input.mark, input.fullMark)
            )
        }
    }
}

/** Whether a typed mark cannot be saved against its full mark, e.g. `90` out of `50`. */
private fun isMarkAboveFullMark(mark: String, fullMark: String): Boolean {
    if (mark.isBlank() || fullMark.isBlank()) return false
    val markValue = parseScoreOrNull(mark) ?: return false
    val fullValue = parseScoreOrNull(fullMark) ?: return false
    return markValue > fullValue
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

@Preview(device = "id:pixel_10_pro")
@Composable
private fun Subject_Card_Preview() {
    Subject_Card(
        subject = "Subject",
        check = true,
        input = ScoreInput(subjectKey = "physics"),
        onInputChange = {}
    )
}

@Preview(device = "id:pixel_10_pro")
@Composable
private fun Inputting_Preview() {
    val navigationState = remember {
        com.melendez.known.ui.navigation.NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    Inputting(navigator = Navigator(navigationState))
}
