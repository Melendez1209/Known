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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.ShareOptionsSheet
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.navigation.rememberNavigationState
import com.melendez.known.ui.screens.main.inners.DeleteExamsDialog
import com.melendez.known.ui.screens.main.inners.History
import com.melendez.known.ui.screens.main.inners.Home
import com.melendez.known.ui.screens.main.inners.Me
import com.melendez.known.ui.viewmodel.ExamViewModel
import com.melendez.known.util.ScreenType
import com.melendez.known.util.share.ShareManager
import com.melendez.known.util.subjectKeyToStringResource
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navigator: Navigator) {

    val screenType = LocalScreenType.current
    val screens = listOf(Screens.Home, Screens.History, Screens.Me)
    val context = LocalContext.current
    val viewModel: ExamViewModel = viewModel()

    // The history list's selection is held here so every layout shares one source of truth for
    // multi-select mode and for the delete confirmation
    val checkedIds = remember { mutableStateListOf<Long>() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }

    val exams by remember { viewModel.exams }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // The exam to share/favourite: the first selected one, or the most recent exam when nothing is selected
    val targetExamId = checkedIds.firstOrNull() ?: exams.firstOrNull()?.exam?.id
    val shareExamWithScores by remember(targetExamId) {
        viewModel.examWithScores(
            targetExamId ?: 0L
        )
    }
        .collectAsStateWithLifecycle(initialValue = null)
    val shareStats by remember(targetExamId) { viewModel.subjectStats(targetExamId ?: 0L) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Favourite state for the target exam
    val isFavorite by remember(targetExamId) { viewModel.isFavorite(targetExamId ?: 0L) }
        .collectAsStateWithLifecycle(initialValue = false)

    if (showDeleteDialog) {
        DeleteExamsDialog(
            checkedIds = checkedIds,
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showShareSheet && shareExamWithScores != null) {
        // Resolve subject names in the @Composable context so they are configuration-aware
        val subjectNames = shareExamWithScores!!.scores.associate { score ->
            score.subjectKey to stringResource(subjectKeyToStringResource(score.subjectKey))
        }
        ShareOptionsSheet(
            onDismiss = { showShareSheet = false },
            onShareAsImage = {
                ShareManager.shareAsImage(
                    context = context,
                    examWithScores = shareExamWithScores!!,
                    allExams = exams,
                    subjectNameResolver = { key -> subjectNames[key] ?: key }
                )
            },
            onShareAsText = {
                ShareManager.shareAsText(
                    context = context,
                    examWithScores = shareExamWithScores!!,
                    allExams = exams,
                    subjectStats = shareStats,
                    subjectNameResolver = { key -> subjectNames[key] ?: key }
                )
            }
        )
    }

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
            navigationState = navigationState,
            checkedIds = checkedIds,
            onRequestDelete = { showDeleteDialog = true },
            onRequestShare = { showShareSheet = true },
            targetExamId = targetExamId,
            isFavorite = isFavorite,
            onToggleFavorite = { examId ->
                kotlinx.coroutines.MainScope().launch {
                    viewModel.toggleFavorite(examId)
                }
            }
        )

        ScreenType.Medium -> Main_Medium(
            navigator = navigator,
            innerNavigator = innerNavigator,
            screens = screens,
            navigationState = navigationState,
            checkedIds = checkedIds,
            onRequestShare = { showShareSheet = true },
            targetExamId = targetExamId,
            isFavorite = isFavorite,
            onToggleFavorite = { examId ->
                kotlinx.coroutines.MainScope().launch {
                    viewModel.toggleFavorite(examId)
                }
            }
        )

        ScreenType.Expanded -> Main_Expanded(
            navigator = navigator,
            innerNavigator = innerNavigator,
            screens = screens,
            navigationState = navigationState,
            checkedIds = checkedIds,
            onRequestShare = { showShareSheet = true },
            targetExamId = targetExamId,
            isFavorite = isFavorite,
            onToggleFavorite = { examId ->
                kotlinx.coroutines.MainScope().launch {
                    viewModel.toggleFavorite(examId)
                }
            }
        )
    }
}

@Composable
fun Main_Compact(
    navigator: Navigator,
    innerNavigator: Navigator,
    screens: List<Screens>,
    navigationState: com.melendez.known.ui.navigation.NavigationState,
    checkedIds: SnapshotStateList<Long>,
    onRequestDelete: () -> Unit,
    onRequestShare: () -> Unit,
    targetExamId: Long?,
    isFavorite: Boolean,
    onToggleFavorite: (Long) -> Unit
) {

    val isEditing = checkedIds.isNotEmpty()

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

                    BottomAppBar(
                        actions = {
                            IconButton(onClick = onRequestShare) {
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
                            IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = stringResource(R.string.edit)
                                )
                            }
                            IconButton(
                                onClick = { targetExamId?.let { examId -> onToggleFavorite(examId) } }
                            ) {
                                Icon(
                                    imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                    contentDescription = stringResource(if (isFavorite) R.string.remove_favourite else R.string.add_favourite)
                                )
                            }
                        },
                        floatingActionButton = {
                            FloatingActionButton(onClick = onRequestDelete) {
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
            FloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
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
                    checkedIds = checkedIds
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
    navigationState: com.melendez.known.ui.navigation.NavigationState,
    checkedIds: SnapshotStateList<Long>,
    onRequestShare: () -> Unit,
    targetExamId: Long?,
    isFavorite: Boolean,
    onToggleFavorite: (Long) -> Unit
) {

    val isEditing = checkedIds.isNotEmpty()
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
                            checkedIds.clear()
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

                        BottomAppBar(
                            actions = {
                                IconButton(onClick = onRequestShare) {
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
                                IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.edit)
                                    )
                                }
                                IconButton(onClick = { targetExamId?.let { onToggleFavorite(it) } }) {
                                    Icon(
                                        imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                        contentDescription = stringResource(if (isFavorite) R.string.remove_favourite else R.string.add_favourite)
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    LargeFloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
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
                            checkedIds = checkedIds
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
    navigationState: com.melendez.known.ui.navigation.NavigationState,
    checkedIds: SnapshotStateList<Long>,
    onRequestShare: () -> Unit,
    targetExamId: Long?,
    isFavorite: Boolean,
    onToggleFavorite: (Long) -> Unit
) {

    val isEditing = checkedIds.isNotEmpty()

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
                                    checkedIds.clear()
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

                        BottomAppBar(
                            actions = {
                                IconButton(onClick = onRequestShare) {
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
                                IconButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = stringResource(R.string.edit)
                                    )
                                }
                                IconButton(onClick = { targetExamId?.let { onToggleFavorite(it) } }) {
                                    Icon(
                                        imageVector = if (!isFavorite) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                                        contentDescription = stringResource(if (isFavorite) R.string.remove_favourite else R.string.add_favourite)
                                    )
                                }
                            }
                        )
                    }
                },
                floatingActionButton = {
                    LargeFloatingActionButton(onClick = { navigator.navigate(com.melendez.known.ui.screens.Screens.DRP()) }) {
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
                            checkedIds = checkedIds
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
