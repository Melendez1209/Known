package com.melendez.known.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
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

@Composable
fun MainScreen(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Main_Compact(navTotalController)
        }

        WindowWidthSizeClass.Medium -> {
            Main_Medium(navTotalController)
        }

        WindowWidthSizeClass.Expanded -> {
            Main_Expanded(navTotalController)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main_Compact(navTotalController: NavHostController) {

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
                                saveState = true
                            }
                            launchSingleTop =
                                true//Avoid multiple copies of the same destination when reelecting the same item
                            restoreState =
                                true // Restore state when reelecting a previously selected item
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
                contentDescription = stringResource(R.string.add)
            )
        }
    }) {
        NavHost(
            navController = navMainController,
            startDestination = Screens.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screens.Home.route) { Home() }
            composable(Screens.History.route) { History() }
            composable(Screens.Account.route) { Account(navTotalController = navTotalController) }
        }
    }
}

@Preview(group = "Main", device = "id:pixel_7_pro")
@Composable
fun Main_Compact_Preview() {
    Main_Compact(navTotalController = rememberNavController())
}

@Composable
fun Main_Medium(navTotalController: NavHostController) {
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
                                    saveState = true
                                }
                                launchSingleTop =
                                    true//Avoid multiple copies of the same destination when reelecting the same item
                                restoreState =
                                    true// Restore state when reelecting a previously selected item
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
            Scaffold(floatingActionButton = {
                LargeFloatingActionButton(onClick = { navTotalController.navigate("DateRangePicker") }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add)
                    )
                }
            }) {
                NavHost(navController = navMainController, startDestination = Screens.Home.route) {
                    composable(Screens.Home.route) { Home() }
                    composable(Screens.History.route) { History() }
                    composable(Screens.Account.route) { Account(navMainController) }
                }
            }
        }
    }
}


@Preview(group = "Main", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun Main_Medium_Preview() {
    Main_Medium(navTotalController = rememberNavController())
}

@Composable
fun Main_Expanded(navTotalController: NavHostController) {

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
                                saveState = true
                            }
                            launchSingleTop =
                                true//Avoid multiple copies of the same destination when reelecting the same item
                            restoreState =
                                true // Restore state when reelecting a previously selected item
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
        Scaffold(floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navTotalController.navigate("DateRangePicker") },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.add)
                    )
                }, text = { Text(text = stringResource(id = R.string.add)) }
            )
        }) {
            NavHost(navController = navMainController, startDestination = Screens.Home.route) {
                composable(Screens.Home.route) { Home() }
                composable(Screens.History.route) { History() }
                composable(Screens.Account.route) { Account(navMainController) }
            }
        }

    }
}

@Preview(group = "Main", device = "spec:width=673dp,height=841dp")
@Composable
fun Main_Expanded_Preview() {
    Main_Expanded(navTotalController = rememberNavController())
}