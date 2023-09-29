package com.melendez.known.main.inners

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Me(navTotalController: NavHostController) {
    Surface {
        Column {
            var imageUrl: Any? by rememberSaveable { mutableStateOf(R.drawable.outline_account_circle_24) }
            val photoPicker =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) {
                    if (it != null) {
                        Log.d("Melendez", "Me: Url = $it")
                        imageUrl = it
                    } else {
                        Log.d("Melendez", "Me: No media selected")
                    }
                }

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(imageUrl)
                    .crossfade(enable = false).build(),
                contentDescription = stringResource(R.string.avatar),
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = { navTotalController.navigate(Screens.Signin.router) },
                        onDoubleClick = { navTotalController.navigate(Screens.Signup.router) },
                        onLongClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ),
                contentScale = ContentScale.Crop
            )
            ListItem(
                headlineContent = { Text(text = stringResource(id = R.string.settings)) },
                modifier = Modifier
                    .clickable { navTotalController.navigate(Screens.Settings.router) }
                    .fillMaxWidth(),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = stringResource(id = R.string.settings)
                    )
                }
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(text = stringResource(R.string.about)) },
                modifier = Modifier
                    .clickable { navTotalController.navigate(Screens.About.router) }
                    .fillMaxWidth(),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = stringResource(R.string.about)
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = stringResource(R.string.about)
                    )
                }
            )
        }
    }
}

@Preview(device = "id:pixel_7_pro", showBackground = true)
@Composable
fun Me_Preview() {
    Me(navTotalController = rememberNavController())
}