package com.melendez.known.main.inners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R

@Composable
fun Me(navTotalController: NavHostController) {
    Surface {
        Column {
            NavigationCard(
                navTotalController = navTotalController,
                router = "Settings",
                icon = Icons.Rounded.Settings,
                title = stringResource(
                    id = R.string.settings
                )
            )
            NavigationCard(
                navTotalController = navTotalController,
                router = "About",
                icon = Icons.Rounded.Info,
                title = stringResource(R.string.about)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCard(
    navTotalController: NavHostController,
    router: String,
    icon: ImageVector,
    title: String,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        onClick = {
            navTotalController.navigate(router)
        }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                    imageVector = icon,
                    contentDescription = stringResource(id = R.string.settings)
                )
                Text(text = title)
            }
            IconButton(
                modifier = Modifier.padding(end = 6.dp),
                onClick = {
                    navTotalController.navigate(router)
                }) {
                Icon(
                    imageVector = Icons.Rounded.NavigateNext,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        }
    }
}

@Preview
@Composable
fun NavigationCard_Preview() {
    NavigationCard(
        navTotalController = rememberNavController(),
        "",
        Icons.Rounded.Settings,
        "Title"
    )
}

@Preview(device = "id:pixel_7_pro", showBackground = true)
@Composable
fun Me_Preview() {
    Me(navTotalController = rememberNavController())
}