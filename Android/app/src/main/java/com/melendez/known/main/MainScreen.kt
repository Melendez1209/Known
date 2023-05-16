package com.melendez.known.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.main.inners.Account
import com.melendez.known.main.inners.History
import com.melendez.known.main.inners.Home
import com.melendez.known.main.inners.Screens

@Composable
fun MainScreen(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            CompactScreen(navTotalController)
        }

        WindowWidthSizeClass.Medium -> {
            MediumScreen()
        }

        WindowWidthSizeClass.Expanded -> {
            ExpandedScreen()
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CompactScreen(navTotalController: NavHostController) {

    val screens = listOf(Screens.Home, Screens.History, Screens.Account)
    val navMainController = rememberNavController()

    Scaffold(bottomBar = {
        NavigationBar {

            val navBackStackEntry by navMainController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            screens.forEach { screen ->
                NavigationBarItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navMainController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                            popUpTo(navMainController.graph.findStartDestination().id) {
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
    }, floatingActionButton = {
        FloatingActionButton(onClick = { navTotalController.navigate("DateRangePicker") }) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.add),
            )
        }
    }) {
        NavHost(
            navController = navMainController,
            startDestination = Screens.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screens.Home.route) { Home(navMainController) }
            composable(Screens.History.route) { History() }
            composable(Screens.Account.route) { Account(navMainController) }
        }
    }
}

@Preview(device = "id:pixel_7_pro", group = "Main")
@Composable
fun CompactScreen_Preview() {
    CompactScreen(navTotalController = rememberNavController())
}

@Preview(group = "Main", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun MediumScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Row {

            val screens = listOf(Screens.Home, Screens.History, Screens.Account)
            val navMainController = rememberNavController()

            NavigationRail {

                val navBackStackEntry by navMainController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationRailItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navMainController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                                popUpTo(navMainController.graph.findStartDestination().id) {
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
            NavHost(navController = navMainController, startDestination = Screens.Home.route) {
                composable(Screens.Home.route) { Home(navMainController) }
                composable(Screens.History.route) { History() }
                composable(Screens.Account.route) { Account(navMainController) }
            }
        }
    }
}

@Preview(group = "Main", device = "spec:width=673dp,height=841dp")
@Composable
fun ExpandedScreen() {

    val screens = listOf(Screens.Home, Screens.History, Screens.Account)
    val navMainController = rememberNavController()

    PermanentNavigationDrawer(drawerContent = {

        val navBackStackEntry by navMainController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        ModalDrawerSheet {
            screens.forEach { screen ->
                NavigationDrawerItem(
                    label = { Text(text = stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navMainController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
                            popUpTo(navMainController.graph.findStartDestination().id) {
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
    }, content = {
        NavHost(navController = navMainController, startDestination = Screens.Home.route) {
            composable(Screens.Home.route) { Home(navMainController) }
            composable(Screens.History.route) { History() }
            composable(Screens.Account.route) { Account(navMainController) }
        }
    })
}