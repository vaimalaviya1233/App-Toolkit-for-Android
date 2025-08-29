package com.d4rk.android.libs.apptoolkit.app.onboarding.utils.helpers

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestFinalOnboardingKonfettiState {

    @Test
    fun `state can be toggled`() {
        println("🚀 [TEST] state can be toggled")

        FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally = false
        assertThat(FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally).isFalse()

        FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally = true
        assertThat(FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally).isTrue()

        FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally = false
        assertThat(FinalOnboardingKonfettiState.hasKonfettiBeenShownGlobally).isFalse()

        println("🏁 [TEST DONE] state can be toggled")
    }
}

