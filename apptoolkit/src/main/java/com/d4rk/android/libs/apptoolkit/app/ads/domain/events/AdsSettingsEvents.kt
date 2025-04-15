package com.d4rk.android.libs.apptoolkit.app.ads.domain.events

import com.d4rk.android.libs.apptoolkit.app.ads.ui.AdsSettingsActivity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class AdsSettingsEvents : UiEvent {
    data object LoadAdsSettings : AdsSettingsEvents()
    data class OpenConsentForm(val activity : AdsSettingsActivity) : AdsSettingsEvents()
}