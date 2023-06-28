package com.melendez.known.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R


@Composable
fun AboutScreen(widthSizeClass: WindowWidthSizeClass) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> AboutScreen_Compat()
        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> AboutScreen_MediumExpanded()
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun AboutScreen_Compat() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(vertical = 12.dp)
            )
            About_Content()
        }
    }
}

@Preview(device = "spec:parent=pixel_7_pro,orientation=landscape")
@Composable
fun AboutScreen_MediumExpanded() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(240.dp)
                    .padding(vertical = 12.dp)
            )
            About_Content()
        }
    }
}

@Preview(device = "id:pixel_7_pro")
@Composable
fun About_Content() {

    val context = LocalContext.current

    Column {
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Melendez1209/known/graphs/contributors")
                )
                context.startActivity(intent)
            },
            headlineContent = { Text(text = stringResource(id = R.string.developers)) },
            supportingContent = { Text(text = stringResource(id = R.string.all_contributors)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.PeopleAlt,
                    contentDescription = stringResource(id = R.string.developers)
                )
            }
        )
        Divider()
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Melendez1209/Known/blob/main/LICENSE")
                )
                context.startActivity(intent)
            },
            headlineContent = { Text(text = stringResource(id = R.string.privacy)) },
            supportingContent = { Text(text = stringResource(id = R.string.policy)) },
            leadingContent = {
                Icon(
                    imageVector = Icons.Rounded.PrivacyTip,
                    contentDescription = stringResource(id = R.string.developers)
                )
            }
        )
    }
}