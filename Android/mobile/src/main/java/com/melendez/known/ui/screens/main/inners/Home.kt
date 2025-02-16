package com.melendez.known.ui.screens.main.inners

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.melendez.known.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(paddingValues: PaddingValues? = null) {
    Surface {

        data class CarouselItem(
            val id: Int,
            @DrawableRes val drawableRes: Int,
            @StringRes val contentDescriptionResId: Int,
        )

        val items =
            listOf(
                CarouselItem(0, R.drawable.sample0, R.string.sample),
                CarouselItem(1, R.drawable.sample1, R.string.sample),
                CarouselItem(2, R.drawable.sample2, R.string.sample),
                CarouselItem(3, R.drawable.sample3, R.string.sample)
            )
        HorizontalMultiBrowseCarousel(
            state = rememberCarouselState { items.count() },
            preferredItemWidth = 260.dp,
            modifier = Modifier.fillMaxWidth(),
            itemSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { i ->
            val item = items[i]
            Image(
                painter = rememberAsyncImagePainter(item.drawableRes),
                contentDescription = stringResource(item.contentDescriptionResId) + i,
                modifier = Modifier
                    .height(220.dp)
                    .maskClip(MaterialTheme.shapes.extraLarge),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Home_Preview() {
    Home()
}