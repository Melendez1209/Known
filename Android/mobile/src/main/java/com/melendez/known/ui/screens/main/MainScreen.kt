package com.melendez.known.ui.screens.main

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.melendez.known.R
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.navigation.rememberNavigationState
import com.melendez.known.ui.navigation.toEntries
import com.melendez.known.ui.screens.main.inners.History
import com.melendez.known.ui.screens.main.inners.Home
import com.melendez.known.ui.screens.main.inners.Me
import com.melendez.known.util.ScreenType

@Composable
fun MainScreen(navigator: Navigator) {

    val screenType = LocalScreenType.current
    val screens = listOf(Screens.Home, Screens.History, Screens.Me)

    val navigationState = rememberNavigationState(
        startRoute = Screens.Home,
        topLevelRoutes = screens.toSet()
    )
    val innerNavigator = remember { Navigator(navigationState) }

    when (screenType) {
        ScreenType.Compact -> Main_Compact(
            navigator = navigator,
            innerNavigator = innerNavigator,
            screens = screens,
            navigationState = navigationState
        )

        ScreenType.Medium -> Main_Medium(
            navigator = navigator,
            innerNavigator = innerNavigator,
            screens = screens,
            navigationState = navigationState
        )

        ScreenType.Expanded -> Main_Expanded(
            navigator = navigator,
            innerNavigator = innerNavigator,
            screens = screens,
            navigationState = navigationState
        )
    }
}

@Composable
fun Main_Compact(
    navigator: Navigator,
    innerNavigator: Navigator,
    screens: List<Screens>,
    navigationState: com.melendez.known.ui.navigation.NavigationState
) {

    var isEditing by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                if (!isEditing) {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = screen == navigationState.topLevelRoute,
                            onClick = {
                                innerNavigator.navigate(screen)
                            },
                            icon = {
                                Icon(
                                    imageVector = if (screen == navigationState.topLevelRoute) screen.iconSelected else screen.iconUnelected,
                                    contentDescription = stringResource(screen.resourceId)
                                )
                            },
                            label = {
                                Text(text = stringResource(id = screen.resourceId))
                            },
                            alwaysShowLabel = false
                        )
                    }
                } else {

                    var isFavorite by remember { mutableStateOf(false) }

                    BottomAppBar(
                        actions = {
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.share)
                                )
                            }
                            IconButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Rounded.Print,
                                    contentDescription = stringResource(R.string.print)
                                )
                            }
                            IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.edit)
                                )
                            }
                            IconButton(onClick = { isFavorite = !isFavorite }) {
                                Icon(
                                    imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                    contentDescription = stringResource(R.string.share)
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = { /*TODO*/ }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = stringResource(R.string.delete)
                                )
                            }
                        }
                    )
                }
            }
        }, floatingActionButton = {
            FloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    ) { paddings ->
        val entryProvider = entryProvider<NavKey> {
            entry<Screens.Home> { Home() }
            entry<Screens.History> {
                History(
                    paddingValues = paddings,
                    navigator = navigator,
                    onEditingChange = { isEditing = it })
            }
            entry<Screens.Me> { Me(navigator) }
        }
        NavDisplay(
            entries = navigationState.toEntries(entryProvider),
            onBack = { innerNavigator.goBack() }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main_Medium(
    navigator: Navigator,
    innerNavigator: Navigator,
    screens: List<Screens>,
    navigationState: com.melendez.known.ui.navigation.NavigationState
) {

    var isEditing by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Row {
            NavigationRail {
                screens.forEach { screen ->
                    NavigationRailItem(
                        selected = screen == navigationState.topLevelRoute,
                        onClick = {
                            innerNavigator.navigate(screen)
                            isEditing = false
                        },
                        icon = {
                            Icon(
                                imageVector = if (screen == navigationState.topLevelRoute) screen.iconSelected else screen.iconUnelected,
                                contentDescription = stringResource(screen.resourceId)
                            )
                        }, label = {
                            Text(text = stringResource(id = screen.resourceId))
                        },
                        alwaysShowLabel = false
                    )
                }
            }
            Scaffold(
                bottomBar = {
                    if (isEditing) {

                        var isFavorite by remember { mutableStateOf(false) }

                        BottomAppBar(
                            actions = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = stringResource(R.string.share)
                                    )
                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Print,
                                        contentDescription = stringResource(R.string.print)
                                    )
                                }
                                IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.edit)
                                    )
                                }
                                IconButton(onClick = { isFavorite = !isFavorite }) {
                                    Icon(
                                        imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                        contentDescription = stringResource(R.string.share)
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    LargeFloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            ) {
                val entryProvider = entryProvider<NavKey> {
                    entry<Screens.Home> { Home() }
                    entry<Screens.History> {
                        History(
                            navigator = navigator,
                            onEditingChange = { isEditing = it })
                    }
                    entry<Screens.Me> { Me(navigator) }
                }
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    onBack = { innerNavigator.goBack() }
                )
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main_Expanded(
    navigator: Navigator,
    innerNavigator: Navigator,
    screens: List<Screens>,
    navigationState: com.melendez.known.ui.navigation.NavigationState
) {

    var isEditing by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        PermanentNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    screens.forEach { screen ->
                        NavigationDrawerItem(
                            label = { Text(text = stringResource(screen.resourceId)) },
                            selected = screen == navigationState.topLevelRoute,
                            onClick = {
                                innerNavigator.navigate(screen)
                                if (screen.router != Screens.History.router) {
                                    isEditing = false
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (screen == navigationState.topLevelRoute) screen.iconSelected else screen.iconUnelected,
                                    contentDescription = stringResource(screen.resourceId)
                                )
                            }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    if (isEditing) {

                        var isFavorite by remember { mutableStateOf(false) }

                        BottomAppBar(
                            actions = {
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = stringResource(R.string.share)
                                    )
                                }
                                IconButton(onClick = { /*TODO*/ }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Print,
                                        contentDescription = stringResource(R.string.print)
                                    )
                                }
                                IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.edit)
                                    )
                                }
                                IconButton(onClick = { isFavorite = !isFavorite }) {
                                    Icon(
                                        imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                        contentDescription = stringResource(R.string.share)
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    LargeFloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP) }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            ) {
                val entryProvider = entryProvider<NavKey> {
                    entry<Screens.Home> {
                        Home()
                    }
                    entry<Screens.History> {
                        History(
                            navigator = navigator,
                            onEditingChange = { isEditing = it })
                    }
                    entry<Screens.Me> { Me(navigator) }
                }
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider),
                    onBack = { innerNavigator.goBack() }
                )
            }
        }
    }
}
