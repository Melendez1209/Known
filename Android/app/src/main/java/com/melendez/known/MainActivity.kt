package com.melendez.known

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.melendez.known.add.compose.DRP
import com.melendez.known.add.compose.Inputting
import com.melendez.known.main.MainScreen
import com.melendez.known.settings.compose.Settings
import com.melendez.known.ui.theme.KnownTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val navTotalController = rememberAnimatedNavController()
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            KnownTheme {
                AnimatedNavHost(
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullHeight -> fullHeight },
                            animationSpec = tween(350)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> (-fullWidth * 0.3f).toInt() },
                            animationSpec = tween(350)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> (-fullWidth * 0.3f).toInt() },
                            animationSpec = tween(350)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(350)
                        )
                    },
                    navController = navTotalController,
                    startDestination = "MainScreen"
                ) {
                    composable("MainScreen") {
                        MainScreen(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable("DateRangePicker") { DRP(navTotalController = navTotalController) }
                    composable("Settings") {
                        Settings(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable("Inputting") { Inputting() }
                }
            }
        }
    }
}
