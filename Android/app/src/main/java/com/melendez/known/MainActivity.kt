package com.melendez.known

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.melendez.known.add.compose.DRP
import com.melendez.known.main.MainScreen
import com.melendez.known.ui.theme.KnownTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navTotalController = rememberNavController()

            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            KnownTheme {
                NavHost(navController = navTotalController, startDestination = "MainScreen") {
                    composable("MainScreen") {
                        MainScreen(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable("DateRangePicker") { DRP(navTotalController = navTotalController) }
                }
            }
        }
    }
}
