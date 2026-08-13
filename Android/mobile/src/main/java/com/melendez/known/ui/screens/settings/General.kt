package com.melendez.known.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.ui.components.generalsets.IdentitySelector
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.PreferenceSubtitle
import com.melendez.known.ui.components.generalsets.RegionField
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.components.generalsets.SubjectSelector
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens
import com.melendez.known.util.Identity
import com.melendez.known.util.PreferenceUtil
import com.melendez.known.util.toSubjectKeySet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun General(navigator: Navigator) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val screenType = LocalScreenType.current
    Column {
        SharedTopBar(
            title = stringResource(R.string.general_settings),
            screenType = screenType,
            navController = navigator,
            scrollBehavior = scrollBehavior
        )
        General_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        )
    }
}

@Composable
fun General_Content(modifier: Modifier) {
    val preferenceUtil: PreferenceUtil = viewModel()
    val settings = preferenceUtil.settings.collectAsStateWithLifecycle(initialValue = null).value

    val identity = settings?.identity ?: Identity.NONE
    val region = settings?.region ?: ""
    val selectedSubjects = (settings?.selectedSubjects ?: "").toSubjectKeySet()

    LazyColumn(modifier = modifier) {
        item { PreferenceSubtitle(text = stringResource(R.string.identity)) }
        item {
            IdentitySelector(
                selected = identity,
                onSelect = { preferenceUtil.updateIdentity(it) }
            )
        }

        item { PreferenceSubtitle(text = stringResource(R.string.region)) }
        item {
            RegionField(
                value = region,
                onValueChange = { preferenceUtil.updateRegion(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item { PreferenceSubtitle(text = stringResource(R.string.subjects)) }
        item {
            SubjectSelector(
                modifier = Modifier.padding(horizontal = 16.dp),
                selected = selectedSubjects,
                onToggle = { key ->
                    preferenceUtil.updateSelectedSubjects(
                        if (key in selectedSubjects) selectedSubjects - key
                        else selectedSubjects + key
                    )
                }
            )
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun GeneralPreview() {
    val navigationState = remember {
        com.melendez.known.ui.navigation.NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    General(navigator = Navigator(navigationState))
}