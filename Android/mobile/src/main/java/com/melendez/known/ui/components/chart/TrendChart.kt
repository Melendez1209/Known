package com.melendez.known.ui.components.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.melendez.known.R
import com.melendez.known.data.entity.ExamWithTotal
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill

/**
 * A line chart showing the trend of total marks across exams.
 *
 * @param exams List of exams with their total marks, ordered by date.
 * @param onExamClick Callback invoked when a data point is clicked, receiving the exam ID.
 */
@Composable
fun TrendChart(
    exams: List<ExamWithTotal>,
    modifier: Modifier = Modifier,
    onExamClick: ((Long) -> Unit)? = null
) {
    if (exams.isEmpty()) {
        EmptyChartPlaceholder(
            modifier = modifier,
            text = stringResource(R.string.no_exam_data)
        )
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(exams) {
        modelProducer.runTransaction {
            lineModel { series(exams.map { it.totalMark }) }
        }
    }

    val lineColor = MaterialTheme.colorScheme.primary

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.Line(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor))
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * A placeholder shown when there is no data to display in a chart.
 */
@Composable
fun EmptyChartPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
