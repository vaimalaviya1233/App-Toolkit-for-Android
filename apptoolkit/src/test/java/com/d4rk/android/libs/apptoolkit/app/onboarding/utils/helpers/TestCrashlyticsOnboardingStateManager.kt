package com.d4rk.android.libs.apptoolkit.app.onboarding.utils.helpers

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestCrashlyticsOnboardingStateManager {

    @Test
    fun `open and dismiss dialog update state`() {
        println("🚀 [TEST] open and dismiss dialog update state")
        CrashlyticsOnboardingStateManager.openDialog()
        assertTrue(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        CrashlyticsOnboardingStateManager.dismissDialog()
        assertFalse(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        println("🏁 [TEST DONE] open and dismiss dialog update state")
    }

    @Test
    fun `repeated open and dismiss calls`() {
        println("🚀 [TEST] repeated open and dismiss calls")
        CrashlyticsOnboardingStateManager.openDialog()
        CrashlyticsOnboardingStateManager.openDialog()
        assertTrue(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        CrashlyticsOnboardingStateManager.dismissDialog()
        CrashlyticsOnboardingStateManager.dismissDialog()
        assertFalse(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        println("🏁 [TEST DONE] repeated open and dismiss calls")
    }
}
