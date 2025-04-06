package com.melendez.known

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.melendez.known.colour.LocalTonalPalettes
import com.melendez.known.colour.TonalPalettes.Companion.toTonalPalettes
import com.melendez.known.ui.components.LocalActivity
import com.melendez.known.ui.components.LocalDarkTheme
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalPaletteStyleIndex
import com.melendez.known.ui.components.LocalSeedColor
import com.melendez.known.ui.components.animatedComposable
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
import com.melendez.known.ui.screens.settings.Appearance
import com.melendez.known.ui.screens.settings.Dark
import com.melendez.known.ui.screens.settings.Language
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.known.ui.theme.KnownTheme
import com.melendez.known.util.DarkThemePreference
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    // Predictive back gesture callback reference
    private var predictiveBackCallback: Any? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialise settings
        val preferenceUtil = PreferenceUtil(application)
        preferenceUtil.forceInitializeSettingsSync()

        // Listen for setting changes and update predictive back gesture accordingly
        lifecycleScope.launch {
            preferenceUtil.settings.collect { settings ->
                val isPredictiveBackEnabled = settings?.predictiveBackEnabled ?: true
                if (isPredictiveBackEnabled) {
                    initPredictiveBackGesture()
                } else {
                    disablePredictiveBackGesture()
                }
            }
        }

        setContent {

            val navTotalController = rememberNavController()
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val viewModelPreferenceUtil: PreferenceUtil = viewModel()
            // Collecting the current settings from the database
            val settings =
                viewModelPreferenceUtil.settings.collectAsStateWithLifecycle(initialValue = null).value
            val isSystemInDarkTheme = isSystemInDarkTheme()
            // Current dark theme settings that should be used
            val darkThemePreference = settings?.let {
                DarkThemePreference(
                    darkThemeValue = it.darkThemeValue,
                    isHighContrastModeEnabled = it.isHighContrastMode
                )
            } ?: DarkThemePreference()
            val isDarkTheme = darkThemePreference.isDarkTheme(isSystemInDarkTheme)

            // Setting the CompositionLocalProvider to Provide Theme Parameters
            CompositionLocalProvider(
                LocalActivity provides this,
                LocalDarkTheme provides darkThemePreference,
                LocalSeedColor provides (settings?.themeColor ?: DEFAULT_SEED_COLOR),
                LocalDynamicColorSwitch provides (settings?.isDynamicColorEnabled ?: false),
                LocalPaletteStyleIndex provides (settings?.paletteStyleIndex ?: 0),
                LocalTonalPalettes provides if (settings?.themeColor != null && settings.themeColor != 0)
                    Color(settings.themeColor).toTonalPalettes()
                else
                    Color(DEFAULT_SEED_COLOR).toTonalPalettes()
            ) {
                KnownTheme(
                    darkTheme = isDarkTheme,
                    isHighContrastModeEnabled = darkThemePreference.isHighContrastModeEnabled
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navTotalController,
                            startDestination = Screens.Main.router
                        ) {
                            animatedComposable(Screens.Main.router) {
                                MainScreen(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Settings.router) {
                                Appearance(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Dark.router) {
                                Dark(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Language.router) {
                                Language(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.DRP.router) { DRP(navTotalController = navTotalController) }
                            animatedComposable(Screens.Inputting.router) {
                                Inputting(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.About.router) {
                                AboutScreen(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Feedback.router) {
                                Feedback(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Bug.router) {
                                Bug(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Feature.router) {
                                Feature(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Signin.router) {
                                Signin(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Detail.router) {
                                Detail(navTotalController = navTotalController)
                            }
                            animatedComposable(Screens.Prophets.router) {
                                Prophets(navTotalController = navTotalController)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initPredictiveBackGesture() {
        if (Build.VERSION.SDK_INT >= 34 && predictiveBackCallback == null) {
            // Using OnBackInvokedDispatcher (Android 13+)
            try {
                Log.d("Melendez", "Initializing predictive back gesture, SDK: ${Build.VERSION.SDK_INT}")
                @Suppress("NewApi")
                val onBackInvokedDispatcher = onBackInvokedDispatcher
                Log.d("Melendez", "Got onBackInvokedDispatcher: $onBackInvokedDispatcher")

                val onBackInvokedCallbackClass =
                    Class.forName("android.window.OnBackInvokedCallback")
                Log.d("Melendez", "Found OnBackInvokedCallback class")
                
                val registerMethod = onBackInvokedDispatcher.javaClass.getMethod(
                    "registerOnBackInvokedCallback",
                    Int::class.java,
                    onBackInvokedCallbackClass
                )
                Log.d("Melendez", "Found register method: $registerMethod")

                // Create a callback instance
                val callback = java.lang.reflect.Proxy.newProxyInstance(
                    onBackInvokedCallbackClass.classLoader,
                    arrayOf(onBackInvokedCallbackClass)
                ) { _, _, _ ->
                    // Trigger standard back operation
                    Log.d("Melendez", "Callback triggered, executing onBackPressed")
                    onBackPressedDispatcher.onBackPressed()
                    null
                }
                Log.d("Melendez", "Created callback proxy: $callback")

                // Register callback using reflection
                registerMethod.invoke(onBackInvokedDispatcher, 0, callback)

                // Save the callback reference so that you can unregister it later
                predictiveBackCallback = callback

                Log.d("Melendez", "Predictive back gesture enabled successfully")
            } catch (e: Exception) {
                Log.e("Melendez", "initPredictiveBackGesture: Exception: ${e.javaClass.simpleName}: ${e.message}")
                e.printStackTrace()
                // When reflection fails, auto degrade to regular back
            }
        } else {
            Log.d("Melendez", "Skipping predictive back initialization: SDK=${Build.VERSION.SDK_INT}, callback=${predictiveBackCallback != null}")
        }
    }

    private fun disablePredictiveBackGesture() {
        if (Build.VERSION.SDK_INT >= 34 && predictiveBackCallback != null) {
            try {
                @Suppress("NewApi")
                val onBackInvokedDispatcher = onBackInvokedDispatcher

                val onBackInvokedCallbackClass =
                    Class.forName("android.window.OnBackInvokedCallback")
                val unregisterMethod = onBackInvokedDispatcher.javaClass.getMethod(
                    "unregisterOnBackInvokedCallback",
                    onBackInvokedCallbackClass
                )

                // Unregister the callback
                unregisterMethod.invoke(onBackInvokedDispatcher, predictiveBackCallback)
                predictiveBackCallback = null

                Log.d("Melendez", "Predictive back gesture disabled")
            } catch (e: Exception) {
                Log.e("Melendez", "disablePredictiveBackGesture: Exception:$e")
            }
        }
    }
}
