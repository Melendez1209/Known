package com.melendez.known.core

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

/**
 * App Open Ad Manager for handling app open advertisements
 */
class AppOpenAdManager(private val application: Application) : DefaultLifecycleObserver,
    Application.ActivityLifecycleCallbacks {
    private val TAG = "AppOpenAdManager"
    private val AD_UNIT_ID = "ca-app-pub-6702369759910475/9508884421"
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.Companion.get().lifecycle.addObserver(this)
    }

    /**
     * Show open ads when the app resumes to the frontend
     */
    override fun onResume(owner: LifecycleOwner) {
        currentActivity?.let { showAdIfAvailable(it) }
    }

    /**
     * Request an App Open Ad
     */
    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    Log.d(TAG, "App Open广告加载成功")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    setupFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(TAG, "App Open广告加载失败: ${loadAdError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    /**
     * Check if adverts are loaded and available
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && !isAdExpired()
    }

    /**
     * Check if the advert has expired (4h)
     */
    private fun isAdExpired(): Boolean {
        val currentTime = Date().time
        val adExpireTime = 1000 * 60 * 60 * 4
        return loadTime > 0 && currentTime - loadTime > adExpireTime
    }

    /**
     * Setting up full-screen content callbacks
     */
    private fun setupFullScreenContentCallback() {
        fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "广告被关闭")
                appOpenAd = null
                isShowingAd = false
                loadAd(application)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(TAG, "广告展示失败: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd(application)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "广告正在展示")
                isShowingAd = true
            }
        }
    }

    /**
     * Show the Ad
     */
    fun showAdIfAvailable(activity: Activity) {
        if (isShowingAd || !isAdAvailable() || activity.isFinishing) {
            return
        }
        appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
        appOpenAd?.show(activity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (!isShowingAd) {
            currentActivity = activity
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = null
    }
}