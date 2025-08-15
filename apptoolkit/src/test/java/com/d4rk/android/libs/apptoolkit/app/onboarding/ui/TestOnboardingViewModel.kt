package com.d4rk.android.libs.apptoolkit.app.onboarding.ui

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class TestOnboardingViewModel {

    @Test
    fun `current tab index mutates as expected`() {
        println("🚀 [TEST] current tab index mutates as expected")
        val viewModel = OnboardingViewModel()

        // Default value
        assertThat(viewModel.currentTabIndex).isEqualTo(0)

        // Changing the index updates the state
        viewModel.currentTabIndex = 1
        assertThat(viewModel.currentTabIndex).isEqualTo(1)

        // Negative values are also accepted
        viewModel.currentTabIndex = -1
        assertThat(viewModel.currentTabIndex).isEqualTo(-1)

        // Extremely large values do not break the model
        viewModel.currentTabIndex = Int.MAX_VALUE
        assertThat(viewModel.currentTabIndex).isEqualTo(Int.MAX_VALUE)

        // Reset back to default
        viewModel.currentTabIndex = 0
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
        println("🏁 [TEST DONE] current tab index mutates as expected")
    }

    @Test
    fun `repeated index changes remain stable`() {
        println("🚀 [TEST] repeated index changes remain stable")
        val viewModel = OnboardingViewModel()

        repeat(5) { index ->
            viewModel.currentTabIndex = index
        }

        assertThat(viewModel.currentTabIndex).isEqualTo(4)

        viewModel.currentTabIndex = 0
        assertThat(viewModel.currentTabIndex).isEqualTo(0)
        println("🏁 [TEST DONE] repeated index changes remain stable")
    }
}
