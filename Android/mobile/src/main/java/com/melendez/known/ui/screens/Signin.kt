package com.melendez.known.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.melendez.known.R
import com.melendez.known.ui.components.SharedTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Signin(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    Column {
        SharedTopBar(
            title = stringResource(R.string.sign_in),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController
        )
        Signin_Content()
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Signin_Content() {
    Column {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            onClick = {}
        ) { Text(stringResource(R.string.google)) }
    }
}