package com.melendez.known.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
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
import com.melendez.known.ui.components.LocalScreenType
import com.melendez.known.ui.components.PreferenceSingleChoiceItem
import com.melendez.known.ui.components.PreferenceSubtitle
import com.melendez.known.ui.components.RegionField
import com.melendez.known.ui.components.SharedTopBar
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens
import com.melendez.known.util.Identity
import com.melendez.known.util.PreferenceUtil
import com.melendez.known.util.subjectKeyToStringResource
import com.melendez.known.util.subjectKeys
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
            PreferenceSingleChoiceItem(
                text = stringResource(R.string.student),
                selected = identity == Identity.STUDENT,
            ) {
                preferenceUtil.updateIdentity(Identity.STUDENT)
            }
        }
        item {
            PreferenceSingleChoiceItem(
                text = stringResource(R.string.teacher),
                selected = identity == Identity.TEACHER,
            ) {
                preferenceUtil.updateIdentity(Identity.TEACHER)
            }
        }
        item {
            PreferenceSingleChoiceItem(
                text = stringResource(R.string.parent),
                selected = identity == Identity.PARENT,
            ) {
                preferenceUtil.updateIdentity(Identity.PARENT)
            }
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
            SubjectChips(
                selectedSubjects = selectedSubjects,
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

@Composable
private fun SubjectChips(
    selectedSubjects: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        subjectKeys.chunked(3).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { key ->
                    FilterChip(
                        selected = key in selectedSubjects,
                        onClick = { onToggle(key) },
                        label = { Text(stringResource(subjectKeyToStringResource(key))) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowKeys.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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