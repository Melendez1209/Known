package com.melendez.known.ui.screens.main.inners

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.melendez.known.R
import com.melendez.known.ui.screens.Screens
import com.melendez.known.util.UserManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Me(navTotalController: NavHostController) {
    val isLoggedIn by UserManager.isLoggedIn.collectAsState()

    Surface {
        Column(
            Modifier.padding(
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
            )
        ) {
            AccountCard(isLoggedIn = isLoggedIn, navTotalController = navTotalController)
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

@Preview(device = "id:pixel_9_pro")
@Composable
fun Me_Preview() {
    Me(navTotalController = rememberNavController())
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountCard(isLoggedIn: Boolean, navTotalController: NavHostController) {

    // Get user information
    val userAvatar by UserManager.userAvatar.collectAsState()
    val userName by UserManager.userName.collectAsState()
    val userEmail by UserManager.userEmail.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                if (!isLoggedIn) {
                    navTotalController.navigate(Screens.Signin.router)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        var imageUrl: Any? by remember {
            mutableStateOf(
                if (isLoggedIn && userAvatar != null) userAvatar
                else Icons.Rounded.AccountCircle
            )
        }
        imageUrl = if (isLoggedIn && userAvatar != null) userAvatar else Icons.Rounded.AccountCircle

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoggedIn && userAvatar != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(enable = true)
                        .build(),
                    contentDescription = stringResource(R.string.avatar),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = stringResource(R.string.avatar),
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (isLoggedIn && !userName.isNullOrEmpty()) userName!!
                    else stringResource(R.string.sign_in),
                    style = MaterialTheme.typography.titleLarge,
                )

                if (isLoggedIn && !userEmail.isNullOrEmpty()) {
                    Text(
                        text = userEmail!!,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (isLoggedIn) {
                IconButton(onClick = { UserManager.signOut() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = stringResource(R.string.sign_out),
                    )
                }
            }
        }
    }
}