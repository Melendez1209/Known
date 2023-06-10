package com.melendez.known.main.inners

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.melendez.known.R
import com.melendez.known.Screens

@Composable
fun Me(navTotalController: NavHostController) {
    Surface {
        Column {

            var image_Url: Any? by rememberSaveable { mutableStateOf(R.drawable.baseline_account_circle_24) } // TODO:Make it savable
            val photoPicker =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                    if (it != null) {
                        Log.d("Melendez", "Me: Url = $it")
                        image_Url = it
                    } else {
                        Log.d("Melendez", "Me: No media selected")
                    }
                }

            AsyncImage(
                modifier = Modifier
                    .height(60.dp)
                    .width(60.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
                    .clip(CircleShape)
                    .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                model = ImageRequest.Builder(LocalContext.current).data(image_Url)
                    .crossfade(enable = false).build(),
                contentDescription = stringResource(R.string.avatar),
                contentScale = ContentScale.Crop
            )

            NavigationCard(
                navTotalController = navTotalController,
                router = Screens.Settings.router,
                icon = Icons.Rounded.Settings,
                title = stringResource(
                    id = R.string.settings
                )
            )
            NavigationCard(
                navTotalController = navTotalController,
                router = Screens.About.router,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                    imageVector = icon,
                    contentDescription = stringResource(id = R.string.settings)
                )
                Text(text = title, style = MaterialTheme.typography.titleMedium)
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