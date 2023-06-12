package com.melendez.known.main

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.melendez.known.R
import com.melendez.known.main.inners.History
import com.melendez.known.main.inners.Home
import com.melendez.known.main.inners.Me

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {

    val navMainController = rememberAnimatedNavController()

    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            Main_Compact(navTotalController, navMainController)
        }

        WindowWidthSizeClass.Medium -> {
            Main_Medium(navTotalController, navMainController)
        }

        WindowWidthSizeClass.Expanded -> {
            Main_Expanded(navTotalController, navMainController)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main_Compact(navTotalController: NavHostController, navMainController: NavHostController) {

    val screens = listOf(Screens.Home, Screens.History, Screens.Me)

    Scaffold(bottomBar = {
        NavigationBar {

            val navBackStackEntry by navMainController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            screens.forEach { screen ->
                NavigationBarItem(selected = currentDestination?.hierarchy?.any { it.route == screen.router } == true,
                    onClick = {
                        navMainController.navigate(screen.router) {
                            popUpTo(navMainController.graph.findStartDestination().id) {
                                saveState = true
                            }// Pop up to the start destination of the graph to avoid building up a large stack of destinations on the back stack as users select items
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
        FloatingActionButton(onClick = { navTotalController.navigate(com.melendez.known.Screens.DRP.router) }) {
            Icon(
                imageVector = Icons.Rounded.Add, contentDescription = stringResource(R.string.add)
            )
        }
    }) { paddings ->
        AnimatedNavHost(
            navController = navMainController,
            startDestination = Screens.Home.router,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screens.Home.router) { Home(paddings) }
            composable(Screens.History.router) { History(paddings) }
            composable(Screens.Me.router) { Me(navTotalController) }
        }
    }
}

@Preview(name = "Main_Compact", device = "id:pixel_7_pro")
@Composable
fun Main_Compact_Preview() {
    Main_Compact(rememberNavController(), rememberNavController())
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Main_Medium(navTotalController: NavHostController, navMainController: NavHostController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Row {

            val screens = listOf(Screens.Home, Screens.History, Screens.Me)

            NavigationRail {

                val navBackStackEntry by navMainController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationRailItem(selected = currentDestination?.hierarchy?.any { it.route == screen.router } == true,
                        onClick = {
                            navMainController.navigate(screen.router) {
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
                LargeFloatingActionButton(onClick = { navTotalController.navigate(com.melendez.known.Screens.DRP.router) }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(R.string.add)
                    )
                }
            }) {
                AnimatedNavHost(
                    navController = navMainController,
                    startDestination = Screens.Home.router
                ) {
                    composable(Screens.Home.router) { Home(null) }
                    composable(Screens.History.router) { History(null) }
                    composable(Screens.Me.router) { Me(navTotalController) }
                }
            }
        }
    }
}


@Preview(name = "Main_Medium", device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun Main_Medium_Preview() {
    Main_Medium(rememberNavController(), rememberNavController())
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Main_Expanded(navTotalController: NavHostController, navMainController: NavHostController) {

    val screens = listOf(Screens.Home, Screens.History, Screens.Me)

    PermanentNavigationDrawer(drawerContent = {

        val navBackStackEntry by navMainController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        ModalDrawerSheet {
            screens.forEach { screen ->
                NavigationDrawerItem(label = { Text(text = stringResource(screen.resourceId)) },
                    selected = currentDestination?.hierarchy?.any { it.route == screen.router } == true,
                    onClick = {
                        navMainController.navigate(screen.router) {
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
            ExtendedFloatingActionButton(onClick = { navTotalController.navigate(com.melendez.known.Screens.DRP.router) },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.add)
                    )
                },
                text = { Text(text = stringResource(id = R.string.add)) })
        }) { it ->
            AnimatedNavHost(
                navController = navMainController,
                startDestination = Screens.Home.router
            ) {
                composable(Screens.Home.router) { Home(null) }
                composable(Screens.History.router) { History(null) }
                composable(Screens.Me.router) { Me(navTotalController) }
            }
        }

    }
}

@Preview(name = "Main_Expanded", device = "spec:width=673dp,height=841dp")
@Composable
fun Main_Expanded_Preview() {
    Main_Expanded(rememberNavController(), rememberNavController())
}