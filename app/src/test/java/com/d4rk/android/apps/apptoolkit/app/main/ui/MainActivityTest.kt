package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ConsentManagerHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.google.android.gms.ads.MobileAds
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRuns
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertDoesNotThrow
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityTest {

    private val testScheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(testScheduler)
    private val unconfinedDispatcher = UnconfinedTestDispatcher(testScheduler)

    private lateinit var dataStore: DataStore
    private lateinit var dispatchers: DispatcherProvider
    private lateinit var activity: MainActivity

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        dataStore = mockk(relaxed = true)
        dispatchers = object : DispatcherProvider {
            override val main = mainDispatcher
            override val io = mainDispatcher
            override val default = mainDispatcher
            override val unconfined = unconfinedDispatcher
        }
        activity = spyk(MainActivity(), recordPrivateCalls = true)
        replaceLazyDelegate(activity, "dataStore\$delegate", lazyOf(dataStore))
        replaceLazyDelegate(activity, "dispatchers\$delegate", lazyOf(dispatchers))
        moveLifecycleToState(activity, Lifecycle.State.RESUMED)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        testScheduler.advanceUntilIdle()
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `handleStartup when startup is true starts startup activity`() = runTest {
        every { dataStore.startup } returns flowOf(true)
        mockkObject(IntentsHelper)
        every { IntentsHelper.openActivity(any(), any()) } justRuns

        activity["handleStartup"]()
        testScheduler.advanceUntilIdle()

        verify { activity["startStartupActivity"]() }
        verify(exactly = 0) { activity["setMainActivityContent"]() }
        assertFalse(readSplashVisibility(activity))
    }

    @Test
    fun `handleStartup when startup is false sets main content`() = runTest {
        every { dataStore.startup } returns flowOf(false)
        every { activity["setMainActivityContent"]() } justRuns

        activity["handleStartup"]()
        testScheduler.advanceUntilIdle()

        verify { activity["setMainActivityContent"]() }
        verify(exactly = 0) { activity["startStartupActivity"]() }
        assertFalse(readSplashVisibility(activity))
    }

    @Test
    fun `handleStartup when startup flow throws still dismisses splash`() = runTest {
        every { dataStore.startup } returns flow { throw IllegalStateException("boom") }
        every { activity["setMainActivityContent"]() } justRuns

        assertDoesNotThrow {
            activity["handleStartup"]()
            testScheduler.advanceUntilIdle()
        }

        assertFalse(readSplashVisibility(activity))
        verify(exactly = 0) { activity["startStartupActivity"]() }
        verify { activity["setMainActivityContent"]() }
    }

    @Test
    fun `initializeDependencies when MobileAds throws does not crash`() = runTest {
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } throws IllegalStateException("ads")
        mockkObject(ConsentManagerHelper)
        coEvery { ConsentManagerHelper.applyInitialConsent(any()) } justRuns

        assertDoesNotThrow {
            activity["initializeDependencies"]()
            testScheduler.advanceUntilIdle()
        }

        verify { MobileAds.initialize(any(), any()) }
        coVerify { ConsentManagerHelper.applyInitialConsent(dataStore) }
    }

    @Test
    fun `initializeDependencies when ConsentManagerHelper throws does not crash`() = runTest {
        mockkStatic(MobileAds::class)
        every { MobileAds.initialize(any(), any()) } justRuns
        mockkObject(ConsentManagerHelper)
        coEvery { ConsentManagerHelper.applyInitialConsent(any()) } throws IllegalStateException("consent")

        assertDoesNotThrow {
            activity["initializeDependencies"]()
            testScheduler.advanceUntilIdle()
        }

        verify { MobileAds.initialize(any(), any()) }
        coVerify { ConsentManagerHelper.applyInitialConsent(dataStore) }
    }

    private fun replaceLazyDelegate(target: Any, fieldName: String, value: Lazy<*>) {
        val field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    private fun moveLifecycleToState(activity: MainActivity, state: Lifecycle.State) {
        val lifecycle = activity.lifecycle
        if (lifecycle is LifecycleRegistry) {
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            if (state == Lifecycle.State.CREATED) {
                lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        }
    }

    private fun readSplashVisibility(activity: MainActivity): Boolean {
        val field = activity.javaClass.getDeclaredField("keepSplashVisible")
        field.isAccessible = true
        return field.getBoolean(activity)
    }
}
