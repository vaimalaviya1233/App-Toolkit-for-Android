package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository.OnboardingRepository
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

private class FakeOnboardingRepository : OnboardingRepository {
    var completed = false
    var shouldFail = false
    private val completion = MutableStateFlow(false)

    override fun observeOnboardingCompletion(): Flow<Boolean> = completion

    override suspend fun setOnboardingCompleted() {
        if (shouldFail) throw RuntimeException("fail")
        completed = true
        completion.value = true
    }

    suspend fun emit(value: Boolean) {
        completion.emit(value)
    }
}

private class ErrorOnboardingRepository : OnboardingRepository {
    override fun observeOnboardingCompletion(): Flow<Boolean> = flow { throw IllegalStateException("boom") }
    override suspend fun setOnboardingCompleted() {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestOnboardingViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `current tab index mutates as expected`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] current tab index mutates as expected")
        val viewModel = OnboardingViewModel(repository = FakeOnboardingRepository())

        // Default value
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(0)

        // Changing the index updates the state
        viewModel.updateCurrentTab(1)
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(1)

        // Negative values are also accepted
        viewModel.updateCurrentTab(-1)
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(-1)

        // Extremely large values do not break the model
        viewModel.updateCurrentTab(Int.MAX_VALUE)
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(Int.MAX_VALUE)

        // Reset back to default
        viewModel.updateCurrentTab(0)
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(0)
        println("🏁 [TEST DONE] current tab index mutates as expected")
    }

    @Test
    fun `repeated index changes remain stable`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] repeated index changes remain stable")
        val viewModel = OnboardingViewModel(repository = FakeOnboardingRepository())

        repeat(5) { index ->
            viewModel.updateCurrentTab(index)
        }

        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(4)

        viewModel.updateCurrentTab(0)
        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(0)
        println("🏁 [TEST DONE] repeated index changes remain stable")
    }

    @Test
    fun `repository completion updates state`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] repository completion updates state")
        val repository = FakeOnboardingRepository()
        val viewModel = OnboardingViewModel(repository = repository)

        repository.emit(true)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOnboardingCompleted).isTrue()
        println("🏁 [TEST DONE] repository completion updates state")
    }

    @Test
    fun `completeOnboarding sets completion and calls callback`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] completeOnboarding sets completion and calls callback")
        val repository = FakeOnboardingRepository()
        val viewModel = OnboardingViewModel(repository = repository)
        var callbackInvoked = false

        viewModel.completeOnboarding { callbackInvoked = true }
        advanceUntilIdle()

        assertThat(repository.completed).isTrue()
        assertThat(callbackInvoked).isTrue()
        assertThat(viewModel.uiState.value.isOnboardingCompleted).isTrue()
        println("🏁 [TEST DONE] completeOnboarding sets completion and calls callback")
    }

    @Test
    fun `completeOnboarding failure resets completion`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] completeOnboarding failure resets completion")
        val repository = FakeOnboardingRepository().apply { shouldFail = true }
        val viewModel = OnboardingViewModel(repository = repository)
        var callbackInvoked = false

        viewModel.completeOnboarding { callbackInvoked = true }
        advanceUntilIdle()

        assertThat(repository.completed).isFalse()
        assertThat(callbackInvoked).isFalse()
        assertThat(viewModel.uiState.value.isOnboardingCompleted).isFalse()
        println("🏁 [TEST DONE] completeOnboarding failure resets completion")
    }

    @Test
    fun `repository error results in false state`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] repository error results in false state")
        val viewModel = OnboardingViewModel(repository = ErrorOnboardingRepository())

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.isOnboardingCompleted).isFalse()
        println("🏁 [TEST DONE] repository error results in false state")
    }

    @Test
    fun `provideFactory creates viewmodel`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] provideFactory creates viewmodel")
        val repository = FakeOnboardingRepository()
        val factory = OnboardingViewModel.provideFactory(repository)

        val viewModel = factory.create(OnboardingViewModel::class.java)

        assertThat(viewModel.uiState.value.currentTabIndex).isEqualTo(0)
        assertThat(viewModel.uiState.value.isOnboardingCompleted).isFalse()
        println("🏁 [TEST DONE] provideFactory creates viewmodel")
    }
}
