package com.melendez.known.ui.screens.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.android.material.color.DynamicColors
import com.melendez.known.R
import com.melendez.known.colour.LocalTonalPalettes
import com.melendez.known.colour.PaletteStyle
import com.melendez.known.colour.TonalPalettes
import com.melendez.known.colour.TonalPalettes.Companion.toTonalPalettes
import com.melendez.known.colour.a1
import com.melendez.known.colour.a2
import com.melendez.known.colour.a3
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalPaletteStyleIndex
import com.melendez.known.ui.components.LocalSeedColor
import com.melendez.known.ui.components.PreferenceItem
import com.melendez.known.ui.components.PreferenceSwitch
import com.melendez.known.ui.components.PreferenceSwitchWithDivider
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.theme.DEFAULT_SEED_COLOR
import com.melendez.known.util.DarkThemePreference
import com.melendez.known.util.PreferenceUtil
import com.melendez.known.util.STYLE_MONOCHROME
import com.melendez.known.util.STYLE_TONAL_SPOT
import com.melendez.known.util.paletteStyles
import com.melendez.known.util.toDisplayName
import io.material.hct.material.hct.Hct
import java.util.Locale


private val ColorList =
    ((4..10) + (1..3)).map { it * 35.0 }.map { Color(Hct.from(it, 40.0, 40.0).toInt()) }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Appearance(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            title = stringResource(R.string.look_and_feel),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        Appearance_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission", "MutableCollectionMutableState", "MemberExtensionConflict")
@Composable
fun Appearance_Content(modifier: Modifier, navTotalController: NavHostController) {
    val preferenceUtil: PreferenceUtil = viewModel()
    val settings = preferenceUtil.settings.collectAsStateWithLifecycle(initialValue = null)
    val isSystemInDarkTheme = isSystemInDarkTheme()

    Surface(modifier.fillMaxSize()) {

        val pageCount = ColorList.size + 1

        val pagerState =
            rememberPagerState(
                initialPage =
                    if (LocalPaletteStyleIndex.current == STYLE_MONOCHROME) pageCount
                    else
                        ColorList.indexOf(Color(LocalSeedColor.current)).run {
                            if (this == -1) 0 else this
                        }
            ) {
                pageCount
            }
        LazyColumn(modifier = modifier) {
            item {
                Column {
                    // Theme colour selector
                    val currentColorSelection =
                        if (settings.value?.themeColor != null && settings.value?.themeColor != 0) {
                            Color(settings.value!!.themeColor)
                        } else {
                            // If not set in the database or set to 0, the default colour is used
                            Color(DEFAULT_SEED_COLOR)
                        }

                    val paletteStyleIndex = settings.value?.paletteStyleIndex ?: 0

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .fillMaxWidth()
                            .height(100.dp)
                    ) { page ->
                        if (page == 0) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                paletteStyles.subList(STYLE_TONAL_SPOT, STYLE_MONOCHROME)
                                    .forEachIndexed { index, style ->
                                        ColorButton(
                                            color = currentColorSelection,
                                            index = index,
                                            tonalStyle = style,
                                            preferenceUtil = preferenceUtil,
                                            isSelected = (!LocalDynamicColorSwitch.current &&
                                                    (settings.value?.themeColor == currentColorSelection.toArgb()) &&
                                                    paletteStyleIndex == index)
                                        )
                                    }
                            }
                        } else {
                            val color = ColorList[page - 1]
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ColorButtons(color = color, preferenceUtil = preferenceUtil)
                            }
                        }
                    }
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = pageCount,
                        modifier =
                            Modifier
                                .clearAndSetSemantics {}
                                .align(Alignment.CenterHorizontally)
                                .padding(vertical = 12.dp),
                        activeColor = MaterialTheme.colorScheme.primary,
                        inactiveColor = MaterialTheme.colorScheme.outlineVariant,
                        indicatorHeight = 6.dp,
                        indicatorWidth = 6.dp,
                    )
                }
            }
            if (DynamicColors.isDynamicColorAvailable()) {
                item {

                    val isDynamicColorEnabled = LocalDynamicColorSwitch.current

                    PreferenceSwitch(
                        title = stringResource(id = R.string.dynamic_color),
                        description = stringResource(id = R.string.dynamic_color_desc),
                        icon = Icons.Rounded.Colorize,
                        isChecked = isDynamicColorEnabled,
                        onClick = {
                            preferenceUtil.switchDynamicColor(enabled = !isDynamicColorEnabled)
                        },
                    )
                }
            }
            item {

                val darkThemePreference = DarkThemePreference(
                    darkThemeValue = settings.value?.darkThemeValue
                        ?: DarkThemePreference.FOLLOW_SYSTEM,
                    isHighContrastModeEnabled = settings.value?.isHighContrastMode == true
                )

                val isDarkTheme = darkThemePreference.isDarkTheme(isSystemInDarkTheme)

                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    icon = if (isDarkTheme)
                        Icons.Rounded.DarkMode
                    else Icons.Rounded.LightMode,
                    isChecked = isDarkTheme,
                    description = darkThemePreference.getDarkThemeDesc(),
                    onChecked = {
                        preferenceUtil.modifyDarkThemePreference(
                            if (isDarkTheme)
                                DarkThemePreference.OFF
                            else DarkThemePreference.ON
                        )
                    },
                    onClick = { navTotalController.navigate(Screens.Dark.router) },
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.language),
                    icon = Icons.Rounded.Language,
                    description = Locale.getDefault().toDisplayName(),
                ) {
                    navTotalController.navigate(Screens.Language.router)
                }
            }

            // Add predictive back gesture settings item(Android 14+)
            if (android.os.Build.VERSION.SDK_INT >= 34) {
                item {
                    PreferenceSwitch(
                        title = stringResource(id = R.string.predictive_back),
                        description = stringResource(id = R.string.predictive_back_desc),
                        icon = Icons.Outlined.Gesture,
                        isChecked = settings.value?.predictiveBackEnabled ?: true,
                        onClick = {
                            preferenceUtil.setPredictiveBackEnabled(
                                !(settings.value?.predictiveBackEnabled ?: true)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.ColorButtons(color: Color, preferenceUtil: PreferenceUtil) {
    paletteStyles.subList(STYLE_TONAL_SPOT, STYLE_MONOCHROME).forEachIndexed { index, style ->
        ColorButton(
            color = color,
            index = index,
            tonalStyle = style,
            preferenceUtil = preferenceUtil
        )
    }
}

@Composable
fun RowScope.ColorButton(
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    index: Int = 0,
    tonalStyle: PaletteStyle = PaletteStyle.TonalSpot,
    preferenceUtil: PreferenceUtil,
    isSelected: Boolean = false
) {
    val tonalPalettes by remember { mutableStateOf(color.toTonalPalettes(tonalStyle)) }
    ColorButtonImpl(
        modifier = modifier,
        tonalPalettes = tonalPalettes,
        isSelected = { isSelected }) {
        preferenceUtil.switchDynamicColor(enabled = false)
        preferenceUtil.modifyThemeSeedColor(color.toArgb(), index)
    }
}

@Composable
fun RowScope.ColorButtonImpl(
    modifier: Modifier = Modifier,
    isSelected: () -> Boolean = { false },
    tonalPalettes: TonalPalettes,
    cardColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onClick: () -> Unit = {},
) {

    val containerSize by animateDpAsState(targetValue = if (isSelected.invoke()) 28.dp else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (isSelected.invoke()) 16.dp else 0.dp)

    Surface(
        modifier =
            modifier
                .padding(4.dp)
                .sizeIn(maxHeight = 80.dp, maxWidth = 80.dp, minHeight = 64.dp, minWidth = 64.dp)
                .weight(1f, false)
                .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        onClick = onClick,
    ) {
        CompositionLocalProvider(LocalTonalPalettes provides tonalPalettes) {
            val color1 = 80.a1
            val color2 = 90.a2
            val color3 = 60.a3
            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier =
                        modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .drawBehind { drawCircle(color1) }
                            .align(Alignment.Center)
                ) {
                    Surface(
                        color = color2,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(24.dp),
                    ) {}
                    Surface(
                        color = color3,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp),
                    ) {}
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .size(containerSize)
                            .drawBehind { drawCircle(containerColor) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier
                                .size(iconSize)
                                .align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Settings_Preview() {
    Appearance(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}

@Suppress("DEPRECATION")
fun getCityName(context: Context): String {
    // Check if the permission has been granted
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // If the permission has not been granted, request it
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            1
        )
        return ""
    } else {
        // If the permission has been granted, get the city name
        // Get an instance of LocationManager
        val locationManager = getSystemService(context, LocationManager::class.java)
        // Get the device's last known location
        val location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        // Create an instance of Geocoder
        val geocoder = Geocoder(context)
        // Get the city address information
        val addresses = location?.let { geocoder.getFromLocation(it.latitude, it.longitude, 1) }
        // Return the city name
        val city = addresses?.get(0)?.locality ?: ""
        Log.d("Melendez", "getCityName: city:$city")
        return city
    }
}
