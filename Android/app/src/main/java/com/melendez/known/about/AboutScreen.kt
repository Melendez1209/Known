package com.melendez.known.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PeopleAlt
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.melendez.known.R

@Preview(device = "id:pixel_7_pro")
@Composable
fun AboutScreen() {

    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize()) {
        Column {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .height(120.dp)
                    .width(120.dp)
                    .padding(vertical = 12.dp)
                    .align(Alignment.CenterHorizontally)
            )
            ClickableCard(
                imageVector = Icons.Rounded.PeopleAlt,
                {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Melendez1209/known/graphs/contributors")
                    )
                    context.startActivity(intent)
                },
                title = stringResource(id = R.string.members),
                sub = stringResource(id = R.string.all_contributors)
            )
            ClickableCard(
                imageVector = Icons.Rounded.PrivacyTip,
                {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/Melendez1209/Known/blob/main/LICENSE")
                    )
                    context.startActivity(intent)
                },
                title = stringResource(id = R.string.privacy),
                sub = stringResource(id = R.string.policy)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableCard(imageVector: ImageVector, onClick: () -> Unit, title: String, sub: String) {

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row {
            Icon(
                imageVector = imageVector,
                contentDescription = stringResource(R.string.members),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 12.dp)
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_7_pro")
@Composable
fun ClickableCard_Preview() {
    ClickableCard(
        imageVector = Icons.Rounded.PeopleAlt, {}, stringResource(id = R.string.members),
        stringResource(id = R.string.all_contributors)
    )
}