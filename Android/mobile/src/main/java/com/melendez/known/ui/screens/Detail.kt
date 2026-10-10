package com.melendez.known.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.data.entity.ExamScore
import com.melendez.known.data.entity.SubjectStat
import com.melendez.known.ui.components.ShareOptionsSheet
import com.melendez.known.ui.components.Tip
import com.melendez.known.ui.components.chart.ComboChart
import com.melendez.known.ui.components.chart.ScorePieChart
import com.melendez.known.ui.navigation.NavigationState
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.viewmodel.ExamViewModel
import com.melendez.known.util.averageDelta
import com.melendez.known.util.settings.examSubjectKeys
import com.melendez.known.util.formatScoreInput
import com.melendez.known.util.percentage
import com.melendez.known.util.rankOf
import com.melendez.known.util.recordedSubjectKeys
import com.melendez.known.util.share.ShareManager
import com.melendez.known.util.settings.subjectKeyToStringResource
import com.melendez.known.util.totalFullMark
import com.melendez.known.util.totalMark
import kotlinx.coroutines.launch

/** `685` rather than `685.0`, matching the way marks are typed on the input screen. */
private fun formatScore(value: Float): String = formatScoreInput(value.toString())

private fun formatPercent(value: Float): String = "%.1f%%".format(value)

private fun formatSigned(value: Float): String = "%+.1f".format(value)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Detail(navigator: Navigator, examId: Long = 0L) {

    val viewModel: ExamViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val examWithScores by remember(examId) { viewModel.examWithScores(examId) }
        .collectAsStateWithLifecycle(initialValue = null)
    val exams by remember { viewModel.exams }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val stats by remember(examId) { viewModel.subjectStats(examId) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val exam = examWithScores?.exam
    val scores = examWithScores?.scores.orEmpty()

    val isFavorite by viewModel.isFavorite(examId)
        .collectAsStateWithLifecycle(initialValue = false)
    val behaviorTop = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var expanded by rememberSaveable { mutableStateOf(true) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
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
                        showDeleteDialog = false
                        coroutineScope.launch {
                            viewModel.deleteExam(examId)
                            navigator.goBack()
                        }
                    }
                ) { Text(text = stringResource(R.string.delete)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showShareSheet && examWithScores != null) {
        val context = LocalContext.current
        val examData = examWithScores!!
        // Resolve subject names in the @Composable context so they are configuration-aware
        val subjectNames = examData.scores.associate { score ->
            score.subjectKey to stringResource(subjectKeyToStringResource(score.subjectKey))
        }
        ShareOptionsSheet(
            onDismiss = { showShareSheet = false },
            onShareAsImage = {
                ShareManager.shareAsImage(
                    context = context,
                    examWithScores = examData,
                    allExams = exams,
                    subjectNameResolver = { key -> subjectNames[key] ?: key }
                )
            },
            onShareAsText = {
                ShareManager.shareAsText(
                    context = context,
                    examWithScores = examData,
                    allExams = exams,
                    subjectStats = stats,
                    subjectNameResolver = { key -> subjectNames[key] ?: key }
                )
            }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = exam?.name ?: stringResource(id = R.string.exam)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = behaviorTop
            )
        }
    ) { innerPadding ->
        // Only subjects that actually carry a score are shown, both as tabs and as rows in the
        // "All" tab. The selection is therefore stored as a subject key rather than an index:
        // when the tab row shrinks, an unknown key simply falls back to the "All" tab instead of
        // pointing at another subject or running past the end of the list
        val scoredKeys = recordedSubjectKeys(examSubjectKeys, scores)
        val courseTabs = listOf("" to stringResource(R.string.all)) +
                scoredKeys.map { it to stringResource(subjectKeyToStringResource(it)) } +
                ("comparison" to stringResource(R.string.comparison))
        val pagerState = rememberPagerState(pageCount = { courseTabs.size })

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            HorizontalFloatingToolbar(
                expanded = expanded,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -ScreenOffset)
                    .zIndex(1f),
                leadingContent = {
                    Tip(null, text = stringResource(R.string.share)) {
                        IconButton(
                            onClick = { showShareSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.share)
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
                trailingContent = {
                    Tip(text = stringResource(R.string.edit)) {
                        IconButton(
                            onClick = {
                                navigator.navigate(
                                    Screens.DRP(
                                        examId = examId,
                                        startDate = exam?.startDate ?: 0L,
                                        endDate = exam?.endDate ?: 0L
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }
                    Tip(text = stringResource(if (isFavorite) R.string.remove_favourite else R.string.add_favourite)) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.toggleFavorite(examId)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                contentDescription = stringResource(if (isFavorite) R.string.remove_favourite else R.string.add_favourite)
                            )
                        }
                    }
                    Tip(text = stringResource(R.string.delete)) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }

                }
            ) {
                FilledIconButton(
                    modifier = Modifier.width(64.dp),
                    onClick = { navigator.navigate(Screens.Prophets) }
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Message,
                        stringResource(R.string.prophets)
                    )
                }
            }

            if (exam == null) {
                // The row has not arrived from Room yet, or it has just been deleted
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {}
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SecondaryScrollableTabRow(selectedTabIndex = pagerState.currentPage) {
                        courseTabs.forEachIndexed { index, (_, title) ->
                            Tab(
                                selected = index == pagerState.currentPage,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                },
                                text = { Text(text = title) }
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(behaviorTop.nestedScrollConnection)
                            .floatingToolbarVerticalNestedScroll(
                                expanded = expanded,
                                onExpand = { expanded = true },
                                onCollapse = { expanded = false }
                            )
                    ) { page ->
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (page == 0) {
                                item(key = "stats") {
                                    StatsCard(
                                        totalMark = totalMark(scores),
                                        totalFullMark = totalFullMark(scores),
                                        rank = rankOf(examId, exams),
                                        examCount = exams.size,
                                        historicalPercentages = exams
                                            .filter { it.exam.id != examId }
                                            .map { percentage(it.totalMark, it.totalFullMark) }
                                    )
                                }
                                item(key = "pie_chart") {
                                    ScorePieChart(scores = scores)
                                }
                                items(scoredKeys, key = { it }) { subjectKey ->
                                    SubjectRow(
                                        label = stringResource(subjectKeyToStringResource(subjectKey)),
                                        score = scores.firstOrNull { it.subjectKey == subjectKey },
                                        stat = stats.firstOrNull { it.subjectKey == subjectKey }
                                    )
                                }
                            } else if (courseTabs[page].first == "comparison") {
                                item(key = "comparison_chart") {
                                    val averageMark = exams
                                        .filter { it.exam.id != examId }
                                        .map { it.totalMark }
                                        .takeIf { it.isNotEmpty() }
                                        ?.average()
                                        ?.toFloat() ?: 0f
                                    ComboChart(
                                        exams = exams,
                                        averageLine = averageMark
                                    )
                                }
                            } else {
                                val subjectKey = courseTabs[page].first
                                item(key = subjectKey) {
                                    SubjectRow(
                                        label = stringResource(subjectKeyToStringResource(subjectKey)),
                                        score = scores.firstOrNull { it.subjectKey == subjectKey },
                                        stat = stats.firstOrNull { it.subjectKey == subjectKey }
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

/** The cross-exam summary shown as the first row of the "All" tab. */
@Composable
private fun StatsCard(
    totalMark: Float,
    totalFullMark: Float,
    rank: Int,
    examCount: Int,
    historicalPercentages: List<Float>
) {
    val currentPercentage = percentage(totalMark, totalFullMark)
    val historicalAverage = historicalPercentages.takeIf { it.isNotEmpty() }
        ?.average()
        ?.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatRow(
                label = stringResource(R.string.total_score),
                value = "${formatScore(totalMark)} / ${formatScore(totalFullMark)}" +
                        " (${formatPercent(currentPercentage)})"
            )
            StatRow(
                label = stringResource(R.string.rank),
                value = "$rank / $examCount"
            )
            if (historicalAverage != null) {
                StatRow(
                    label = stringResource(R.string.history_average),
                    value = formatPercent(historicalAverage) +
                            " (${formatSigned(currentPercentage - historicalAverage)})"
                )
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** One subject's mark, with its distance from the student's own historical average. */
@Composable
private fun SubjectRow(label: String, score: ExamScore?, stat: SubjectStat?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        if (score == null) {
                            "—"
                        } else {
                            "${formatScore(score.mark)} / ${formatScore(score.fullMark)}"
                        },
                    style = MaterialTheme.typography.bodyLarge
                )
                val delta = score?.let { averageDelta(it.mark, it.fullMark, stat) }
                if (stat != null && delta != null) {
                    Text(
                        text = stringResource(R.string.history_average) +
                                " ${formatPercent(percentage(stat.avgMark, stat.avgFullMark))}" +
                                " (${formatSigned(delta)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(device = "id:pixel_10_pro")
@Composable
fun Detail_Preview() {
    val navigationState = remember {
        NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    Detail(navigator = Navigator(navigationState))
}
