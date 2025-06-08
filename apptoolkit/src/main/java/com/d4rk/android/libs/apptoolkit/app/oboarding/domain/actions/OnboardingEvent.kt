package com.d4rk.android.libs.apptoolkit.app.oboarding.domain.actions

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class OnboardingEvent : UiEvent {
    data class OpenConsentForm(val activity: Activity) : OnboardingEvent()
    data class LoadConsentInfo(val activity: Activity? = null) : OnboardingEvent()
}
