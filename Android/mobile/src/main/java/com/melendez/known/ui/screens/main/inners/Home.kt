package com.melendez.known.ui.screens.main.inners

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.melendez.known.R
import com.melendez.known.ui.components.chart.SubjectBarChart
import com.melendez.known.ui.components.chart.TrendChart
import com.melendez.known.ui.viewmodel.exam.ExamViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    onExamClick: (Long) -> Unit = {},
    viewModel: ExamViewModel = viewModel()
) {
    val exams by viewModel.exams.collectAsStateWithLifecycle(initialValue = emptyList())
    val stats by viewModel.subjectStats(0L).collectAsStateWithLifecycle(initialValue = emptyList())

    Surface {
        val items =
            listOf(
                CarouselItem(0, R.drawable.sample0, R.string.sample),
                CarouselItem(1, R.drawable.sample1, R.string.sample),
                CarouselItem(2, R.drawable.sample2, R.string.sample),
                CarouselItem(3, R.drawable.sample3, R.string.sample)
            )

        Column(
            modifier = Modifier
                .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { items.count() },
                preferredItemWidth = 260.dp,
                modifier = Modifier.fillMaxWidth(),
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { i ->
                val item = items[i]
                Image(
                    painter = rememberAsyncImagePainter(item.drawableRes),
                    contentDescription = stringResource(item.contentDescriptionResId) + i,
                    modifier = Modifier
                        .height(220.dp)
                        .maskClip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Crop
                )
            }

            if (exams.isNotEmpty()) {
                ChartCard(
                    title = stringResource(R.string.score_trend),
                    content = { TrendChart(exams = exams, onExamClick = onExamClick) }
                )

                ChartCard(
                    title = stringResource(R.string.subject_average),
                    content = { SubjectBarChart(stats = stats) }
                )
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Home_Preview() {
    Home()
}

data class CarouselItem(
    val id: Int,
    @DrawableRes val drawableRes: Int,
    @StringRes val contentDescriptionResId: Int,
)
