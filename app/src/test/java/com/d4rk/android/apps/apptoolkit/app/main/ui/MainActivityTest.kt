package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ReviewHelper
import io.mockk.CapturingSlot
import io.mockk.Runs
import io.mockk.arg
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityTest {

    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)
    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val unconfined: CoroutineDispatcher = dispatcher
    }

    private val dataStore = mockk<DataStore>(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        startKoin {
            modules(
                module {
                    single { dataStore }
                    single<DispatcherProvider> { dispatcherProvider }
                }
            )
        }
        mockkObject(ReviewHelper)
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `eligible review prompts and increments`() {
        every { dataStore.sessionCount } returns flowOf(3)
        every { dataStore.hasPromptedReview } returns flowOf(false)
        coEvery { dataStore.setHasPromptedReview(true) } just Runs
        coEvery { dataStore.incrementSessionCount() } just Runs

        val reviewCallback: CapturingSlot<() -> Unit> = slot()
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                activity = any(),
                sessionCount = 3,
                hasPromptedBefore = false,
                scope = any(),
                onReviewLaunched = capture(reviewCallback)
            )
        } answers {
            reviewCallback.captured.invoke()
        }

        val activity = MainActivity()
        moveToResumed(activity)

        activity.invokeCheckInAppReview()
        scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { dataStore.setHasPromptedReview(true) }
        coVerify(exactly = 1) { dataStore.incrementSessionCount() }
        verify(exactly = 1) {
            ReviewHelper.launchInAppReviewIfEligible(
                activity,
                sessionCount = 3,
                hasPromptedBefore = false,
                scope = any(),
                onReviewLaunched = any()
            )
        }
    }

    @Test
    fun `session count increments even when review helper throws`() {
        every { dataStore.sessionCount } returns flowOf(3)
        every { dataStore.hasPromptedReview } returns flowOf(false)
        coEvery { dataStore.incrementSessionCount() } just Runs

        every {
            ReviewHelper.launchInAppReviewIfEligible(
                activity = any(),
                sessionCount = 3,
                hasPromptedBefore = false,
                scope = any(),
                onReviewLaunched = any()
            )
        } answers {
            val scope = arg<CoroutineScope>(3)
            scope.launch(CoroutineExceptionHandler { _, _ -> }) {
                throw IllegalStateException("boom")
            }
        }

        val activity = MainActivity()
        moveToResumed(activity)

        activity.invokeCheckInAppReview()
        scheduler.runCurrent()
        try {
            scheduler.advanceUntilIdle()
        } catch (_: IllegalStateException) {
            // Ignore exception from the launched coroutine to assert side effects
        }

        coVerify(exactly = 1) { dataStore.incrementSessionCount() }
    }

    private fun moveToResumed(activity: MainActivity) {
        val registry = activity.lifecycle as LifecycleRegistry
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun MainActivity.invokeCheckInAppReview() {
        val method = MainActivity::class.java.getDeclaredMethod("checkInAppReview")
        method.isAccessible = true
        method.invoke(this)
    }
}
