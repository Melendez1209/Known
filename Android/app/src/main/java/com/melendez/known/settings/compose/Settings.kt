package com.melendez.known.settings.compose

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens

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
                        imageVector = Icons.Rounded.NavigateBefore,
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
                        imageVector = Icons.Rounded.NavigateBefore,
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission", "MutableCollectionMutableState")
@Composable
fun Settings_Content(modifier: Modifier, navTotalController: NavHostController) {

    val context = LocalContext.current

    // Variables related to settings
    var colorMode by rememberSaveable { mutableStateOf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) }
    var cityName by rememberSaveable { mutableStateOf("") }
    var grade by rememberSaveable { mutableIntStateOf(10) }
    val courses = rememberSaveable { mutableListOf<String>() }


    // Variables related to components
    var visibleAppearance by remember { mutableStateOf(true) }
    var visibleAnalysis by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }
    var showingDialog by remember { mutableStateOf(false) }

    if (showingDialog) {

        val physiotherapy = stringResource(R.string.physiotherapy)
        val chemotherapy = stringResource(R.string.chemotherapy)
        val biology = stringResource(R.string.biology)
        val political = stringResource(R.string.political)
        val history = stringResource(R.string.history)
        val geography = stringResource(R.string.geography)
        val courseList = listOf(physiotherapy, chemotherapy, biology, political, history, geography)

        val message = stringResource(id = R.string.wrong_courses)

        AlertDialog(
            onDismissRequest = {
                courses.clear()
                showingDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (courses.size != 3) {
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        } else {
                            showingDialog = false
                        }
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.reserve))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        courses.clear()
                        showingDialog = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Book,
                    contentDescription = stringResource(id = R.string.optional)
                )
            },
            title = {
                Text(text = stringResource(id = R.string.take_course))
            },
            text = {
                FlowColumn(horizontalArrangement = Arrangement.Center) {
                    courseList.forEach { s ->

                        var selected by rememberSaveable { mutableStateOf(s in courses) }

                        FilterChip(
                            selected = selected,
                            onClick = {
                                courses.apply {
                                    if (s in this) {
                                        remove(s)
                                    } else {
                                        add(s)
                                    }
                                }
                                selected = !selected
                            },
                            label = { Text(text = s) },
                            modifier = Modifier.padding(horizontal = 3.dp),
                            leadingIcon = {
                                Row {
                                    AnimatedVisibility(visible = s in courses) {
                                        Icon(
                                            imageVector = Icons.Rounded.Done,
                                            contentDescription = stringResource(R.string.selected)
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                Column(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { navTotalController.navigate(Screens.Signin.router) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        Text(text = stringResource(R.string.sign_to))
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { visibleAppearance = !visibleAppearance }
                    ) {
                        Text(
                            text = stringResource(R.string.appearance),
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    AnimatedVisibility(visible = visibleAppearance) {
                        Column {
                            ListItem(
                                headlineContent = { Text(text = stringResource(R.string.dynamic_color)) },
                                overlineContent = {
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) Text(
                                        text = stringResource(R.string.low_version)
                                    )
                                },
                                supportingContent = { Text(text = stringResource(R.string.apply_wallpaper)) },
                                trailingContent = {
                                    Switch(
                                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                                        checked = colorMode,
                                        onCheckedChange = { colorMode = it },
                                        thumbContent = {
                                            Icon(
                                                imageVector = if (colorMode) Icons.Rounded.Check else Icons.Rounded.Clear,
                                                contentDescription = ""
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { visibleAnalysis = !visibleAnalysis }
                    ) {
                        Text(
                            text = stringResource(R.string.analysis),
                            modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    AnimatedVisibility(visible = visibleAnalysis) {
                        Column {

                            val gradeList = mutableListOf(
                                stringResource(id = R.string.g7),
                                stringResource(id = R.string.g8),
                                stringResource(id = R.string.g9),
                                stringResource(id = R.string.g10),
                                stringResource(id = R.string.g11),
                                stringResource(id = R.string.g12)
                            )

                            ListItem(
                                headlineContent = { Text(text = (stringResource(R.string.district))) },
                                trailingContent = {
                                    TextButton(onClick = { cityName = getCityName(context) }) {
                                        Text(text = cityName.ifEmpty { stringResource(R.string.locate) })
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text(text = stringResource(id = R.string.grade)) },
                                trailingContent = {
                                    Button(onClick = { expanded = true }) {
                                        Text(text = gradeList[grade - 7])
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDropDown,
                                            contentDescription = stringResource(R.string.select_grade)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }) {
                                        gradeList.forEach {
                                            DropdownMenuItem(
                                                text = { Text(text = it) },
                                                onClick = {
                                                    grade = gradeList.indexOf(it) + 7
                                                    expanded = false
                                                    Log.d(
                                                        "Melendez",
                                                        "Settings_Content: grade:$grade"
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            )
                            HorizontalDivider()
                            ListItem(
                                headlineContent = { Text(text = stringResource(R.string.optional)) },
                                trailingContent = {
                                    TextButton(onClick = { showingDialog = true }) {
                                        Text(
                                            text = if (courses.isNotEmpty()) courses.toString() else stringResource(
                                                id = R.string.take_course
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
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

@Preview(device = "id:pixel_7_pro")
@Composable
fun Settings_Preview() {
    Settings(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}
