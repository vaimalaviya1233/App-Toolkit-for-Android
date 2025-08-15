package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import io.mockk.*
import kotlin.test.assertTrue
import org.junit.Test

class TestConsentFormHelper {
    @Test
    fun `showConsentForm handles OutOfMemoryError from loadConsentForm`() {
        println("🚀 [TEST] showConsentForm handles OutOfMemoryError from loadConsentForm")
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } throws OutOfMemoryError("oom")

        var called = false
        ConsentFormHelper.showConsentForm(activity, consentInfo) { called = true }

        assertTrue(called)
        println("🏁 [TEST DONE] showConsentForm handles OutOfMemoryError from loadConsentForm")
    }
}
