package com.melendez.known

sealed class Screens(val router: String) {
    object Main : Screens("MainScreen")
    object Settings : Screens("Settings")
    object DRP : Screens("DateRangePicker")
    object Inputting : Screens("Inputting")
}