package com.d4rk.android.libs.apptoolkit.app.ads.domain.actions

import com.d4rk.android.libs.apptoolkit.app.ads.ui.AdsSettingsActivity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class AdsSettingsEvent : UiEvent {
    data object LoadAdsSettings : AdsSettingsEvent()
    data class AdsSettingChanged(val isEnabled : Boolean) : AdsSettingsEvent()
    data class OpenConsentForm(val activity : AdsSettingsActivity) : AdsSettingsEvent()
}