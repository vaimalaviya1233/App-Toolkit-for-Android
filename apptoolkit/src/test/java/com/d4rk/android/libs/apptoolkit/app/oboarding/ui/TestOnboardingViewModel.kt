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
}
