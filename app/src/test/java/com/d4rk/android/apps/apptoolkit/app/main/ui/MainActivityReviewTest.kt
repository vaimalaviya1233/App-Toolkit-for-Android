package com.d4rk.android.apps.apptoolkit.app.main.ui

import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ReviewHelper
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainActivityReviewTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ReviewHelper)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `checkInAppReview triggers review and updates datastore`() = runTest(testDispatcher) {
        val fakeDataStore = FakeDataStore(sessionCount = 3, hasPromptedReview = false)
        startKoin {
            modules(
                module {
                    single<DataStore> { fakeDataStore.mock }
                    single<DispatcherProvider> { TestDispatchers(testDispatcher) }
                }
            )
        }
        val activity = MainActivity()
        val sessionSlot = slot<Int>()
        val promptedSlot = slot<Boolean>()
        val lambdaSlot = slot<() -> Unit>()
        every {
            ReviewHelper.launchInAppReviewIfEligible(
                activity = activity,
                sessionCount = capture(sessionSlot),
                hasPromptedBefore = capture(promptedSlot),
                scope = any(),
                onReviewLaunched = capture(lambdaSlot)
            )
        } answers {
            lambdaSlot.captured.invoke()
        }

        val method = MainActivity::class.java.getDeclaredMethod("checkInAppReview")
        method.isAccessible = true
        method.invoke(activity)
        advanceUntilIdle()

        assertEquals(3, sessionSlot.captured)
        assertEquals(false, promptedSlot.captured)
        coVerify(exactly = 1) { fakeDataStore.mock.setHasPromptedReview(true) }
        coVerify(exactly = 1) { fakeDataStore.mock.incrementSessionCount() }
    }

    private class FakeDataStore(sessionCount: Int, hasPromptedReview: Boolean) {
        val sessionCountFlow = MutableStateFlow(sessionCount)
        val hasPromptedFlow = MutableStateFlow(hasPromptedReview)
        val mock: DataStore = mockk(relaxed = true)

        init {
            every { mock.sessionCount } returns sessionCountFlow
            every { mock.hasPromptedReview } returns hasPromptedFlow
            coEvery { mock.setHasPromptedReview(any()) } coAnswers {
                hasPromptedFlow.value = firstArg()
            }
            coEvery { mock.incrementSessionCount() } coAnswers {
                sessionCountFlow.value += 1
            }
        }
    }

    private class TestDispatchers(private val dispatcher: TestDispatcher) : DispatcherProvider {
        override val main get() = dispatcher
        override val io get() = dispatcher
        override val default get() = dispatcher
        override val unconfined get() = dispatcher
    }
}

