package com.melendez.known.ui.screens.main.inners

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
    Surface(modifier = Modifier.fillMaxSize()) {

        data class CarouselItem(
            val id: Int,
            @DrawableRes val drawableRes: Int,
            @StringRes val contentDescriptionResId: Int,
        )

        val items =
            listOf(
                CarouselItem(0, R.drawable.plane, R.string.app_name),
                CarouselItem(1, R.drawable.hiot, R.string.about),
                CarouselItem(2, R.drawable.sunset, R.string.actual),
                CarouselItem(3, R.drawable.crose, R.string.add)
            )


        Column(
            modifier = if (paddingValues != null) Modifier
                .padding(top = 8.dp, bottom = paddingValues.calculateBottomPadding())
                .fillMaxSize()
            else Modifier
                .padding(top = 8.dp)
                .fillMaxSize()
        ) {
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
                    contentDescription = stringResource(item.contentDescriptionResId),
                    modifier = Modifier
                        .height(220.dp)
                        .maskClip(MaterialTheme.shapes.extraLarge),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Preview(device = "id:pixel_8_pro")
@Composable
fun Home_Preview() {
    Home()
}