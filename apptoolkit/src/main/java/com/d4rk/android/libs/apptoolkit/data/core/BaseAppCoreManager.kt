@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.data.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.d4rk.android.libs.apptoolkit.data.client.KtorClient
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

abstract class BaseAppCoreManager : MultiDexApplication(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    protected lateinit var ktorClient: HttpClient
    private var isAppLoaded: Boolean = false
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        CoroutineScope(Dispatchers.IO).launch {
            initializeBaseComponents()
        }
    }

    private suspend fun initializeBaseComponents() = supervisorScope {
        val ktor = async { initializeKtorClient() }

        ktor.await()

        initializeApp() // Calls the app-specific initialization
        finalizeInitialization()
    }

    private suspend fun initializeKtorClient() {
        runCatching {
            ktorClient = KtorClient().createClient()
        }.onFailure {
            handleInitializationFailure("Ktor client initialization failed", it)
        }
    }

    protected open suspend fun initializeApp() {
        // Allow subclasses to override this to add custom initialization logic
    }

    protected open fun handleInitializationFailure(message: String, exception: Throwable) {
        Log.e("BaseAppCoreManager" , "$message: ${exception.message}" , exception)
    }

    private fun finalizeInitialization() {
        isAppLoaded = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        currentActivity?.let { showAdIfNeeded(it) }
    }

    open fun showAdIfNeeded(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}