package com.melendez.known.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.melendez.known.R
import com.melendez.known.ui.components.generalsets.IdentitySelector
import com.melendez.known.ui.components.generalsets.RegionField
import com.melendez.known.ui.components.generalsets.SubjectSelector
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.util.Identity
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Guide(
    navigator: Navigator
) {
    var selectedIdentity by remember { mutableIntStateOf(Identity.STUDENT) }
    var region by remember { mutableStateOf("") }
    val selectedSubjects = remember { mutableStateOf(setOf<String>()) }
    val preferenceUtil: PreferenceUtil = viewModel()

    val pagerState = rememberPagerState { 3 }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.onboarding_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> IdentitySelection(
                    selected = selectedIdentity,
                    onIdentitySelected = { identity ->
                        selectedIdentity = identity
                    }
                )

                1 -> RegionSelection { selectedRegion ->
                    region = selectedRegion
                }

                2 -> SubjectSelection(selectedSubjects)
            }
        }

        // 页面指示器
        PagerIndicator(
            currentPage = pagerState.currentPage,
            pageCount = 3,
            modifier = Modifier.padding(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    } else {
                        // Skip the welcome screen and set  `isFirstLogin` to `false`
                        preferenceUtil.setFirstLogin(false)
                        persistOnboarding(
                            preferenceUtil,
                            selectedIdentity,
                            region,
                            selectedSubjects.value
                        )
                        navigator.navigate(Screens.Main)
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage == 0) stringResource(R.string.skip) else stringResource(
                        R.string.back
                    )
                )
            }

            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        //  Complete the onboarding process and set `isFirstLogin` to `false`
                        preferenceUtil.setFirstLogin(false)
                        persistOnboarding(
                            preferenceUtil,
                            selectedIdentity,
                            region,
                            selectedSubjects.value
                        )
                        navigator.navigate(Screens.Main)
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) stringResource(R.string.get_started) else stringResource(
                        R.string.next
                    )
                )
            }
        }
    }
}

@Composable
fun PagerIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { iteration ->
            val color = if (currentPage == iteration) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(10.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Composable
fun IdentitySelection(
    selected: Int,
    onIdentitySelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.onboarding_identity),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            IdentitySelector(
                selected = selected,
                onSelect = onIdentitySelected
            )
        }
    }
}

@Composable
fun RegionSelection(onRegionSelected: (String) -> Unit) {
    var region by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.onboarding_region),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            RegionField(
                value = region,
                onValueChange = {
                    region = it
                    onRegionSelected(it)
                }
            )
        }
    }
}

@Composable
fun SubjectSelection(selectedSubjects: androidx.compose.runtime.MutableState<Set<String>>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.onboarding_subjects),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SubjectSelector(
                selected = selectedSubjects.value,
                onToggle = { key ->
                    selectedSubjects.value =
                        if (selectedSubjects.value.contains(key)) {
                            selectedSubjects.value - key
                        } else {
                            selectedSubjects.value + key
                        }
                }
            )
        }
    }
}

private fun persistOnboarding(
    preferenceUtil: PreferenceUtil,
    identity: Int,
    region: String,
    selectedSubjects: Set<String>
) {
    preferenceUtil.updateIdentity(identity)
    preferenceUtil.updateRegion(region)
    preferenceUtil.updateSelectedSubjects(selectedSubjects)
}