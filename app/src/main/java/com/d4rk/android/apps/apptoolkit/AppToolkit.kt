@file:Suppress("DEPRECATION")

package com.d4rk.android.apps.apptoolkit

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.core.di.initializeKoin
import com.d4rk.android.apps.apptoolkit.core.utils.constants.ads.AdsConstants
import com.d4rk.android.libs.apptoolkit.data.core.BaseCoreManager
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.koin.android.ext.android.getKoin

class AppToolkit : BaseCoreManager(), DefaultLifecycleObserver {
    private var currentActivity : Activity? = null

    private val adsCoreManager : AdsCoreManager by lazy { getKoin().get<AdsCoreManager>() }

    override fun onCreate() {
        initializeKoin(context = this)
        super<BaseCoreManager>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer = this)
    }

    override suspend fun onInitializeApp() : Unit = supervisorScope {
        listOf(async { initializeAds() }).awaitAll()
    }

    private fun initializeAds() {
        adsCoreManager.initializeAds(AdsConstants.APP_OPEN_UNIT_ID)
    }

    override fun onStart(owner: LifecycleOwner) {
        val startTime = System.currentTimeMillis()
        Log.d("AppToolkit", "onStart at $startTime")
        currentActivity?.let { activity ->
            if (BuildConfig.DEFER_APP_OPEN) {
                activity.window.decorView.post {
                    val firstFrameTime = System.currentTimeMillis()
                    Log.d("AppToolkit", "first frame at $firstFrameTime")
                    adsCoreManager.showAdIfAvailable(activity, owner.lifecycleScope)
                    Log.d("AppToolkit", "ad show at ${System.currentTimeMillis()}")
                }
            } else {
                adsCoreManager.showAdIfAvailable(activity, owner.lifecycleScope)
                Log.d("AppToolkit", "ad show at ${System.currentTimeMillis()}")
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        billingRepository.processPastPurchases()
    }

    override fun onActivityCreated(activity : Activity , savedInstanceState : Bundle?) {}

    override fun onActivityStarted(activity : Activity) {
        currentActivity = activity
    }
}