package com.melendez.known.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.melendez.known.R


sealed class Screens(val router: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Home : Screens("home", R.string.home, Icons.Rounded.Home)
    object History : Screens("history", R.string.history, Icons.Rounded.History)
    object Me : Screens("account", R.string.me, Icons.Rounded.AccountCircle)
}