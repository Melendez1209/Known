package com.melendez.known.main.inners

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Preview(device = "id:pixel_7_pro")
@Composable
fun Home(paddingValues: PaddingValues?) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            modifier = if (paddingValues != null)
                Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            else Modifier.fillMaxSize(),
            columns = StaggeredGridCells.Adaptive(160.dp),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(20) {
                Card(modifier = Modifier.height(Random.nextInt(140, 320).dp)) {
                    Text(
                        text = stringResource(id = R.string.exam),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 3.dp, horizontal = 3.dp)
                    )
                }
            }
        }
    }
}