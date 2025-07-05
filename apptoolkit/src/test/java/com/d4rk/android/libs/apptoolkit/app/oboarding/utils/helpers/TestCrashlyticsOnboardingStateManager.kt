package com.d4rk.android.libs.apptoolkit.app.oboarding.utils.helpers

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestCrashlyticsOnboardingStateManager {

    @Test
    fun `open and dismiss dialog update state`() {
        CrashlyticsOnboardingStateManager.openDialog()
        assertTrue(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        CrashlyticsOnboardingStateManager.dismissDialog()
        assertFalse(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
    }

    @Test
    fun `repeated open and dismiss calls`() {
        CrashlyticsOnboardingStateManager.openDialog()
        CrashlyticsOnboardingStateManager.openDialog()
        assertTrue(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
        CrashlyticsOnboardingStateManager.dismissDialog()
        CrashlyticsOnboardingStateManager.dismissDialog()
        assertFalse(CrashlyticsOnboardingStateManager.showCrashlyticsDialog)
    }
}
