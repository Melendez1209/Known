package com.melendez.known.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens : NavKey {
    @Serializable
    data object Main : Screens()
    @Serializable
    data object Settings : Screens()
    @Serializable
    data object Appearance : Screens()
    @Serializable
    data object Dark : Screens()
    @Serializable
    data object Language : Screens()
    @Serializable
    data object DRP : Screens()
    @Serializable
    data object Inputting : Screens()
    @Serializable
    data object About : Screens()
    @Serializable
    data object Signin : Screens()
    @Serializable
    data object Detail : Screens()
    @Serializable
    data object Prophets : Screens()
    @Serializable
    data object Credits : Screens()
    @Serializable
    data object Guide : Screens()
}