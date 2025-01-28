package com.melendez.known.ui.screens.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.melendez.known.R

sealed class Screens(
    val router: String,
    @StringRes val resourceId: Int,
    val iconSelected: ImageVector,
    val iconUnelected: ImageVector,
) {
    data object Home : Screens("home", R.string.home, Icons.Rounded.Home, Icons.Outlined.Home)
    data object History :
        Screens("history", R.string.history, Icons.Rounded.History, Icons.Outlined.History)

    data object Me :
        Screens("account", R.string.me, Icons.Rounded.AccountCircle, Icons.Outlined.AccountCircle)
}