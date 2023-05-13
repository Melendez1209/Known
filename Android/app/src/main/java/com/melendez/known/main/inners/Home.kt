package com.melendez.known.main.inners

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun Home(navController: NavHostController) {
    Text(text = "Home")
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun HomePreview() {
    val nav = rememberNavController()
    Home(navController = nav)
}