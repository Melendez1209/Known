package com.melendez.known.ui.screens.add

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Adjust
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.melendez.known.R
import com.melendez.known.ui.navigation.Navigator
import com.melendez.known.ui.screens.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DRP(navigator: Navigator) {

    val state = rememberDateRangePickerState()

    var showingDialog by remember { mutableStateOf(false) }

    if (showingDialog) {
        AlertDialog(
            onDismissRequest = {
                showingDialog = false
            },
            confirmButton = {
                Button(
                    onClick = {
                        showingDialog = false
                        //TODO: Save incomplete content
                        navigator.goBack()
                    }
                ) {
                    Text(stringResource(R.string.reserve))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showingDialog = false
                        navigator.goBack()
                    }
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Adjust,
                    contentDescription = stringResource(id = R.string.back)
                )
            },
            title = {
                Text(text = stringResource(id = R.string.back))
            },
            text = {
                Text(text = stringResource(R.string.discard_sum))
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.exam_dates)) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.selectedStartDateMillis != null) showingDialog = true
                        else navigator.goBack()
                    }) {
                        Icon(
                            imageVector = if (state.selectedStartDateMillis != null) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            Log.d(
                                "Melendez",
                                "DRP: ${state.selectedStartDateMillis!!..state.selectedEndDateMillis!!}"
                            )
                            navigator.navigate(Screens.Inputting)
                        },
                        enabled = state.selectedEndDateMillis != null
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                            contentDescription = stringResource(R.string.next)
                        )
                    }
                }
            )
        }) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            DateRangePicker(state = state, modifier = Modifier.fillMaxSize())
        }
    }
    BackHandler {
        if (state.selectedStartDateMillis != null) showingDialog = true
        else navigator.goBack()
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun DRP_preview() {
    val navigationState = remember {
        com.melendez.known.ui.navigation.NavigationState(
            startRoute = Screens.Main,
            topLevelRoute = mutableStateOf(Screens.Main),
            backStacks = emptyMap()
        )
    }
    DRP(navigator = Navigator(navigationState))
}