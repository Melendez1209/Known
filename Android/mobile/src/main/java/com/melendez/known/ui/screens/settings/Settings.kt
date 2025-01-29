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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.melendez.known.ui.components.LocalDarkTheme
import com.melendez.known.ui.components.LocalDynamicColorSwitch
import com.melendez.known.ui.components.LocalPaletteStyleIndex
import com.melendez.known.ui.components.LocalSeedColor
import com.melendez.known.ui.components.PreferenceItem
import com.melendez.known.ui.components.PreferenceSwitch
import com.melendez.known.ui.components.PreferenceSwitchWithDivider
import com.melendez.known.ui.screens.Screens
import com.melendez.known.util.DarkThemePreference.Companion.OFF
import com.melendez.known.util.DarkThemePreference.Companion.ON
import com.melendez.known.util.PreferenceUtil
import com.melendez.known.util.STYLE_MONOCHROME
import com.melendez.known.util.STYLE_TONAL_SPOT
import com.melendez.known.util.paletteStyles
import com.melendez.known.util.toDisplayName
import io.material.hct.Hct
import java.util.Locale


private val ColorList =
    ((4..10) + (1..3)).map { it * 35.0 }.map { Color(Hct.from(it, 40.0, 40.0).toInt()) }


@Composable
fun Settings(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Settings_CompactExpanded(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium -> Settings_Medium(navTotalController = navTotalController)
        WindowWidthSizeClass.Expanded -> Settings_CompactExpanded(navTotalController = navTotalController)
        else -> Settings_CompactExpanded(navTotalController = navTotalController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_CompactExpanded(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navTotalController.navigate(Screens.Feedback.router)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Feedback,
                        contentDescription = stringResource(R.string.feedback)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Settings_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Medium(navTotalController: NavHostController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        MediumTopAppBar(
            title = { Text(text = stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navTotalController.navigate(Screens.Feedback.router)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Feedback,
                        contentDescription = stringResource(R.string.feedback)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Settings_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            navTotalController = navTotalController
        )
    }
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission", "MutableCollectionMutableState")
@Composable
fun Settings_Content(modifier: Modifier, navTotalController: NavHostController) {

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
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {},
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) { page ->
                        if (page < pageCount - 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                ColorButtons(ColorList[page])
                            }
                        } else {
                            // ColorButton for Monochrome theme
                            val isSelected =
                                LocalPaletteStyleIndex.current == STYLE_MONOCHROME &&
                                        !LocalDynamicColorSwitch.current
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                ColorButtonImpl(
                                    modifier = Modifier,
                                    isSelected = { isSelected },
                                    tonalPalettes =
                                        Color.Black.toTonalPalettes(PaletteStyle.Monochrome),
                                    onClick = {
                                        PreferenceUtil.switchDynamicColor(enabled = false)
                                        PreferenceUtil.modifyThemeSeedColor(
                                            Color.Black.toArgb(),
                                            STYLE_MONOCHROME,
                                        )
                                    },
                                )
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
                    PreferenceSwitch(
                        title = stringResource(id = R.string.dynamic_color),
                        description = stringResource(id = R.string.dynamic_color_desc),
                        icon = if (LocalDynamicColorSwitch.current) Icons.Rounded.Colorize else Icons.Outlined.Colorize,
                        isChecked = LocalDynamicColorSwitch.current,
                        onClick = { PreferenceUtil.switchDynamicColor() },
                    )
                }
            }
            item {
                val isDarkTheme = LocalDarkTheme.current.isDarkTheme()
                PreferenceSwitchWithDivider(
                    title = stringResource(id = R.string.dark_theme),
                    icon = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    isChecked = isDarkTheme,
                    description = LocalDarkTheme.current.getDarkThemeDesc(),
                    onChecked = {
                        PreferenceUtil.modifyDarkThemePreference(if (isDarkTheme) OFF else ON)
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
        }
    }
}


//    val context = LocalContext.current
//
//    // Variables related to settings
//    var colorMode by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
//    var cityName by rememberSaveable { mutableStateOf("") }
//    var grade by rememberSaveable { mutableIntStateOf(10) }
//    val courseList = listOf(
//        stringResource(R.string.physiotherapy),
//        stringResource(R.string.chemotherapy),
//        stringResource(R.string.biology),
//        stringResource(R.string.political),
//        stringResource(R.string.history),
//        stringResource(R.string.geography)
//    )
//    val courses =
//        rememberSaveable { mutableListOf(courseList[0], courseList[1], courseList[2]) }
//
//
//    // Variables related to components
//    var visibleAppearance by remember { mutableStateOf(true) }
//    var visibleAnalysis by remember { mutableStateOf(true) }
//    var expanded by remember { mutableStateOf(false) }
//
//    Surface(modifier = Modifier.fillMaxSize()) {
//        LazyColumn(modifier = modifier) {
//            item {
//                Column(Modifier.fillMaxWidth()) {
//                    Button(
//                        onClick = { navTotalController.navigate(Screens.Signin.router) },
//                        modifier = Modifier
//                            .align(Alignment.CenterHorizontally)
//                            .widthIn(max = 320.dp),
//                    ) {
//                        Text(text = stringResource(R.string.sign_to))
//                    }
//                }
//            }
//            item {
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier
//                            .height(48.dp)
//                            .fillMaxWidth()
//                            .clickable { visibleAppearance = !visibleAppearance }
//                    ) {
//                        Text(
//                            text = stringResource(R.string.appearance),
//                            modifier = Modifier
//                                .padding(start = 12.dp)
//                                .align(Alignment.CenterVertically),
//                            style = MaterialTheme.typography.titleLarge
//                        )
//                    }
//                    AnimatedVisibility(visible = visibleAppearance) {
//                        Column {
//                            ListItem(
//                                headlineContent = { Text(text = stringResource(R.string.dynamic_color)) },
//                                overlineContent = {
//                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) Text(
//                                        text = stringResource(R.string.low_version)
//                                    )
//                                },
//                                supportingContent = { Text(text = stringResource(R.string.apply_wallpaper)) },
//                                trailingContent = {
//                                    Switch(
//                                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
//                                        checked = colorMode,
//                                        onCheckedChange = { colorMode = it },
//                                        thumbContent = {
//                                            Icon(
//                                                imageVector = if (colorMode) Icons.Rounded.Check else Icons.Rounded.Clear,
//                                                contentDescription = ""
//                                            )
//                                        }
//                                    )
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//            item {
//                Column(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier
//                            .height(48.dp)
//                            .fillMaxWidth()
//                            .clickable { visibleAnalysis = !visibleAnalysis }
//                    ) {
//                        Text(
//                            text = stringResource(R.string.analysis),
//                            modifier = Modifier
//                                .padding(start = 12.dp)
//                                .align(Alignment.CenterVertically),
//                            style = MaterialTheme.typography.titleLarge
//                        )
//                    }
//                    AnimatedVisibility(visible = visibleAnalysis) {
//                        Column {
//
//                            val gradeList = mutableListOf(
//                                stringResource(id = R.string.g7),
//                                stringResource(id = R.string.g8),
//                                stringResource(id = R.string.g9),
//                                stringResource(id = R.string.g10),
//                                stringResource(id = R.string.g11),
//                                stringResource(id = R.string.g12)
//                            )
//
//                            ListItem(
//                                headlineContent = { Text(text = (stringResource(R.string.district))) },
//                                trailingContent = {
//                                    TextButton(onClick = { cityName = getCityName(context) }) {
//                                        Text(text = cityName.ifEmpty { stringResource(R.string.locate) })
//                                    }
//                                }
//                            )
//                            HorizontalDivider()
//                            ListItem(
//                                headlineContent = { Text(text = stringResource(id = R.string.grade)) },
//                                trailingContent = {
//                                    Button(onClick = { expanded = true }) {
//                                        Text(text = gradeList[grade - 7])
//                                        Icon(
//                                            imageVector = Icons.Rounded.ArrowDropDown,
//                                            contentDescription = stringResource(R.string.select_grade)
//                                        )
//                                    }
//                                    DropdownMenu(
//                                        expanded = expanded,
//                                        onDismissRequest = { expanded = false }) {
//                                        gradeList.forEach {
//                                            DropdownMenuItem(
//                                                text = { Text(text = it) },
//                                                onClick = {
//                                                    grade = gradeList.indexOf(it) + 7
//                                                    expanded = false
//                                                    Log.d(
//                                                        "Melendez",
//                                                        "Settings_Content: grade:$grade"
//                                                    )
//                                                }
//                                            )
//                                        }
//                                    }
//                                }
//                            )
//                            HorizontalDivider()
//                            ListItem(
//                                headlineContent = { Text(text = stringResource(R.string.optional)) },
//                                supportingContent = {
//                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
//                                        courseList.forEach { s ->
//                                            var selected by rememberSaveable { mutableStateOf(s in courses) }
//                                            FilterChip(
//                                                selected = selected,
//                                                onClick = {
//                                                    selected = !selected/*TODO*/
//                                                },
//                                                label = { Text(text = s) }
//                                            )
//                                            Spacer(modifier = Modifier.width(6.dp))
//                                        }
//                                        IconButton(onClick = { /*TODO*/ }) {
//                                            Icon(
//                                                imageVector = Icons.Rounded.Add,
//                                                contentDescription = stringResource(id = R.string.add)
//                                            )
//                                        }
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }


@Composable
fun RowScope.ColorButtons(color: Color) {
    paletteStyles.subList(STYLE_TONAL_SPOT, STYLE_MONOCHROME).forEachIndexed { index, style ->
        ColorButton(color = color, index = index, tonalStyle = style)
    }
}

@Composable
fun RowScope.ColorButton(
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    index: Int = 0,
    tonalStyle: PaletteStyle = PaletteStyle.TonalSpot,
) {
    val tonalPalettes by remember { mutableStateOf(color.toTonalPalettes(tonalStyle)) }
    val isSelect =
        !LocalDynamicColorSwitch.current &&
                LocalSeedColor.current == color.toArgb() &&
                LocalPaletteStyleIndex.current == index
    ColorButtonImpl(modifier = modifier, tonalPalettes = tonalPalettes, isSelected = { isSelect }) {
        PreferenceUtil.switchDynamicColor(enabled = false)
        PreferenceUtil.modifyThemeSeedColor(color.toArgb(), index)
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
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .size(containerSize)
                                .drawBehind { drawCircle(containerColor) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
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
    Settings(
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