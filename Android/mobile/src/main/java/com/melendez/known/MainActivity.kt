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
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.melendez.known.colour.LocalTonalPalettes
import com.melendez.known.colour.TonalPalettes.Companion.toTonalPalettes
import com.melendez.known.ui.components.LocalActivity
import com.melendez.known.ui.components.LocalDarkTheme
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalPaletteStyleIndex
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.LocalSeedColor
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.navigation.rememberNavigationState
import com.melendez.known.ui.navigation.toEntries
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
import com.melendez.known.util.DarkThemePreference
import com.melendez.known.util.PreferenceUtil
import com.melendez.known.util.getUnifiedSizeClass

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    private val TAG = "Melendez"

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

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val screenType = getUnifiedSizeClass(
                windowHeightSizeClass = windowSizeClass.heightSizeClass,
                windowWidthSizeClass = windowSizeClass.widthSizeClass
            )
            val viewModelPreferenceUtil: PreferenceUtil = viewModel()


            // Collect settings and update loading state
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
                LocalScreenType provides screenType,
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

                        if (settings != null) {
                            key(settings.isFirstLogin) {
                                val startDestination = if (settings.isFirstLogin) {
                                    Screens.Guide
                                } else {
                                    Screens.Main
                                }

                                val navigationState = rememberNavigationState(
                                    startRoute = startDestination,
                                    topLevelRoutes = setOf(
                                        Screens.Main,
                                        Screens.Settings,
                                        Screens.Appearance,
                                        Screens.Dark,
                                        Screens.Language,
                                        Screens.DRP,
                                        Screens.Inputting,
                                        Screens.About,
                                        Screens.Signin,
                                        Screens.Detail,
                                        Screens.Prophets,
                                        Screens.Credits,
                                        Screens.Guide
                                    )
                                )
                                val navigator = remember { Navigator(navigationState) }
                                val entryProvider = entryProvider<NavKey> {
                                    entry<Screens.Guide> {
                                        Guide(navigator = navigator)
                                    }
                                    entry<Screens.Main> {
                                        MainScreen(navigator = navigator)
                                    }
                                    entry<Screens.Appearance> {
                                        Appearance(navigator = navigator)
                                    }
                                    entry<Screens.Settings> {
                                        Settings(navigator = navigator)
                                    }
                                    entry<Screens.Dark> {
                                        Dark(navigator = navigator)
                                    }
                                    entry<Screens.Language> {
                                        Language(navigator = navigator)
                                    }
                                    entry<Screens.DRP> { DRP(navigator = navigator) }
                                    entry<Screens.Inputting> {
                                        Inputting(navigator = navigator)
                                    }
                                    entry<Screens.About> {
                                        About(navigator = navigator)
                                    }
                                    entry<Screens.Signin> {
                                        Signin(navigator = navigator)
                                    }
                                    entry<Screens.Detail> {
                                        Detail(navigator = navigator)
                                    }
                                    entry<Screens.Prophets> {
                                        Prophets(navigator = navigator)
                                    }
                                    entry<Screens.Credits> {
                                        Credits(navigator = navigator)
                                    }
                                }
                                NavDisplay(
                                    entries = navigationState.toEntries(entryProvider),
                                    onBack = { navigator.goBack() },
                                    sceneStrategy = remember { DialogSceneStrategy() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}