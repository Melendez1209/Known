package com.melendez.known.ui.components.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.melendez.known.R
import com.melendez.known.data.entity.ExamScore
import com.patrykandpatrick.vico.compose.pie.PieChartHost
import com.patrykandpatrick.vico.compose.pie.data.PieChartModelProducer
import com.patrykandpatrick.vico.compose.pie.data.pieModel
import com.patrykandpatrick.vico.compose.pie.rememberPieChart

/**
 * A pie chart showing the distribution of marks across subjects for one exam.
 *
 * @param scores List of exam scores for each subject.
 * @param modifier Modifier for the chart.
 */
@Composable
fun ScorePieChart(
    scores: List<ExamScore>,
    modifier: Modifier = Modifier
) {
    if (scores.isEmpty()) {
        EmptyChartPlaceholder(
            modifier = modifier,
            text = stringResource(R.string.no_score_data)
        )
        return
    }

    val modelProducer = remember { PieChartModelProducer() }

    LaunchedEffect(scores) {
        modelProducer.runTransaction {
            pieModel {
                series(scores.map { it.mark })
            }
        }
    }

    PieChartHost(
        chart = rememberPieChart(),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
