package com.d4rk.android.libs.apptoolkit.data.core.ads

import android.app.Activity
import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.utils.interfaces.OnShowAdCompleteListener
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Date

class TestAdsCoreManager {
    @Test
    fun `initializeAds triggers MobileAds`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)

        mockkStatic(MobileAds::class)
        justRun { MobileAds.initialize(context) }

        manager.initializeAds("id")
        verify { MobileAds.initialize(context) }
    }

    @Test
    fun `showAdIfAvailable before init does nothing`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)
        val activity = mockk<Activity>()

        manager.showAdIfAvailable(activity)
    }

    @Test
    fun `loadAd does not load when already loading or available`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)
        manager.initializeAds("unit")

        val mgrField = AdsCoreManager::class.java.getDeclaredField("appOpenAdManager")
        mgrField.isAccessible = true
        val inner = mgrField.get(manager)!!

        val loadingField = inner.javaClass.getDeclaredField("isLoadingAd")
        loadingField.isAccessible = true
        loadingField.setBoolean(inner, true)

        mockkStatic(AppOpenAd::class)
        inner.javaClass.getDeclaredMethod("loadAd", Context::class.java).apply {
            isAccessible = true
            invoke(inner, context)
        }
        verify(exactly = 0) { AppOpenAd.load(any(), any(), any(), any()) }

        loadingField.setBoolean(inner, false)
        val adField = inner.javaClass.getDeclaredField("appOpenAd")
        adField.isAccessible = true
        adField.set(inner, mockk<AppOpenAd>())
        val timeField = inner.javaClass.getDeclaredField("loadTime")
        timeField.isAccessible = true
        timeField.setLong(inner, Date().time)

        inner.javaClass.getDeclaredMethod("loadAd", Context::class.java).apply {
            isAccessible = true
            invoke(inner, context)
        }
        verify(exactly = 0) { AppOpenAd.load(any(), any(), any(), any()) }
    }

    @Test
    fun `showAdIfAvailable loads when no ad`() {
        val context = mockk<Context>()
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)
        manager.initializeAds("unit")

        val dataStore = mockk<CommonDataStore>()
        every { dataStore.ads(any()) } returns flowOf(true)
        val storeField = AdsCoreManager::class.java.getDeclaredField("dataStore")
        storeField.isAccessible = true
        storeField.set(manager, dataStore)

        mockkStatic(AppOpenAd::class)
        justRun { AppOpenAd.load(any(), any(), any(), any()) }

        var completed = false
        val mgrField2 = AdsCoreManager::class.java.getDeclaredField("appOpenAdManager")
        mgrField2.isAccessible = true
        val inner2 = mgrField2.get(manager)!!
        val method = inner2.javaClass.getDeclaredMethod(
            "showAdIfAvailable",
            Activity::class.java,
            OnShowAdCompleteListener::class.java
        )
        method.isAccessible = true
        val listener = object : OnShowAdCompleteListener { override fun onShowAdComplete() { completed = true } }
        method.invoke(inner2, mockk<Activity>(), listener)

        assert(completed)
        verify { AppOpenAd.load(any(), any(), any(), any()) }
    }

    @Test
    fun `callback dismiss reloads ad`() {
        val context = mockk<Context>(relaxed = true)
        val provider = mockk<BuildInfoProvider>()
        val manager = AdsCoreManager(context, provider)
        manager.initializeAds("unit")

        val dataStore = mockk<CommonDataStore>()
        every { dataStore.ads(any()) } returns flowOf(true)
        val storeField = AdsCoreManager::class.java.getDeclaredField("dataStore")
        storeField.isAccessible = true
        storeField.set(manager, dataStore)

        val ad = mockk<AppOpenAd>(relaxed = true)
        val mgrField3 = AdsCoreManager::class.java.getDeclaredField("appOpenAdManager")
        mgrField3.isAccessible = true
        val inner3 = mgrField3.get(manager)!!
        val adField = inner3.javaClass.getDeclaredField("appOpenAd")
        adField.isAccessible = true
        adField.set(inner3, ad)

        mockkStatic(AppOpenAd::class)
        justRun { AppOpenAd.load(any(), any(), any(), any()) }

        val slot = slot<FullScreenContentCallback>()
        every { ad.fullScreenContentCallback = capture(slot) } returns Unit

        val method2 = inner3.javaClass.getDeclaredMethod(
            "showAdIfAvailable",
            Activity::class.java,
            OnShowAdCompleteListener::class.java
        )
        method2.isAccessible = true
        method2.invoke(inner3, mockk<Activity>(), object : OnShowAdCompleteListener {
            override fun onShowAdComplete() {}
        })

        slot.captured.onAdDismissedFullScreenContent()

        val showField = inner3.javaClass.getDeclaredField("isShowingAd")
        showField.isAccessible = true
        assertFalse(showField.getBoolean(inner3))
        verify { AppOpenAd.load(any(), any(), any(), any()) }
    }

}
