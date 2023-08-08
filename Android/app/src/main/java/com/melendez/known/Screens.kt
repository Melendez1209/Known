package com.melendez.known

sealed class Screens(val router: String) {
    data object Main : Screens("MainScreen")
    data object Settings : Screens("Settings")
    data object DRP : Screens("DateRangePicker")
    data object Inputting : Screens("Inputting")
    data object About : Screens("About")
    data object Feedback : Screens("Feedback")
    data object Bug : Screens("Bug")
    data object Feature : Screens("Feature")
    data object Signin : Screens("Signin")
    data object Signup : Screens("Signup")
    data object Detail : Screens("Detail")
}