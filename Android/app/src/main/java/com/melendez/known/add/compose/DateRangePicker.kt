package com.melendez.known.add.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.Screens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DRP(navTotalController: NavHostController) {
    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()
    SnackbarHost(hostState = snackState, Modifier.zIndex(1f))

    val state = rememberDateRangePickerState()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        CenterAlignedTopAppBar(
            title = { Text(text = stringResource(R.string.exam_dates)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }, actions = {
                IconButton(
                    onClick = {
                        snackScope.launch {
                            snackState.showSnackbar(
                                "Saved range (timestamps): " +
                                        "${state.selectedStartDateMillis!!..state.selectedEndDateMillis!!}"
                            )
                        }
                        navTotalController.navigate(Screens.Inputting.router)
                    },
                    enabled = state.selectedEndDateMillis != null
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NavigateNext,
                        contentDescription = stringResource(R.string.next)
                    )
                }
            })
    }) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            DateRangePicker(modifier = Modifier.fillMaxSize(), state = state)
        }
    }
}


@Preview(group = "DateRangePicker", device = "id:pixel_7_pro")
@Composable
fun DRP_preview() {
    DRP(navTotalController = rememberNavController())
}