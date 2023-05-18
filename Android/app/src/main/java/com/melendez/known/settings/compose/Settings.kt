package com.melendez.known.settings.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.NavigateBefore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings_Compact(navTotalController: NavHostController) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = {
            Text(text = stringResource(R.string.settings))
        }, navigationIcon = {
            IconButton(onClick = { navTotalController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Rounded.NavigateBefore,
                    contentDescription = stringResource(R.string.back)
                )
            }
        }, actions = {
            IconButton(onClick = { TODO("Login") }) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(R.string.login)
                )

            }
        })
    }) {
        Surface {

        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Settings_Compact_Preview() {
    Settings_Compact(navTotalController = rememberNavController())
}