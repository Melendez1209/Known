package com.melendez.known.main.inners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(paddingValues: PaddingValues? = null, navTotalController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(160.dp),
            modifier = if (paddingValues != null) Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
            else Modifier.fillMaxSize(),
            verticalItemSpacing = 6.dp,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(20) {
                Card(
                    modifier = Modifier.height(Random.nextInt(130, 290).dp),
                    onClick = { navTotalController.navigate(Screens.Detail.router) }) {
                    Text(
                        text = stringResource(id = R.string.exam),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 3.dp, horizontal = 3.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Home_Preview() {
    Home(navTotalController = rememberNavController())
}