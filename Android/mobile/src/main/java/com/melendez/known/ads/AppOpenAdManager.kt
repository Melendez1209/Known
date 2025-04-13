package com.melendez.known.ads

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
    private val LOG_TAG = "AppOpenAdManager"

    // 正式发布时需要替换为实际的广告ID
    private val AD_UNIT_ID = "ca-app-pub-6702369759910475/9508884421"

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0
    private var currentActivity: Activity? = null
    private var isShowingAd = false

    // 广告回调
    private var fullScreenContentCallback: FullScreenContentCallback? = null

    init {
        // 注册应用生命周期观察者和Activity生命周期回调
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    /**
     * 请求一个App Open广告
     */
    fun loadAd(context: Context) {
        // 如果正在加载广告或广告已加载且未过期，则不重复加载
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
                    Log.d(LOG_TAG, "App Open广告加载成功")
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    setupFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.d(LOG_TAG, "App Open广告加载失败: ${loadAdError.message}")
                    isLoadingAd = false
                }
            }
        )
    }

    /**
     * 检查广告是否已加载且可用
     */
    private fun isAdAvailable(): Boolean {
        return appOpenAd != null && !isAdExpired()
    }

    /**
     * 检查广告是否已过期（超过4小时）
     */
    private fun isAdExpired(): Boolean {
        val currentTime = Date().time
        val adExpireTime = 1000 * 60 * 60 * 4 // 4小时过期时间
        return loadTime > 0 && currentTime - loadTime > adExpireTime
    }

    /**
     * 设置全屏内容回调
     */
    private fun setupFullScreenContentCallback() {
        fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(LOG_TAG, "广告被关闭")
                appOpenAd = null
                isShowingAd = false
                loadAd(application)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.d(LOG_TAG, "广告展示失败: ${adError.message}")
                appOpenAd = null
                isShowingAd = false
                loadAd(application)
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(LOG_TAG, "广告正在展示")
                isShowingAd = true
            }
        }
    }

    /**
     * 显示广告
     */
    fun showAdIfAvailable(activity: Activity) {
        // 如果正在显示广告或广告不可用，则返回
        if (isShowingAd || !isAdAvailable() || activity.isFinishing) {
            return
        }

        appOpenAd?.fullScreenContentCallback = fullScreenContentCallback
        appOpenAd?.show(activity)
    }

    /**
     * 当应用从后台回到前台时尝试显示广告
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // 应用进入前台时，尝试显示广告
        if (!isShowingAd) {
            currentActivity?.let { showAdIfAvailable(it) }
        }
    }

    // Activity生命周期回调
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