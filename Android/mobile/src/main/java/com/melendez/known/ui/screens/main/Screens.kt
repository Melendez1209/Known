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
import androidx.navigation3.runtime.NavKey
import com.melendez.known.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class Screens(
    val router: String,
    @StringRes val resourceId: Int,
) : NavKey {
    @Transient
    abstract val iconSelected: ImageVector

    @Transient
    abstract val iconUnelected: ImageVector

    @Serializable
    data object Home : Screens("home", R.string.home) {
        override val iconSelected: ImageVector = Icons.Rounded.Home
        override val iconUnelected: ImageVector = Icons.Outlined.Home
    }

    @Serializable
    data object History :
        Screens("history", R.string.history) {
        override val iconSelected: ImageVector = Icons.Rounded.History
        override val iconUnelected: ImageVector = Icons.Outlined.History
    }

    @Serializable
    data object Me :
        Screens("account", R.string.me) {
        override val iconSelected: ImageVector = Icons.Rounded.AccountCircle
        override val iconUnelected: ImageVector = Icons.Outlined.AccountCircle
    }
}