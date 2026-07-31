package com.melendez.known.ui.screens.main

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Menu
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
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.navigation.rememberNavigationState
import com.melendez.known.ui.screens.main.inners.History
import com.melendez.known.ui.screens.main.inners.Home
import com.melendez.known.ui.screens.main.inners.Me
import com.melendez.known.util.ScreenType
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navigator: Navigator) {

    val screenType = LocalScreenType.current
    val screens = listOf(Screens.Home, Screens.History, Screens.Me)

    val navigationState = rememberNavigationState(
        startRoute = Screens.Home,
        topLevelRoutes = screens.toSet()
    )
    val innerNavigator = remember { Navigator(navigationState) }

    BackHandler(
        enabled = navigator.state.topLevelRoute == com.melendez.known.ui.screens.Screens.Main
                && navigationState.topLevelRoute != Screens.Home
    ) {
        innerNavigator.navigate(Screens.Home)
    }

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
        Crossfade(
            targetState = navigationState.topLevelRoute,
            animationSpec = tween(durationMillis = 300),
            label = "InnerTabTransition"
        ) { screen ->
            when (screen) {
                Screens.Home -> Home()
                Screens.History -> History(
                    paddingValues = paddings,
                    navigator = navigator,
                    onEditingChange = { isEditing = it }
                )

                Screens.Me -> Me(navigator)
            }
        }
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
    val wideNavigationRailState = rememberWideNavigationRailState()
    val wideNavigationRailScope = rememberCoroutineScope()

    val expandedLabel = stringResource(R.string.expanded)
    val collapsedLabel = stringResource(R.string.collapsed)

    Surface(modifier = Modifier.fillMaxSize()) {
        Row {
            WideNavigationRail(state = wideNavigationRailState) {

                IconButton(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .semantics {
                            stateDescription =
                                if (wideNavigationRailState.currentValue == WideNavigationRailValue.Expanded) {
                                    expandedLabel
                                } else {
                                    collapsedLabel
                                }
                        },
                    onClick = { wideNavigationRailScope.launch { if (wideNavigationRailState.targetValue == WideNavigationRailValue.Expanded) wideNavigationRailState.collapse() else wideNavigationRailState.expand() } }
                ) {
                    Icon(
                        imageVector =
                            if (wideNavigationRailState.targetValue == WideNavigationRailValue.Expanded) Icons.AutoMirrored.Rounded.MenuOpen
                            else Icons.Rounded.Menu,
                        contentDescription =
                            if (wideNavigationRailState.targetValue == WideNavigationRailValue.Expanded)
                                expandedLabel else collapsedLabel
                    )
                }

                screens.forEach { screen ->
                    WideNavigationRailItem(
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
                        railExpanded = wideNavigationRailState.currentValue == WideNavigationRailValue.Expanded
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
                Crossfade(
                    targetState = navigationState.topLevelRoute,
                    animationSpec = tween(durationMillis = 300),
                    label = "InnerTabTransition"
                ) { screen ->
                    when (screen) {
                        Screens.Home -> Home()
                        Screens.History -> History(
                            navigator = navigator,
                            onEditingChange = { isEditing = it }
                        )

                        Screens.Me -> Me(navigator)
                    }
                }
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
                Crossfade(
                    targetState = navigationState.topLevelRoute,
                    animationSpec = tween(durationMillis = 300),
                    label = "InnerTabTransition"
                ) { screen ->
                    when (screen) {
                        Screens.Home -> Home()
                        Screens.History -> History(
                            navigator = navigator,
                            onEditingChange = { isEditing = it }
                        )

                        Screens.Me -> Me(navigator)
                    }
                }
            }
        }
    }
}


@Preview(device = "id:pixel_10_pro")
@Composable
private fun MainScreen_Preview() {
    val screens = listOf(Screens.Home, Screens.History, Screens.Me)
    val navigationState = rememberNavigationState(
        startRoute = Screens.Home,
        topLevelRoutes = screens.toSet()
    )
    MainScreen(navigator = remember { Navigator(navigationState) })
}