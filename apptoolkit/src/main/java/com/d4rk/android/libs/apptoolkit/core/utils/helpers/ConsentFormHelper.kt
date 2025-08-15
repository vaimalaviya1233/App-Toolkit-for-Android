package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentFormHelper {

    fun showConsentFormIfRequired(
        activity : Activity ,
        consentInfo : ConsentInformation ,
        onFormShown : () -> Unit = {}
    ) {
        val params : ConsentRequestParameters =
            ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInfo.requestConsentInfoUpdate(activity , params , {
            runCatching {
                UserMessagingPlatform.loadConsentForm(activity , { consentForm : ConsentForm ->
                    if (consentInfo.consentStatus == ConsentInformation.ConsentStatus.REQUIRED || consentInfo.consentStatus == ConsentInformation.ConsentStatus.UNKNOWN) {
                        runCatching {
                            consentForm.show(activity) { onFormShown() }
                        }.onFailure {
                            Log.e("ConsentFormHelper", "Failed to load consent form", it)
                            onFormShown()
                        }
                    }
                } , { t ->
                    Log.e("ConsentFormHelper", "Failed to load consent form", t)
                    onFormShown()
                })
            }.onFailure {
                Log.e("ConsentFormHelper", "Failed to load consent form", it)
                onFormShown()
            }
        } , {})
    }

    fun showConsentForm(
        activity : Activity ,
        consentInfo : ConsentInformation ,
        onFormShown : () -> Unit = {}
    ) {
        val params : ConsentRequestParameters =
            ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()

        consentInfo.requestConsentInfoUpdate(activity , params , {
            runCatching {
                UserMessagingPlatform.loadConsentForm(activity , { consentForm : ConsentForm ->
                    runCatching {
                        consentForm.show(activity) { onFormShown() }
                    }.onFailure {
                        Log.e("ConsentFormHelper", "Failed to load consent form", it)
                        onFormShown()
                    }
                } , { t ->
                    Log.e("ConsentFormHelper", "Failed to load consent form", t)
                    onFormShown()
                })
            }.onFailure {
                Log.e("ConsentFormHelper", "Failed to load consent form", it)
                onFormShown()
            }
        } , {})
    }
}
