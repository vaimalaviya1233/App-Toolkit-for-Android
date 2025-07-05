package com.d4rk.android.libs.apptoolkit.app.oboarding.ui

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import com.d4rk.android.libs.apptoolkit.app.oboarding.ui.OnboardingViewModel

class TestOnboardingViewModel {

    @Test
    fun `default tab index is zero`() {
        val viewModel = OnboardingViewModel()
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `tab index can be changed`() {
        val viewModel = OnboardingViewModel()
        viewModel.currentTabIndex = 1
        assertThat(viewModel.currentTabIndex).isEqualTo(1)
    }

    @Test
    fun `setting negative tab index`() {
        val viewModel = OnboardingViewModel()
        viewModel.currentTabIndex = -1
        assertThat(viewModel.currentTabIndex).isEqualTo(-1)
    }

    @Test
    fun `resetting tab index to default`() {
        val viewModel = OnboardingViewModel()
        viewModel.currentTabIndex = 2
        viewModel.currentTabIndex = 0
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }

    @Test
    fun `setting extremely large tab index`() {
        val viewModel = OnboardingViewModel()
        viewModel.currentTabIndex = Int.MAX_VALUE
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
            viewModel.currentTabIndex = index
            viewModel.currentTabIndex = 0
        }
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
    }
}
