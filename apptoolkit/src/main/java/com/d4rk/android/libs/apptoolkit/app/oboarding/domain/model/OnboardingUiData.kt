package com.d4rk.android.libs.apptoolkit.app.oboarding.domain.model

import com.google.android.ump.ConsentInformation

/**
 * Holds UI related data for the onboarding flow.
 */
data class OnboardingUiData(
    val consentRequired: Boolean = false,
    val consentFormLoaded: Boolean = false,
    val consentInformation: ConsentInformation? = null
)
