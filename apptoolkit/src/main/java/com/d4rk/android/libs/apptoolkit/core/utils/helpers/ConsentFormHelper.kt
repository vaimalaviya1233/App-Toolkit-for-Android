package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.ConsentForm
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Helper functions for showing the Google UMP consent dialog.
 *
 * These methods expose a suspend API so callers can work with coroutines and
 * remain on the main thread while the underlying UMP SDK performs network and
 * I/O work on background threads.
 */
object ConsentFormHelper {

    internal fun interface ConsentFormHandle {
        fun show(activity: Activity, onDismissed: (Throwable?) -> Unit)
    }

    internal interface ConsentSdk {
        val consentStatus: ConsentInformation.ConsentStatus

        fun requestConsentInfoUpdate(
            activity: Activity,
            params: ConsentRequestParameters,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit,
        )

        fun loadConsentForm(
            activity: Activity,
            onFormLoaded: (ConsentFormHandle) -> Unit,
            onFailure: (Throwable) -> Unit,
        )
    }

    private class RealConsentSdk(
        private val consentInfo: ConsentInformation,
    ) : ConsentSdk {

        override val consentStatus: ConsentInformation.ConsentStatus
            get() = consentInfo.consentStatus

        override fun requestConsentInfoUpdate(
            activity: Activity,
            params: ConsentRequestParameters,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit,
        ) {
            consentInfo.requestConsentInfoUpdate(activity, params, { onSuccess() }, onFailure)
        }

        override fun loadConsentForm(
            activity: Activity,
            onFormLoaded: (ConsentFormHandle) -> Unit,
            onFailure: (Throwable) -> Unit,
        ) {
            UserMessagingPlatform.loadConsentForm(activity, { consentForm: ConsentForm ->
                onFormLoaded(RealConsentForm(consentForm))
            }, onFailure)
        }
    }

    private class RealConsentForm(
        private val consentForm: ConsentForm,
    ) : ConsentFormHandle {

        override fun show(activity: Activity, onDismissed: (Throwable?) -> Unit) {
            consentForm.show(activity) { error: FormError? ->
                onDismissed(error)
            }
        }
    }

    /**
     * Request consent information and display the consent form if required.
     * The function returns once the form has been displayed or it has been
     * determined that showing the form isn't necessary.
     */
    suspend fun showConsentFormIfRequired(
        activity: Activity,
        consentInfo: ConsentInformation,
    ) {
        showConsentFormIfRequired(activity = activity, consentSdk = RealConsentSdk(consentInfo))
    }

    internal suspend fun showConsentFormIfRequired(
        activity: Activity,
        consentSdk: ConsentSdk,
    ) {
        requestAndMaybeShow(activity = activity, consentSdk = consentSdk, checkStatus = true)
    }

    /**
     * Always show the consent form regardless of the current consent status.
     * The function returns once the form has been displayed (or if loading the
     * form failed).
     */
    suspend fun showConsentForm(
        activity: Activity,
        consentInfo: ConsentInformation,
    ) {
        showConsentForm(activity = activity, consentSdk = RealConsentSdk(consentInfo))
    }

    internal suspend fun showConsentForm(
        activity: Activity,
        consentSdk: ConsentSdk,
    ) {
        requestAndMaybeShow(activity = activity, consentSdk = consentSdk, checkStatus = false)
    }

    private suspend fun requestAndMaybeShow(
        activity: Activity,
        consentSdk: ConsentSdk,
        checkStatus: Boolean,
    ) {
        val params: ConsentRequestParameters =
            ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        suspendCancellableCoroutine { continuation ->
            consentSdk.requestConsentInfoUpdate(activity, params, {
                if (checkStatus &&
                    consentSdk.consentStatus != ConsentInformation.ConsentStatus.REQUIRED &&
                    consentSdk.consentStatus != ConsentInformation.ConsentStatus.UNKNOWN) {
                    if (continuation.isActive) continuation.resume(Unit)
                    return@requestConsentInfoUpdate
                }

                loadAndShow(activity, consentSdk, continuation)
            }, {
                if (continuation.isActive) continuation.resume(Unit)
            })
        }
    }

    private fun loadAndShow(
        activity: Activity,
        consentSdk: ConsentSdk,
        continuation: CancellableContinuation<Unit>,
    ) {
        runCatching {
            consentSdk.loadConsentForm(activity, { consentForm: ConsentFormHandle ->
                runCatching {
                    consentForm.show(activity) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }.onFailure {
                    Log.e("ConsentFormHelper", "Failed to load consent form", it)
                    if (continuation.isActive) continuation.resume(Unit)
                }
            }, { error ->
                Log.e("ConsentFormHelper", "Failed to load consent form: ${error.message}")
                if (continuation.isActive) continuation.resume(Unit)
            })
        }.onFailure {
            Log.e("ConsentFormHelper", "Failed to load consent form", it)
            if (continuation.isActive) continuation.resume(Unit)
        }
    }
}

