package com.melendez.known.ui.screens

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Screens : NavKey {
    @Serializable
    data object Main : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object Appearance : NavKey

    @Serializable
    data object Dark : NavKey

    @Serializable
    data object Language : NavKey

    @Serializable
    data object DRP : NavKey

    @Serializable
    data object Inputting : NavKey

    @Serializable
    data object About : NavKey

    @Serializable
    data object Signin : NavKey

    @Serializable
    data object Detail : NavKey

    @Serializable
    data object Prophets : NavKey

    @Serializable
    data object Credits : NavKey

    @Serializable
    data object Guide : NavKey
}