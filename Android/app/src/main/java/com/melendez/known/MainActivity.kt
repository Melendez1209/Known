package com.melendez.known

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.melendez.known.main.screens.Account
import com.melendez.known.main.screens.History
import com.melendez.known.main.screens.Home
import com.melendez.known.main.screens.Screens
import com.melendez.known.ui.theme.KnownTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val widthSizeClass = calculateWindowSizeClass(this).widthSizeClass

            KnownTheme {
                when (widthSizeClass) {
                    WindowWidthSizeClass.Compact -> {
                        CompactScreen()
                    }

                    WindowWidthSizeClass.Medium -> {
                        MediumScreen()
                    }

                    WindowWidthSizeClass.Expanded -> {
                        ExpandedScreen()
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(device = "id:pixel_7_pro", group = "Main")
@Composable
fun CompactScreen() {

    val screens = listOf(Screens.Home, Screens.History, Screens.Account)
    val navController = rememberNavController()

    Scaffold(bottomBar = {
        NavigationBar {

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            screens.forEach { screen ->
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop =
                                true//Avoid multiple copies of the same destination when reelecting the same item
                            restoreState =
                                false // Restore state when reelecting a previously selected item
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(screen.resourceId)
                        )
                    })
            }
        }
    }) {
        NavHost(navController = navController, startDestination = Screens.Home.route) {
            composable(Screens.Home.route) { Home(navController) }
            composable(Screens.History.route) { History(navController) }
            composable(Screens.Account.route) { Account(navController) }
        }

    }
}

@Preview(group = "Main", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun MediumScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Row {

            val screens = listOf(Screens.Home, Screens.History, Screens.Account)
            val navController = rememberNavController()

            NavigationRail {

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationRailItem(
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                launchSingleTop =
                                    true//Avoid multiple copies of the same destination when reelecting the same item
                                restoreState =
                                    false // Restore state when reelecting a previously selected item
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.resourceId)
                            )
                        })
                }
            }
            NavHost(navController = navController, startDestination = Screens.Home.route) {
                composable(Screens.Home.route) { Home(navController) }
                composable(Screens.History.route) { History(navController) }
                composable(Screens.Account.route) { Account(navController) }
            }
        }
    }
}

@Preview(group = "Main", device = "spec:width=673dp,height=841dp")
@Composable
fun ExpandedScreen() {

    val screens = listOf(Screens.Home, Screens.History, Screens.Account)
    val navController = rememberNavController()

    PermanentNavigationDrawer(drawerContent = {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        screens.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(text = stringResource(screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = false
                        }
                        launchSingleTop =
                            true//Avoid multiple copies of the same destination when reelecting the same item
                        restoreState =
                            false // Restore state when reelecting a previously selected item
                    }
                }, icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = stringResource(screen.resourceId)
                    )
                })
        }
    }) {
        NavHost(navController = navController, startDestination = Screens.Home.route) {
            composable(Screens.Home.route) { Home(navController) }
            composable(Screens.History.route) { History(navController) }
            composable(Screens.Account.route) { Account(navController) }
        }
    }
}