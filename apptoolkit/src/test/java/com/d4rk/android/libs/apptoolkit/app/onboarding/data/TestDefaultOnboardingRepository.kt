package com.d4rk.android.libs.apptoolkit.app.onboarding.data

import app.cash.turbine.test
import com.d4rk.android.libs.apptoolkit.app.onboarding.data.repository.DefaultOnboardingRepository
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestDefaultOnboardingRepository {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `observeOnboardingCompletion emits inverse of startup`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] observeOnboardingCompletion emits inverse of startup")
        val startupFlow = MutableSharedFlow<Boolean>()
        val dataStore = mockk<CommonDataStore> {
            every { startup } returns startupFlow
        }
        val repository = DefaultOnboardingRepository(
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        repository.observeOnboardingCompletion().test {
            startupFlow.emit(true)
            assertThat(awaitItem()).isFalse()

            startupFlow.emit(false)
            assertThat(awaitItem()).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
        println("🏁 [TEST DONE] observeOnboardingCompletion emits inverse of startup")
    }

    @Test
    fun `setOnboardingCompleted saves false`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] setOnboardingCompleted saves false")
        val dataStore = mockk<CommonDataStore>()
        coEvery { dataStore.saveStartup(any()) } returns Unit

        val repository = DefaultOnboardingRepository(
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        repository.setOnboardingCompleted()

        coVerify { dataStore.saveStartup(isFirstTime = false) }
        println("🏁 [TEST DONE] setOnboardingCompleted saves false")
    }
}

