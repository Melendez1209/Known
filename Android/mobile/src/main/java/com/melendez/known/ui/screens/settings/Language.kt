package com.melendez.known.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.melendez.known.R
import com.melendez.known.ui.components.PreferenceSingleChoiceItem
import com.melendez.known.ui.components.PreferenceSubtitle
import com.melendez.known.ui.components.PreferencesHintCard
import com.melendez.known.ui.screens.Screens
import com.melendez.known.ui.screens.weblate
import com.melendez.known.util.LocaleLanguageCodeMap
import com.melendez.known.util.toDisplayName
import java.util.Locale

@Composable
fun Language(widthSizeClass: WindowWidthSizeClass, navTotalController: NavHostController) {
    when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> Language_CompactExpanded(navTotalController = navTotalController)
        WindowWidthSizeClass.Medium -> Language_Medium(navTotalController = navTotalController)
        WindowWidthSizeClass.Expanded -> Language_CompactExpanded(navTotalController = navTotalController)
        else -> Language_CompactExpanded(navTotalController = navTotalController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language_CompactExpanded(navTotalController: NavHostController) {

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val preferredLocales = remember {
        val defaultLocaleListCompat = LocaleListCompat.getDefault()
        val mLocaleSet = mutableSetOf<Locale>()

        for (index in 0..defaultLocaleListCompat.size()) {
            val locale = defaultLocaleListCompat[index]
            if (locale != null) {
                mLocaleSet.add(locale)
            }
        }

        return@remember mLocaleSet
    }
    val supportedLocales = LocaleLanguageCodeMap.keys
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                val uri = Uri.fromParts("package", context.packageName, null)
                data = uri
            }
        } else {
            Intent()
        }

    val selectedLocale by remember { mutableStateOf(Locale.getDefault()) }
    val suggestedLocales =
        remember(preferredLocales) {
            val localeSet = mutableSetOf<Locale>()

            preferredLocales.forEach { desired ->
                val matchedLocale =
                    supportedLocales.firstOrNull { supported ->
                        LocaleListCompat.matchesLanguageAndScript(
                            /* supported = */ desired,
                            /* desired = */ supported,
                        )
                    }
                if (matchedLocale != null) {
                    localeSet.add(matchedLocale)
                }
            }

            return@remember localeSet
        }
    val otherLocales = supportedLocales - suggestedLocales
    val isSystemLocaleSettingsAvailable =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .isNotEmpty()
        } else {
            false
        }

    Column {
        LargeTopAppBar(
            title = { Text(text = stringResource(R.string.language)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navTotalController.navigate(Screens.Feedback.router)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Feedback,
                        contentDescription = stringResource(R.string.feedback)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Language_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            suggestedLocales = suggestedLocales,
            otherLocales = otherLocales,
            isSystemLocaleSettingsAvailable = isSystemLocaleSettingsAvailable,
            onNavigateToSystemLocaleSettings = {
                if (isSystemLocaleSettingsAvailable) {
                    context.startActivity(intent)
                }
            },
            selectedLocale = selectedLocale
        )
    }
}

@SuppressLint("QueryPermissionsNeeded")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Language_Medium(navTotalController: NavHostController) {

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val preferredLocales = remember {
        val defaultLocaleListCompat = LocaleListCompat.getDefault()
        val mLocaleSet = mutableSetOf<Locale>()

        for (index in 0..defaultLocaleListCompat.size()) {
            val locale = defaultLocaleListCompat[index]
            if (locale != null) {
                mLocaleSet.add(locale)
            }
        }

        return@remember mLocaleSet
    }
    val supportedLocales = LocaleLanguageCodeMap.keys
    val intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                val uri = Uri.fromParts("package", context.packageName, null)
                data = uri
            }
        } else {
            Intent()
        }

    val selectedLocale by remember { mutableStateOf(Locale.getDefault()) }
    val suggestedLocales =
        remember(preferredLocales) {
            val localeSet = mutableSetOf<Locale>()

            preferredLocales.forEach { desired ->
                val matchedLocale =
                    supportedLocales.firstOrNull { supported ->
                        LocaleListCompat.matchesLanguageAndScript(
                            /* supported = */ desired,
                            /* desired = */ supported,
                        )
                    }
                if (matchedLocale != null) {
                    localeSet.add(matchedLocale)
                }
            }

            return@remember localeSet
        }
    val otherLocales = supportedLocales - suggestedLocales
    val isSystemLocaleSettingsAvailable =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .isNotEmpty()
        } else {
            false
        }

    Column {
        MediumTopAppBar(
            title = { Text(text = stringResource(R.string.language)) },
            navigationIcon = {
                IconButton(onClick = { navTotalController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateBefore,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        navTotalController.navigate(Screens.Feedback.router)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Feedback,
                        contentDescription = stringResource(R.string.feedback)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
        Language_Content(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            suggestedLocales = suggestedLocales,
            otherLocales = otherLocales,
            isSystemLocaleSettingsAvailable = isSystemLocaleSettingsAvailable,
            onNavigateToSystemLocaleSettings = {
                if (isSystemLocaleSettingsAvailable) {
                    context.startActivity(intent)
                }
            },
            selectedLocale = selectedLocale
        )
    }
}

@Composable
fun Language_Content(
    modifier: Modifier,
    suggestedLocales: Set<Locale>,
    otherLocales: Set<Locale>,
    isSystemLocaleSettingsAvailable: Boolean = false,
    onNavigateToSystemLocaleSettings: () -> Unit,
    selectedLocale: Locale,
    onLanguageSelected: (Locale?) -> Unit = {}
) {

    val uriHandler = LocalUriHandler.current

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = modifier) {
            item {
                PreferencesHintCard(
                    title = stringResource(R.string.translate),
                    description = stringResource(R.string.translate_desc),
                    icon = Icons.Outlined.Translate,
                ) {
                    uriHandler.openUri(weblate)
                }
            }

            if (suggestedLocales.isNotEmpty()) {

                item { PreferenceSubtitle(text = stringResource(id = R.string.suggested)) }

                if (!suggestedLocales.contains(Locale.getDefault())) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = stringResource(id = R.string.follow_system),
                            selected = !suggestedLocales.contains(selectedLocale),
                        ) {
                            onLanguageSelected(null)
                        }
                    }
                }

                for (locale in suggestedLocales) {
                    item {
                        PreferenceSingleChoiceItem(
                            text = locale.toDisplayName(),
                            selected = selectedLocale == locale,
                        ) {
                            onLanguageSelected(locale)
                        }
                    }
                }
            }

            item { PreferenceSubtitle(text = stringResource(id = R.string.all_languages)) }

            for (locale in otherLocales) {
                item {
                    PreferenceSingleChoiceItem(
                        text = locale.toDisplayName(),
                        selected = selectedLocale == locale,
                    ) {
                        onLanguageSelected(locale)
                    }
                }
            }

            if (isSystemLocaleSettingsAvailable) {
                item {
                    androidx.compose.material3.HorizontalDivider()
                    Surface(
                        modifier =
                            Modifier.clickable(onClick = onNavigateToSystemLocaleSettings)
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        PaddingValues(horizontal = 8.dp, vertical = 16.dp)
                                    ),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.system_settings),
                                    maxLines = 1,
                                    style =
                                        MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 20.sp
                                        ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(device = "id:pixel_9_pro")
@Composable
fun Language_Preview() {
    Language(
        widthSizeClass = WindowWidthSizeClass.Compact,
        navTotalController = rememberNavController()
    )
}