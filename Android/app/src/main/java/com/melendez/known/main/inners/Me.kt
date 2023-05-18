package com.melendez.known.main.inners

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Me(navTotalController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = { navTotalController.navigate("Settings") }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Icon(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = stringResource(id = R.string.settings)
                )
                Text(text = stringResource(id = R.string.settings))
            }
            IconButton(
                modifier = Modifier.padding(end = 6.dp),
                onClick = { navTotalController.navigate("Settings") }) {
                Icon(
                    imageVector = Icons.Rounded.NavigateNext,
                    contentDescription = stringResource(id = R.string.settings)
                )
            }
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun Me_Preview() {
    Me(navTotalController = rememberNavController())
}