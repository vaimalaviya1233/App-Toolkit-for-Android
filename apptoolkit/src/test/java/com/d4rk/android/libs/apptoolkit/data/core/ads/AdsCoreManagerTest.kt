package com.d4rk.android.libs.apptoolkit.data.core.ads

import android.app.Activity
import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.interfaces.OnShowAdCompleteListener
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.any
import io.mockk.anyConstructed
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalCoroutinesApi::class)
class AdsCoreManagerTest {

    @MockK(relaxed = true)
    private lateinit var context: Context

    @MockK(relaxed = true)
    private lateinit var activity: Activity

    @MockK(relaxed = true)
    private lateinit var buildInfoProvider: BuildInfoProvider

    @MockK(relaxed = true)
    private lateinit var dataStore: CommonDataStore

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(CommonDataStore.Companion)
        every { CommonDataStore.getInstance(any()) } returns dataStore
        every { buildInfoProvider.isDebugBuild } returns false
        every { context.applicationContext } returns context
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `initializeAds creates manager when ads enabled`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = TestDispatcherProvider(dispatcher)
        every { dataStore.ads(default = true) } returns flowOf(true)
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(context) } just Runs

        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)

        manager.initializeAds("test-unit")
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { MobileAds.initialize(context) }
        val field = AdsCoreManager::class.java.getDeclaredField("appOpenAdManager").apply { isAccessible = true }
        assertThat(field.get(manager)).isNotNull()
    }

    @Test
    fun `initializeAds skips setup when ads disabled`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = TestDispatcherProvider(dispatcher)
        every { dataStore.ads(default = true) } returns flowOf(false)
        mockkStatic(MobileAds::class)

        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)

        manager.initializeAds("test-unit")
        testScheduler.advanceUntilIdle()

        verify { MobileAds.initialize(any<Context>()) wasNot Called }
        val field = AdsCoreManager::class.java.getDeclaredField("appOpenAdManager").apply { isAccessible = true }
        assertThat(field.get(manager)).isNull()
    }

    @Test
    fun `showAdIfAvailable delegates to AppOpenAdManager`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = TestDispatcherProvider(dispatcher)
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)

        val innerManager = createAppOpenAdManager(manager)
        val spyManager = spyk(innerManager, recordPrivateCalls = true)
        every { spyManager["showAdIfAvailable"](any<Activity>()) } just Runs
        setField(manager, "appOpenAdManager", spyManager)

        val scope = TestScope(dispatcher)
        manager.showAdIfAvailable(activity, scope)
        scope.runCurrent()

        verify(exactly = 1) { spyManager["showAdIfAvailable"](activity) }
    }

    @Test
    fun `loadAd returns early when already loading`() = runTest {
        val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)
        mockkStatic(AppOpenAd::class)

        setField(appOpenManager, "isLoadingAd", true)

        val method = appOpenManager.javaClass.getDeclaredMethod("loadAd", Context::class.java).apply { isAccessible = true }
        method.invoke(appOpenManager, context)

        verify { AppOpenAd.load(any(), any(), any(), any()) wasNot Called }
    }

    @Test
    fun `loadAd returns early when ad already available`() = runTest {
        val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)
        mockkStatic(AppOpenAd::class)

        setField(appOpenManager, "appOpenAd", mockk<AppOpenAd>())
        setField(appOpenManager, "loadTime", Date().time)

        val method = appOpenManager.javaClass.getDeclaredMethod("loadAd", Context::class.java).apply { isAccessible = true }
        method.invoke(appOpenManager, context)

        verify { AppOpenAd.load(any(), any(), any(), any()) wasNot Called }
    }

    @Test
    fun `isAdAvailable true when loaded within four hours`() = runTest {
        val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)

        setField(appOpenManager, "appOpenAd", mockk<AppOpenAd>())
        setField(appOpenManager, "loadTime", Date().time - 2 * 3_600_000)

        val method = appOpenManager.javaClass.getDeclaredMethod("isAdAvailable").apply { isAccessible = true }
        val result = method.invoke(appOpenManager) as Boolean

        assertThat(result).isTrue()
    }

    @Test
    fun `isAdAvailable false when ad expired`() = runTest {
        val dispatcherProvider = TestDispatcherProvider(StandardTestDispatcher(testScheduler))
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)

        setField(appOpenManager, "appOpenAd", mockk<AppOpenAd>())
        setField(appOpenManager, "loadTime", Date().time - 5 * 3_600_000)

        val method = appOpenManager.javaClass.getDeclaredMethod("isAdAvailable").apply { isAccessible = true }
        val result = method.invoke(appOpenManager) as Boolean

        assertThat(result).isFalse()
    }

    @Test
    fun `showAdIfAvailable without ad invokes callback and reloads`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = TestDispatcherProvider(dispatcher)
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)
        setField(manager, "appOpenAdManager", appOpenManager)
        every { dataStore.ads(default = true) } returns flowOf(true)

        mockkConstructor(AdRequest.Builder::class)
        every { anyConstructed<AdRequest.Builder>().build() } returns mockk()
        mockkStatic(AppOpenAd::class)
        every { AppOpenAd.load(any(), any(), any(), any()) } just Runs

        val listener = mockk<OnShowAdCompleteListener>(relaxed = true)

        callShowAdIfAvailable(appOpenManager, activity, listener)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { listener.onShowAdComplete() }
        verify(exactly = 1) { AppOpenAd.load(any(), any(), any(), any()) }
    }

    @Test
    fun `showAdIfAvailable with ad reloads after dismissal`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatcherProvider = TestDispatcherProvider(dispatcher)
        val manager = AdsCoreManager(context, buildInfoProvider, dispatcherProvider)
        val appOpenManager = createAppOpenAdManager(manager)
        setField(manager, "appOpenAdManager", appOpenManager)
        every { dataStore.ads(default = true) } returns flowOf(true)

        val ad = mockk<AppOpenAd>(relaxed = true)
        val callbackSlot = slot<FullScreenContentCallback>()
        every { ad.fullScreenContentCallback = capture(callbackSlot) } just Runs
        every { ad.show(activity) } answers {
            callbackSlot.captured.onAdDismissedFullScreenContent()
        }

        setField(appOpenManager, "appOpenAd", ad)
        setField(appOpenManager, "loadTime", Date().time)

        mockkConstructor(AdRequest.Builder::class)
        every { anyConstructed<AdRequest.Builder>().build() } returns mockk()
        mockkStatic(AppOpenAd::class)
        every { AppOpenAd.load(any(), any(), any(), any()) } just Runs

        val listener = mockk<OnShowAdCompleteListener>(relaxed = true)

        callShowAdIfAvailable(appOpenManager, activity, listener)
        testScheduler.advanceUntilIdle()

        verify(exactly = 1) { ad.show(activity) }
        verify(exactly = 1) { listener.onShowAdComplete() }
        verify(exactly = 1) { AppOpenAd.load(any(), any(), any(), any()) }
        assertThat(getField(appOpenManager, "appOpenAd")).isNull()
        assertThat(getField(appOpenManager, "isShowingAd")).isEqualTo(false)
    }

    private fun createAppOpenAdManager(outer: AdsCoreManager): Any {
        val clazz = AdsCoreManager::class.java.declaredClasses.first { it.simpleName == "AppOpenAdManager" }
        val constructor = clazz.getDeclaredConstructor(AdsCoreManager::class.java, String::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(outer, "test-unit")
    }

    private fun setField(instance: Any, name: String, value: Any?) {
        val field = instance.javaClass.getDeclaredField(name)
        field.isAccessible = true
        field.set(instance, value)
    }

    private fun getField(instance: Any, name: String): Any? {
        val field = instance.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(instance)
    }

    private suspend fun callShowAdIfAvailable(instance: Any, activity: Activity, listener: OnShowAdCompleteListener) {
        val method = instance.javaClass.getDeclaredMethod(
            "showAdIfAvailable",
            Activity::class.java,
            OnShowAdCompleteListener::class.java,
            Continuation::class.java
        )
        method.isAccessible = true
        suspendCoroutine<Unit> { continuation ->
            try {
                val result = method.invoke(instance, activity, listener, continuation)
                if (result != COROUTINE_SUSPENDED) {
                    continuation.resume(Unit)
                }
            } catch (throwable: Throwable) {
                continuation.resumeWithException(throwable.cause ?: throwable)
            }
        }
    }

    private class TestDispatcherProvider(private val dispatcher: StandardTestDispatcher) : DispatcherProvider {
        override val main = dispatcher
        override val io = dispatcher
        override val default = dispatcher
        override val unconfined = dispatcher
    }
}

