package com.melendez.known.ui.screens.main.inners

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.data.entity.ExamWithTotal
import com.melendez.known.svg.DynamicColorImageVectors
import com.melendez.known.svg.drawablevectors.download
import com.melendez.known.ui.navigation.NavigationState
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.viewmodel.ExamViewModel
import com.melendez.known.util.formatDateRange
import com.melendez.known.util.formatScoreInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** Upper bound on the pull-to-refresh indicator, so it cannot outlive a silent re-query. */
private const val REFRESH_TIMEOUT_MILLIS = 500L

@SuppressLint("MemberExtensionConflict")
@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun History(
    paddingValues: PaddingValues? = null,
    navigator: Navigator,
    checkedIds: SnapshotStateList<Long>
) {
    val viewModel: ExamViewModel = viewModel()

    var key by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    // Room already keeps every list live, so a pull-to-refresh just starts a fresh subscription
    var refreshTick by rememberSaveable { mutableIntStateOf(0) }
    var isRefreshing by rememberSaveable { mutableStateOf(false) }

    val examsResult by remember(key, refreshTick) {
        if (key.isBlank()) viewModel.exams else viewModel.search(key.trim())
    }.collectAsStateWithLifecycle(initialValue = null)
    val exams: List<ExamWithTotal> = examsResult.orEmpty()
    // Either this subscription has yet to deliver anything, or the user has just pulled
    val loading = examsResult == null || isRefreshing

    // A refreshed subscription emits as soon as its query returns. The timeout only guarantees the
    // indicator cannot stick when those rows come back identical to the ones already shown.
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(REFRESH_TIMEOUT_MILLIS.milliseconds)
            isRefreshing = false
        }
    }
    val isEditing = checkedIds.isNotEmpty()

    // Selection must never outlive the rows it was taken from, otherwise the tri-state header and
    // the delete action would act on exams the user can no longer see
    LaunchedEffect(exams) {
        checkedIds.retainAll(exams.map { it.exam.id })
    }

    val rowPadding by animateDpAsState(
        targetValue = if (isEditing) 36.dp else 12.dp,
        label = "Checkbox spacing to expand or not"
    )
    val searchbarPadding by animateDpAsState(
        targetValue = if (active) 0.dp else 12.dp,
        label = "SearchBar spacing to expand or not"
    )

    val triState = when {
        exams.isNotEmpty() && exams.all { it.exam.id in checkedIds } -> ToggleableState.On
        exams.any { it.exam.id in checkedIds } -> ToggleableState.Indeterminate
        else -> ToggleableState.Off
    }
    val toggleTriState: () -> Unit = {
        if (triState == ToggleableState.On) {
            checkedIds.clear()
        } else {
            checkedIds.clear()
            checkedIds.addAll(exams.map { it.exam.id })
        }
    }

    fun select(examId: Long, selected: Boolean) {
        if (selected) {
            if (examId !in checkedIds) checkedIds.add(examId)
        } else {
            checkedIds.remove(examId)
        }
    }

    BackHandler(enabled = isEditing) {
        checkedIds.clear()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (exams.isEmpty() && !loading && key.isBlank()) {
                HistoryPlaceholder(
                    text = stringResource(R.string.no_history),
                    modifier = Modifier.weight(1f),
                    action = {
                        TextButton(
                            onClick = { navigator.navigate(Screens.DRP()) },
                            modifier = Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = stringResource(id = R.string.add))
                        }
                    }
                )
            } else {
                SearchBar(
                    query = key,
                    onQueryChange = { key = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = searchbarPadding),
                    placeholder = { Text(text = stringResource(R.string.search)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            enabled = key.isNotBlank() || active,
                            onClick = {
                                if (key.isNotBlank()) {
                                    key = ""
                                } else {
                                    active = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = if (key.isNotBlank()) stringResource(R.string.clear) else stringResource(
                                    R.string.close_bar
                                )
                            )
                        }
                    }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        itemsIndexed(exams, key = { _, item -> item.exam.id }) { index, item ->
                            ListItem(
                                headlineContent = { Text(item.exam.name) },
                                supportingContent = {
                                    Text(
                                        text = formatDateRange(
                                            item.exam.startDate,
                                            item.exam.endDate
                                        )
                                    )
                                },
                                modifier = Modifier.clickable {
                                    active = false
                                    navigator.navigate(Screens.Detail(examId = item.exam.id))
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.School,
                                        contentDescription = item.exam.name
                                    )
                                }
                            )
                            if (index != exams.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }

                if (exams.isEmpty() && !loading) {
                    HistoryPlaceholder(
                        text = stringResource(R.string.no_search_result),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = isEditing) {
                            TriStateCheckbox(state = triState, onClick = toggleTriState)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = rowPadding,
                                    end = 12.dp,
                                    top = 12.dp,
                                    bottom = 12.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.exam),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.time),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.mark),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    val pullToRefreshState: PullToRefreshState = rememberPullToRefreshState()

                    PullToRefreshBox(
                        isRefreshing = loading,
                        onRefresh = {
                            isRefreshing = true
                            refreshTick++
                        },
                        state = pullToRefreshState,
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = pullToRefreshState,
                                isRefreshing = loading,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
                        LazyColumn(
                            modifier =
                                if (paddingValues != null) {
                                    Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                                } else {
                                    Modifier
                                }
                        ) {
                            items(exams, key = { it.exam.id }) { item ->
                                Row(
                                    modifier = Modifier.padding(
                                        top = 6.dp,
                                        bottom = 6.dp,
                                        start = rowPadding,
                                        end = 12.dp
                                    ),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AnimatedVisibility(visible = isEditing) {
                                        Checkbox(
                                            checked = item.exam.id in checkedIds,
                                            onCheckedChange = { select(item.exam.id, it) }
                                        )
                                    }
                                    Card(
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                if (isEditing) {
                                                    select(
                                                        item.exam.id,
                                                        item.exam.id !in checkedIds
                                                    )
                                                } else {
                                                    navigator.navigate(Screens.Detail(examId = item.exam.id))
                                                }
                                            },
                                            onLongClick = {
                                                if (isEditing) {
                                                    checkedIds.clear()
                                                } else {
                                                    checkedIds.add(item.exam.id)
                                                }
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = item.exam.name,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(start = 12.dp),
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = formatDateRange(
                                                    item.exam.startDate,
                                                    item.exam.endDate
                                                ),
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = formatScoreInput(item.totalMark.toString()),
                                                modifier = Modifier.padding(end = 12.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty-state placeholder shared by the two history states: the download illustration over the
 * message that explains why the list is empty, with an optional action underneath. The picture is
 * decorative, so the message alone carries the meaning for screen readers
 */
@Composable
private fun HistoryPlaceholder(
    text: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberVectorPainter(image = DynamicColorImageVectors.download()),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(bottom = 16.dp)
        )
        Text(text = text)
        action()
    }
}

/**
 * Confirmation shown by the bottom bar's delete action. Owning the view model here keeps the three
 * main layouts free of save plumbing; clearing [checkedIds] is what takes every screen back out of
 * multi-select mode.
 */
@Composable
fun DeleteExamsDialog(
    checkedIds: SnapshotStateList<Long>,
    onDismiss: () -> Unit
) {
    val viewModel: ExamViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = stringResource(R.string.delete)
            )
        },
        title = { Text(text = stringResource(R.string.delete)) },
        text = { Text(text = stringResource(R.string.delete_exam_message)) },
        confirmButton = {
            Button(
                onClick = {
                    val examIds = checkedIds.toList()
                    checkedIds.clear()
                    onDismiss()
                    coroutineScope.launch { viewModel.deleteExams(examIds) }
                }
            ) { Text(text = stringResource(R.string.delete)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(device = "id:pixel_10_pro")
@Composable
fun History_Preview() {
    val navigationState = remember {
        NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    History(
        navigator = Navigator(navigationState),
        checkedIds = remember { mutableStateListOf() }
    )
}
