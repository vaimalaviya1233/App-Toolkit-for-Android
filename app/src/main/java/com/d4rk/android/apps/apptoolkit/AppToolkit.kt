@file:Suppress("DEPRECATION")

package com.d4rk.android.apps.apptoolkit

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
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

    private suspend fun initializeAds() {
        adsCoreManager.initializeAds(AdsConstants.APP_OPEN_UNIT_ID)
    }

    override fun onStart(owner: LifecycleOwner) {
        currentActivity?.let { adsCoreManager.showAdIfAvailable(it, owner.lifecycleScope) }
    }

    override fun onResume(owner: LifecycleOwner) {
        billingRepository.processPastPurchases()
    }

    override fun onActivityCreated(activity : Activity , savedInstanceState : Bundle?) {}

    override fun onActivityStarted(activity : Activity) {
        currentActivity = activity
    }
}