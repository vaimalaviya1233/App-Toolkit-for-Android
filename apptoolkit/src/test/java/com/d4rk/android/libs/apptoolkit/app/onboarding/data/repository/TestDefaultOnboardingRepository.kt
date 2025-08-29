package com.d4rk.android.libs.apptoolkit.app.onboarding.data.repository

import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class TestDefaultOnboardingRepository {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `observeOnboardingCompletion emits inverted startup`() = runTest(dispatcherExtension.testDispatcher) {
        val startupFlow = MutableSharedFlow<Boolean>()
        val dataStore = mockk<CommonDataStore>()
        every { dataStore.startup } returns startupFlow
        val repository = DefaultOnboardingRepository(
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )
        val values = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeOnboardingCompletion().toList(values)
        }

        startupFlow.emit(true)
        startupFlow.emit(false)

        assertThat(values).containsExactly(false, true).inOrder()
    }

    @Test
    fun `setOnboardingCompleted saves value`() = runTest(dispatcherExtension.testDispatcher) {
        val dataStore = mockk<CommonDataStore>()
        coEvery { dataStore.saveStartup(isFirstTime = false) } returns Unit
        val repository = DefaultOnboardingRepository(
            dataStore = dataStore,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        repository.setOnboardingCompleted()

        coVerify { dataStore.saveStartup(isFirstTime = false) }
    }
}

