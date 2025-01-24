package com.d4rk.android.libs.apptoolkit.data.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.multidex.MultiDexApplication
import com.d4rk.android.libs.apptoolkit.data.client.KtorClient
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

open class BaseCoreManager : MultiDexApplication(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    companion object {
        lateinit var ktorClient: HttpClient
            private set

        var isAppLoaded: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        CoroutineScope(Dispatchers.IO).launch {
            initializeApp()
        }
    }

    private suspend fun initializeApp() = supervisorScope {
        val httpClient : Deferred<Unit> = async { initializeKtorClient() }
        val advertisementSdk : Deferred<Unit> = async { initializeAds() }
        val application : Deferred<Unit> = async { onInitializeApp() }

        httpClient.await()
        advertisementSdk.await()
        application.await()

        finalizeInitialization()
    }

    private suspend fun initializeKtorClient() {
        ktorClient = KtorClient().createClient()
    }

    private suspend fun initializeAds() {
        AdsCoreManager(context = this).initializeAds(getAppOpenAdUnitId())
    }

    protected open suspend fun onInitializeApp() {}

    private fun finalizeInitialization() {
        isAppLoaded = true
    }

    protected open fun getAppOpenAdUnitId(): String = ""

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}