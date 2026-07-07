package com.melendez.known.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults.floatingToolbarVerticalNestedScroll
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.melendez.known.R
import com.melendez.known.ui.navigation.NavigationState
import com.melendez.known.ui.navigation.Navigator

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Detail(navigator: Navigator) {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {}
    )

    var isFavorite by remember { mutableStateOf(false) }
    val behaviorTop = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var expanded by rememberSaveable { mutableStateOf(true) }
    val scrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.exam)) },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                scrollBehavior = behaviorTop
            )
        }
    ) { innerPadding ->
        val courseList = listOf(
            stringResource(R.string.all),
            stringResource(id = R.string.chinese),
            stringResource(id = R.string.maths),
            stringResource(id = R.string.foreign_language),
            stringResource(R.string.physiotherapy),
            stringResource(R.string.chemotherapy),
            stringResource(R.string.biology),
            stringResource(R.string.political),
            stringResource(R.string.history),
            stringResource(R.string.geography),
            stringResource(R.string.pe)
        )
        var course by remember { mutableIntStateOf(0) }

        Box(Modifier.padding(innerPadding)) {


            HorizontalFloatingToolbar(
                modifier = Modifier.align(Alignment.BottomCenter),
                expanded = expanded
            ) {

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                SecondaryScrollableTabRow(selectedTabIndex = course) {
                    courseList.forEachIndexed { index, title ->
                        Tab(
                            selected = course == index,
                            onClick = { course = index },
                            text = { Text(text = title) }
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(behaviorTop.nestedScrollConnection)
                        .floatingToolbarVerticalNestedScroll(
                            expanded = expanded,
                            onExpand = { expanded = true },
                            onCollapse = { expanded = false })
                ) {
                    items(100) { count ->
                        Text(text = "$count")
                    }
                }
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Detail_Preview() {
    val navigationState = remember {
        NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    Detail(navigator = Navigator(navigationState))
}
