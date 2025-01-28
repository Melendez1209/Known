package com.melendez.known

import android.app.Application
import kotlinx.coroutines.CoroutineScope

class App : Application() {
    companion object {
        lateinit var applicationScope: CoroutineScope
    }
}