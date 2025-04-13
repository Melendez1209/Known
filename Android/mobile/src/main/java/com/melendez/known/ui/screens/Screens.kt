package com.melendez.known.ui.screens

sealed class Screens(val router: String) {
    data object Main : Screens("MainScreen")
    data object Settings : Screens("Appearance")
    data object Dark : Screens("Dark")
    data object Language : Screens("Language")
    data object DRP : Screens("DateRangePicker")
    data object Inputting : Screens("Inputting")
    data object About : Screens("About")
    data object Signin : Screens("Signin")
    data object Detail : Screens("Detail")
    data object Prophets : Screens("Prophets")
    data object Credits : Screens("Credits")
}