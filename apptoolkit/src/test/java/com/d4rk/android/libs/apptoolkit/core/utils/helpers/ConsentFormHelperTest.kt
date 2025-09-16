package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.UserMessagingPlatform
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentFormHelperTest {

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `showConsentFormIfRequired completes without loading when consent not required`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()

        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.NOT_REQUIRED
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        justRun { UserMessagingPlatform.loadConsentForm(any(), any(), any()) }

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { UserMessagingPlatform.loadConsentForm(any(), any(), any()) wasNot Called }
    }

    @Test
    fun `showConsentFormIfRequired completes when form is shown`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val consentForm = mockk<ConsentForm>()

        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.REQUIRED
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onLoaded = arg<(ConsentForm) -> Unit>(1)
            onLoaded(consentForm)
        }
        every { consentForm.show(activity, any()) } answers {
            val onDismissed = arg<() -> Unit>(1)
            onDismissed()
        }

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify(exactly = 1) { UserMessagingPlatform.loadConsentForm(activity, any(), any()) }
        verify(exactly = 1) { consentForm.show(activity, any()) }
    }

    @Test
    fun `showConsentFormIfRequired completes when load callback reports error`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val formError = mockk<ConsentInformation.FormError>()

        every { formError.message } returns "error"
        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.UNKNOWN
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onFailure = arg<(ConsentInformation.FormError) -> Unit>(2)
            onFailure(formError)
        }

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { UserMessagingPlatform.loadConsentForm(any(), any(), any()) }
        verify { Log.e(any(), any()) }
    }

    @Test
    fun `showConsentFormIfRequired completes when load throws`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()

        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.REQUIRED
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } throws IllegalStateException("boom")

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { Log.e(any(), any(), any()) }
    }

    @Test
    fun `showConsentFormIfRequired completes when show throws`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val consentForm = mockk<ConsentForm>()

        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.REQUIRED
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onLoaded = arg<(ConsentForm) -> Unit>(1)
            onLoaded(consentForm)
        }
        every { consentForm.show(activity, any()) } throws IllegalStateException("show failed")

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify(exactly = 1) { consentForm.show(activity, any()) }
        verify { Log.e(any(), any(), any()) }
    }

    @Test
    fun `showConsentFormIfRequired completes when request fails`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val formError = mockk<ConsentInformation.FormError>()

        every { consentInfo.consentStatus } returns ConsentInformation.ConsentStatus.REQUIRED
        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onFailure = arg<(ConsentInformation.FormError) -> Unit>(3)
            onFailure(formError)
        }

        mockkStatic(UserMessagingPlatform::class)
        justRun { UserMessagingPlatform.loadConsentForm(any(), any(), any()) }

        var completed = false
        try {
            ConsentFormHelper.showConsentFormIfRequired(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentFormIfRequired to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { UserMessagingPlatform.loadConsentForm(any(), any(), any()) wasNot Called }
    }

    @Test
    fun `showConsentForm completes when form is shown`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val consentForm = mockk<ConsentForm>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onLoaded = arg<(ConsentForm) -> Unit>(1)
            onLoaded(consentForm)
        }
        every { consentForm.show(activity, any()) } answers {
            val onDismissed = arg<() -> Unit>(1)
            onDismissed()
        }

        var completed = false
        try {
            ConsentFormHelper.showConsentForm(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentForm to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify(exactly = 1) { UserMessagingPlatform.loadConsentForm(activity, any(), any()) }
        verify(exactly = 1) { consentForm.show(activity, any()) }
    }

    @Test
    fun `showConsentForm completes when load callback reports error`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val formError = mockk<ConsentInformation.FormError>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onFailure = arg<(ConsentInformation.FormError) -> Unit>(2)
            onFailure(formError)
        }

        var completed = false
        try {
            ConsentFormHelper.showConsentForm(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentForm to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { UserMessagingPlatform.loadConsentForm(any(), any(), any()) }
    }

    @Test
    fun `showConsentForm completes when load throws`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } throws RuntimeException("load failure")

        var completed = false
        try {
            ConsentFormHelper.showConsentForm(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentForm to complete without exception, but was $t")
        }

        assertTrue(completed)
    }

    @Test
    fun `showConsentForm completes when show throws`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val consentForm = mockk<ConsentForm>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onSuccess = arg<() -> Unit>(2)
            onSuccess()
        }

        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.loadConsentForm(any(), any(), any()) } answers {
            val onLoaded = arg<(ConsentForm) -> Unit>(1)
            onLoaded(consentForm)
        }
        every { consentForm.show(activity, any()) } throws IllegalStateException("show failure")

        var completed = false
        try {
            ConsentFormHelper.showConsentForm(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentForm to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify(exactly = 1) { consentForm.show(activity, any()) }
    }

    @Test
    fun `showConsentForm completes when request fails`() = runTest {
        val activity = mockk<Activity>()
        val consentInfo = mockk<ConsentInformation>()
        val formError = mockk<ConsentInformation.FormError>()

        every { consentInfo.requestConsentInfoUpdate(activity, any(), any(), any()) } answers {
            val onFailure = arg<(ConsentInformation.FormError) -> Unit>(3)
            onFailure(formError)
        }

        mockkStatic(UserMessagingPlatform::class)
        justRun { UserMessagingPlatform.loadConsentForm(any(), any(), any()) }

        var completed = false
        try {
            ConsentFormHelper.showConsentForm(activity, consentInfo)
            completed = true
        } catch (t: Throwable) {
            fail("Expected showConsentForm to complete without exception, but was $t")
        }

        assertTrue(completed)
        verify { UserMessagingPlatform.loadConsentForm(any(), any(), any()) wasNot Called }
    }
}
