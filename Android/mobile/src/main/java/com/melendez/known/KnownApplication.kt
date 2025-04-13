package com.melendez.known

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.melendez.known.ads.AppOpenAdManager

class KnownApplication : Application() {
    
    // App Open广告管理器
    lateinit var appOpenAdManager: AppOpenAdManager
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Google Mobile Ads SDK
        MobileAds.initialize(this) {}
        
        // 初始化App Open广告管理器
        appOpenAdManager = AppOpenAdManager(this)
        
        // 预加载App Open广告
        appOpenAdManager.loadAd(this)
    }
} 