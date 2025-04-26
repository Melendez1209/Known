package com.melendez.known

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.melendez.known.core.AppOpenAdManager

class App : Application() {
    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) {}
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd(this)
    }
} 