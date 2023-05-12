package com.melendez.known.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun History(navController: NavHostController) {
    LazyColumn {
        item(10) {
            Card {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "考试名称")
                    Text(text = "时间")
                    Text(text = "分类")
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
