package com.melendez.known

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.melendez.known.about.AboutScreen
import com.melendez.known.add.compose.DRP
import com.melendez.known.add.compose.Inputting
import com.melendez.known.main.MainScreen
import com.melendez.known.settings.compose.Settings
import com.melendez.known.theme.KnownTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            // Set the content colour of the status bar
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (isSystemInDarkTheme()) {
                    window.insetsController?.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    window.insetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            } else {
                if (isSystemInDarkTheme()) {
                    val controller = ViewCompat.getWindowInsetsController(LocalView.current)
                    if (controller != null) {
                        controller.isAppearanceLightStatusBars = false
                    }
                } else {
                    val controller = ViewCompat.getWindowInsetsController(LocalView.current)
                    if (controller != null) {
                        controller.isAppearanceLightStatusBars = true
                    }
                }
            }

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
                    composable(Screens.Main.router) {
                        MainScreen(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Settings.router) {
                        Settings(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.DRP.router) { DRP(navTotalController = navTotalController) }
                    composable(Screens.Inputting.router) { Inputting() }
                    composable(Screens.About.router) { AboutScreen() }
                }
            }
        }
    }
}
