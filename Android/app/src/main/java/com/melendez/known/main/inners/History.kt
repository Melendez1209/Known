package com.melendez.known.main.inners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(navController: NavHostController) {
    Column {
        TopAppBar(title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.exam_name))
                Text(text = stringResource(R.string.time))
                Text(text = stringResource(R.string.Classification))
            }
        })

        LazyColumn {
            items(100) { count ->
                Card(modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.exam_name) + count.toString(),
                            Modifier.padding(start = 14.dp)
                        )
                        Text(text = stringResource(R.string.time) + count.toString())
                        Text(text = count.toString(), Modifier.padding(end = 18.dp))
                    }
                }
            }
        }
    }
}


@Preview(device = "id:pixel_7_pro")
@Composable
fun HistoryPreview() {
    val nav = rememberNavController()
    History(navController = nav)
}
