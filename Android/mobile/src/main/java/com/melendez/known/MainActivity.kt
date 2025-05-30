package com.melendez.known

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.melendez.known.colour.LocalTonalPalettes
import com.melendez.known.colour.TonalPalettes.Companion.toTonalPalettes
import com.melendez.known.ui.components.LocalActivity
import com.melendez.known.ui.components.LocalDarkTheme
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalPaletteStyleIndex
import com.melendez.known.ui.components.LocalSeedColor
import com.melendez.known.ui.components.animatedComposable
import com.melendez.known.ui.screens.Detail
import com.melendez.known.ui.screens.Guide
import com.melendez.known.ui.screens.Prophets
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.screens.Signin
import com.melendez.known.ui.screens.about.About
import com.melendez.known.ui.screens.about.Credits
import com.melendez.known.ui.screens.add.DRP
import com.melendez.known.ui.screens.add.Inputting
import com.melendez.known.ui.screens.main.MainScreen
import com.melendez.known.ui.screens.settings.Settings
import com.melendez.known.ui.screens.settings.appearance.Appearance
import com.melendez.known.ui.screens.settings.appearance.Dark
import com.melendez.known.ui.screens.settings.appearance.Language
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.known.ui.theme.KnownTheme
import com.melendez.known.util.AppStartupManager
import com.melendez.known.util.DarkThemePreference
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    // Predictive back gesture callback reference
    private val TAG = "Melendez"
    private var predictiveBackCallback: Any? = null
    
    // Flag to indicate UI is fully loaded
    private var isUIFullyLoaded = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission has been granted")
        } else {
            Log.d(TAG, "Notification permission denied")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "Notification permission has been granted")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        askNotificationPermission()
        // Get FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "获取 FCM 令牌失败", task.exception)
                return@OnCompleteListener
            }
            // Get a new token
            val token = task.result
            Log.d(TAG, "FCM 令牌: $token")
            // TODO: Send the token to the back-end server
        })

        // Listen for setting changes and update predictive back gesture accordingly
        lifecycleScope.launch {
            try {
                val preferenceUtil = PreferenceUtil(application)
                preferenceUtil.settings.collect { settings ->
                    val isPredictiveBackEnabled = settings?.predictiveBackEnabled ?: true
                    if (isPredictiveBackEnabled) {
                        initPredictiveBackGesture()
                    } else {
                        disablePredictiveBackGesture()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "监听设置变化时出错: ${e.message}", e)
            }
        }

        setContent {
            val navTotalController = rememberNavController()
            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass
            val viewModelPreferenceUtil: PreferenceUtil = viewModel()


            // Collect settings and update loading state
            val settings =
                viewModelPreferenceUtil.settings.collectAsStateWithLifecycle(initialValue = null).value

            // Improved loading logic that checks both database readiness and settings availability
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    // First check if database is ready from App class
                    val app = application as App
                    var attempts = 0
                    val maxAttempts = 30 // 3 seconds with 100ms intervals

                    while (!app.isDatabaseReady() && attempts < maxAttempts) {
                        delay(100)
                        attempts++
                    }

                    if (app.isDatabaseReady()) {
                        Log.d(TAG, "Database is ready, waiting for settings to load")
                        // Database is ready, now wait a bit more for settings to be available
                        delay(500) // Additional buffer for settings to propagate
                    } else {
                        Log.w(TAG, "Database readiness check timed out")
                    }
                }
            }

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

                        val startDestination = when {
                            settings == null -> Screens.Main.router
                            settings.isFirstLogin -> Screens.Guide.router
                            else -> Screens.Main.router
                        }

                        NavHost(
                            navController = navTotalController,
                            startDestination = startDestination
                        ) {
                            animatedComposable(Screens.Guide.router) {
                                Guide(navTotalController = navTotalController)
                            }
                            animatedComposable(Screens.Main.router) {
                                MainScreen(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Appearance.router) {
                                Appearance(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                            animatedComposable(Screens.Settings.router) {
                                Settings(
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
                                About(
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
                            animatedComposable(Screens.Credits.router) {
                                Credits(
                                    widthSizeClass = widthSizeClass,
                                    navTotalController = navTotalController
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Mark UI as fully loaded after content is set
        isUIFullyLoaded = true
        Log.d(TAG, "MainActivity UI fully loaded")
        
        // Notify startup manager that main UI is ready
        AppStartupManager.markMainUIReady()
    }

    private fun initPredictiveBackGesture() {
        if (Build.VERSION.SDK_INT >= 34 && predictiveBackCallback == null) {
            // Using OnBackInvokedDispatcher (Android 13+)
            try {
                Log.d(
                    "Melendez",
                    "Initializing predictive back gesture, SDK: ${Build.VERSION.SDK_INT}"
                )
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
                Log.e(
                    "Melendez",
                    "initPredictiveBackGesture: Exception: ${e.javaClass.simpleName}: ${e.message}"
                )
                e.printStackTrace()
                // When reflection fails, auto degrade to regular back
            }
        } else {
            Log.d(
                "Melendez",
                "Skipping predictive back initialization: SDK=${Build.VERSION.SDK_INT}, callback=${predictiveBackCallback != null}"
            )
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
