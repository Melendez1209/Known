package com.melendez.known.main.screens

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.melendez.known.R


sealed class Screens(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Home : Screens("home", R.string.home, Icons.Rounded.Home)
    object History : Screens("history", R.string.history, Icons.Rounded.History)
    object Account : Screens("account", R.string.account, Icons.Rounded.AccountBox)
}