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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.melendez.known.R
import com.melendez.known.util.PreferenceUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Onboarding(
    navTotalController: NavHostController
) {
    var selectedIdentity by remember { mutableIntStateOf(R.string.student) }
    var region by remember { mutableStateOf("") }
    val selectedSubjects = remember { mutableStateOf(setOf<Int>()) }
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
                0 -> IdentitySelection { identity ->
                    selectedIdentity = identity
                }

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
                        // 跳过引导，设置isFirstLogin为false
                        preferenceUtil.setFirstLogin(false)
                        // Skip the entire onboarding
                        navTotalController.navigate(Screens.Main.router) {
                            popUpTo(Screens.Guide.router) { inclusive = true }
                        }
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
                        // 完成引导，设置isFirstLogin为false
                        preferenceUtil.setFirstLogin(false)
                        // Navigate to main screen
                        navTotalController.navigate(Screens.Main.router) {
                            popUpTo(Screens.Guide.router) { inclusive = true }
                        }
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
fun IdentitySelection(onIdentitySelected: (Int) -> Unit) {
    // 保存资源ID列表
    val identityOptions = listOf(
        R.string.student,
        R.string.teacher,
        R.string.parent
    )

    // 保存选中的资源ID
    var selectedOptionId by remember { mutableIntStateOf(identityOptions[0]) }

    // 用于显示的字符串列表
    val radioOptions = identityOptions.map { stringResource(it) }

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

            Column(Modifier.selectableGroup()) {
                identityOptions.forEachIndexed { index, resourceId ->
                    val text = radioOptions[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (resourceId == selectedOptionId),
                                onClick = {
                                    selectedOptionId = resourceId
                                    onIdentitySelected(resourceId)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (resourceId == selectedOptionId),
                            onClick = null // null because we're handling the click on the Row
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
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

            OutlinedTextField(
                value = region,
                onValueChange = {
                    region = it
                    onRegionSelected(it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.district)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* 模拟定位功能，实际情况需要实现位置权限请求和获取 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.use_location),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectSelection(selectedSubjects: androidx.compose.runtime.MutableState<Set<Int>>) {
    // 保存资源ID列表
    val subjectIds = listOf(
        R.string.physics,
        R.string.chemistry,
        R.string.biology,
        R.string.political,
        R.string.history_subject,
        R.string.geography,
        R.string.pe
    )

    // 用于显示的字符串列表
    val subjectTexts = subjectIds.map { stringResource(it) }

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

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                subjectIds.chunked(3).forEachIndexed { rowIndex, rowSubjectIds ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSubjectIds.forEachIndexed { colIndex, subjectId ->
                            val index = rowIndex * 3 + colIndex
                            FilterChip(
                                selected = selectedSubjects.value.contains(subjectId),
                                onClick = {
                                    selectedSubjects.value =
                                        if (selectedSubjects.value.contains(subjectId)) {
                                            selectedSubjects.value - subjectId
                                        } else {
                                            selectedSubjects.value + subjectId
                                        }
                                },
                                label = { Text(subjectTexts[index]) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Add empty chips to fill the row if needed
                        repeat(3 - rowSubjectIds.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
} 