package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import com.d4rk.android.libs.apptoolkit.app.onboarding.domain.repository.OnboardingRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

private class FakeOnboardingRepository : OnboardingRepository {
    var completed = false
    private val completion = MutableStateFlow(false)

    override fun observeOnboardingCompletion(): Flow<Boolean> = completion

    override suspend fun setOnboardingCompleted() {
        completed = true
        completion.value = true
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TestOnboardingViewModel {

    @Test
    fun `current tab index mutates as expected`() = runTest {
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
    fun `repeated index changes remain stable`() = runTest {
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
    fun `complete onboarding calls repository`() = runTest {
        val repository = FakeOnboardingRepository()
        val viewModel = OnboardingViewModel(repository = repository)
        viewModel.completeOnboarding {}
        advanceUntilIdle()
        assertThat(repository.completed).isTrue()
        assertThat(viewModel.uiState.value.isOnboardingCompleted).isTrue()
    }
}
