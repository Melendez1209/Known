package com.melendez.known.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.junkfood.seal.ui.svg.drawablevectors.coder
import com.melendez.known.R
import com.melendez.known.svg.DynamicColorImageVectors
import com.melendez.known.ui.components.CreditItem
import com.melendez.known.ui.components.SharedTopBar

data class Dependency(
    val title: String,
    val license: String,
    val url: String
)

const val Apache = "Apache License 2.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Credits(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Column {
        SharedTopBar(
            title = stringResource(R.string.credits),
            widthSizeClass = widthSizeClass,
            navTotalController = navTotalController,
            scrollBehavior = scrollBehavior
        )
        Credits_Content(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection))
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun Credits_Content(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 所有依赖项列表，按类别分组
    val dependencies = listOf(
        // AndroidX 依赖
        Dependency("AndroidX Core", Apache, "https://github.com/androidx/androidx"),
        Dependency("Coil Compose", Apache, "https://github.com/coil-kt/coil"),
        Dependency("Volley", Apache, "https://github.com/google/volley"),
        Dependency(
            "Google Generative AI",
            Apache,
            "https://github.com/google/generative-ai-android"
        ),
        Dependency(
            "Material Icons Extended",
            Apache,
            "https://mvnrepository.com/artifact/androidx.compose.material/material-icons-extended"
        ),
        Dependency(
            "Navigation Compose",
            Apache,
            "https://github.com/androidx/androidx"
        ),

        // Firebase
        Dependency(
            "Firebase BOM",
            Apache,
            "https://github.com/firebase/firebase-android-sdk"
        ),
        Dependency(
            "Firebase Auth",
            Apache,
            "https://github.com/firebase/firebase-android-sdk"
        ),
        Dependency(
            "Google Play Services Auth",
            Apache,
            "https://github.com/google/google-api-java-client"
        ),
        Dependency(
            "FirebaseUI Auth",
            Apache,
            "https://github.com/firebase/FirebaseUI-Android"
        ),
        Dependency(
            "Firebase Messaging",
            Apache,
            "https://github.com/firebase/firebase-android-sdk"
        ),
        Dependency(
            "Google Play Services Ads",
            Apache,
            "https://mvnrepository.com/artifact/com.google.android.gms/play-services-ads"
        ),

        // Lifecycle
        Dependency("Activity KTX", Apache, "https://github.com/androidx/androidx"),
        Dependency(
            "Activity Compose",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Lifecycle ViewModel Compose",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Lifecycle Runtime KTX",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Lifecycle Runtime Compose",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Lifecycle Process",
            Apache,
            "https://github.com/androidx/androidx"
        ),

        // Compose
        Dependency("Compose UI", Apache, "https://github.com/androidx/androidx"),
        Dependency(
            "Compose Animation",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Compose UI Graphics",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Compose UI Tooling Preview",
            Apache,
            "https://github.com/androidx/androidx"
        ),
        Dependency(
            "Compose Runtime LiveData",
            Apache,
            "https://github.com/androidx/androidx"
        ),

        // Room
        Dependency("Room Runtime", Apache, "https://github.com/androidx/androidx"),
        Dependency("Room KTX", Apache, "https://github.com/androidx/androidx"),

        // Material
        Dependency(
            "Material 3",
            Apache,
            "https://github.com/material-components/material-components-android"
        ),
        Dependency(
            "Material 3 Window Size Class",
            Apache,
            "https://github.com/androidx/androidx"
        ),

        // Accompanist
        Dependency(
            "Accompanist SwipeRefresh",
            Apache,
            "https://github.com/google/accompanist"
        ),
        Dependency(
            "Accompanist Pager Indicators",
            Apache,
            "https://github.com/google/accompanist"
        )
    )

    LazyColumn(modifier = modifier) {
        item {
            Surface(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clearAndSetSemantics {},
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                val painter =
                    rememberVectorPainter(image = DynamicColorImageVectors.coder())
                Image(
                    painter = painter,
                    contentDescription = stringResource(R.string.credits),
                    modifier = Modifier.padding(horizontal = 72.dp, vertical = 48.dp)
                )
            }

        }
        items(dependencies) { dependency ->
            CreditItem(
                title = dependency.title,
                license = dependency.license
            ) {
                openUrl(dependency.url, context)
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
private fun Credits_Preview() {
    Credits(WindowWidthSizeClass.Compact, rememberNavController())
}