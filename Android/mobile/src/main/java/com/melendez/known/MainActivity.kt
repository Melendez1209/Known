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
import com.melendez.known.ui.screens.AboutScreen
import com.melendez.known.ui.screens.Detail
import com.melendez.known.ui.screens.Prophets
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.screens.Signin
import com.melendez.known.ui.screens.add.DRP
import com.melendez.known.ui.screens.add.Inputting
import com.melendez.known.ui.screens.feedback.Bug
import com.melendez.known.ui.screens.feedback.Feature
import com.melendez.known.ui.screens.feedback.Feedback
import com.melendez.known.ui.screens.main.MainScreen
import com.melendez.known.ui.screens.settings.Dark
import com.melendez.known.ui.screens.settings.Language
import com.melendez.known.ui.screens.settings.Settings
import com.melendez.known.ui.theme.KnownTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request per

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
                    startDestination = Screens.Main.router
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
                    composable(Screens.Dark.router) {
                        Dark()
                    }
                    composable(Screens.Language.router) {
                        Language()
                    }
                    composable(Screens.DRP.router) { DRP(navTotalController = navTotalController) }
                    composable(Screens.Inputting.router) {
                        Inputting(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.About.router) {
                        AboutScreen(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Feedback.router) {
                        Feedback(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Bug.router) {
                        Bug(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Feature.router) {
                        Feature(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Signin.router) {
                        Signin(
                            widthSizeClass = widthSizeClass,
                            navTotalController = navTotalController
                        )
                    }
                    composable(Screens.Detail.router) {
                        Detail(navTotalController = navTotalController)
                    }
                    composable(Screens.Prophets.router) {
                        Prophets(navTotalController = navTotalController)
                    }
                }
            }
        }
    }
}
