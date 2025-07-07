package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TestOnboardingViewModel {

    @Test
    fun `default tab index is zero`() {
        val viewModel = OnboardingViewModel()
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `tab index can be changed`() {
        val viewModel = OnboardingViewModel()
        viewModel.onEvent(OnboardingEvent.SetIndex(1))
        assertThat(viewModel.currentTabIndex).isEqualTo(1)
    }

    @Test
    fun `setting negative tab index`() {
        val viewModel = OnboardingViewModel()
        viewModel.onEvent(OnboardingEvent.SetIndex(-1))
        assertThat(viewModel.currentTabIndex).isEqualTo(-1)
    }

    @Test
    fun `resetting tab index to default`() {
        val viewModel = OnboardingViewModel()
        viewModel.onEvent(OnboardingEvent.SetIndex(2))
        viewModel.onEvent(OnboardingEvent.SetIndex(0))
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `setting extremely large tab index`() {
        val viewModel = OnboardingViewModel()
        viewModel.onEvent(OnboardingEvent.SetIndex(Int.MAX_VALUE))
        assertThat(viewModel.currentTabIndex).isEqualTo(Int.MAX_VALUE)
    }

    @Test
    fun `placeholder for onboarding side effects`() {
        val viewModel = OnboardingViewModel()
        // Future analytics or persistence checks would go here
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `repeated open dismiss remains stable`() {
        val viewModel = OnboardingViewModel()
        repeat(5) { index ->
            viewModel.onEvent(OnboardingEvent.SetIndex(index))
            viewModel.onEvent(OnboardingEvent.SetIndex(0))
        }
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `unknown event does nothing`() {
        val viewModel = OnboardingViewModel()
        viewModel.onEvent(OnboardingEvent.SetIndex(3))
        viewModel.onEvent(OnboardingEvent.Unknown)
        assertThat(viewModel.currentTabIndex).isEqualTo(3)
    }

    @Test
    fun `state persists through saved state handle`() {
        val handle = SavedStateHandle(mapOf("currentTabIndex" to 5))
        val viewModel = OnboardingViewModel(handle)
        assertThat(viewModel.currentTabIndex).isEqualTo(5)
        viewModel.onEvent(OnboardingEvent.Next)
        assertThat(handle.get<Int>("currentTabIndex")).isEqualTo(6)
    }
}
