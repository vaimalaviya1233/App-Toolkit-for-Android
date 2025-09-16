package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import com.google.android.ump.ConsentInformation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentFormHelperTest {

    @Test
    fun `showConsentFormIfRequired skips loading when consent already obtained`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.OBTAINED).apply {
            statusAfterRequest = ConsentInformation.ConsentStatus.OBTAINED
        }

        ConsentFormHelper.showConsentFormIfRequired(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.requestCount)
        assertEquals(0, sdk.loadCount)
        assertEquals(0, sdk.showCount)
    }

    @Test
    fun `showConsentFormIfRequired loads form when consent is required`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.UNKNOWN).apply {
            statusAfterRequest = ConsentInformation.ConsentStatus.REQUIRED
        }

        ConsentFormHelper.showConsentFormIfRequired(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.requestCount)
        assertEquals(1, sdk.loadCount)
        assertEquals(1, sdk.showCount)
        assertEquals(1, sdk.dismissCallbackCount)
    }

    @Test
    fun `showConsentFormIfRequired resumes when request fails`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.REQUIRED).apply {
            requestFailure = RuntimeException("network")
        }

        ConsentFormHelper.showConsentFormIfRequired(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.requestCount)
        assertEquals(0, sdk.loadCount)
        assertEquals(0, sdk.showCount)
    }

    @Test
    fun `showConsentFormIfRequired resumes when load fails`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.REQUIRED).apply {
            loadFailure = RuntimeException("load")
        }

        ConsentFormHelper.showConsentFormIfRequired(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.loadCount)
        assertEquals(0, sdk.showCount)
    }

    @Test
    fun `showConsentFormIfRequired resumes when show throws`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.REQUIRED).apply {
            showException = IllegalStateException("show")
        }

        ConsentFormHelper.showConsentFormIfRequired(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.showCount)
        assertEquals(0, sdk.dismissCallbackCount)
    }

    @Test
    fun `showConsentForm always requests and loads regardless of status`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.OBTAINED).apply {
            statusAfterRequest = ConsentInformation.ConsentStatus.OBTAINED
        }

        ConsentFormHelper.showConsentForm(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.requestCount)
        assertEquals(1, sdk.loadCount)
        assertEquals(1, sdk.showCount)
        assertEquals(1, sdk.dismissCallbackCount)
    }

    @Test
    fun `showConsentForm resumes after load exception`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.REQUIRED).apply {
            loadException = IllegalStateException("boom")
        }

        ConsentFormHelper.showConsentForm(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.loadCount)
        assertEquals(0, sdk.showCount)
    }

    @Test
    fun `showConsentForm resumes after dismiss error`() = runTest {
        val sdk = FakeConsentSdk(initialStatus = ConsentInformation.ConsentStatus.REQUIRED).apply {
            dismissError = RuntimeException("dismiss")
        }

        ConsentFormHelper.showConsentForm(activity = FakeActivity(), consentSdk = sdk)

        assertEquals(1, sdk.showCount)
        assertEquals(1, sdk.dismissCallbackCount)
    }

    private class FakeConsentSdk(
        initialStatus: ConsentInformation.ConsentStatus,
    ) : ConsentFormHelper.ConsentSdk {

        override var consentStatus: ConsentInformation.ConsentStatus = initialStatus
            private set

        var statusAfterRequest: ConsentInformation.ConsentStatus? = null
        var requestCount: Int = 0
        var loadCount: Int = 0
        var showCount: Int = 0
        var dismissCallbackCount: Int = 0

        var requestFailure: Throwable? = null
        var loadFailure: Throwable? = null
        var loadException: Throwable? = null
        var showException: Throwable? = null
        var dismissError: Throwable? = null

        override fun requestConsentInfoUpdate(
            activity: Activity,
            params: com.google.android.ump.ConsentRequestParameters,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit,
        ) {
            requestCount++
            requestFailure?.let { failure ->
                onFailure(failure)
            } ?: run {
                statusAfterRequest?.let { consentStatus = it }
                onSuccess()
            }
        }

        override fun loadConsentForm(
            activity: Activity,
            onFormLoaded: (ConsentFormHelper.ConsentFormHandle) -> Unit,
            onFailure: (Throwable) -> Unit,
        ) {
            loadCount++
            loadException?.let { throw it }
            loadFailure?.let { failure ->
                onFailure(failure)
            } ?: onFormLoaded(FakeConsentForm())
        }

        private inner class FakeConsentForm : ConsentFormHelper.ConsentFormHandle {
            override fun show(activity: Activity, onDismissed: (Throwable?) -> Unit) {
                showCount++
                showException?.let { throw it }
                dismissCallbackCount++
                onDismissed(dismissError)
            }
        }
    }

    private class FakeActivity : Activity()
}
