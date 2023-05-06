package com.melendez.known.main.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun History(navController: NavHostController) {
    Text(text = "History")
}


@Preview(device = "id:pixel_7_pro")
@Composable
fun HistoryPreview() {
    val nav = rememberNavController()
    History(navController = nav)
}
