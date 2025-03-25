package com.d4rk.android.libs.apptoolkit.app.ads.domain.actions

import com.d4rk.android.libs.apptoolkit.app.ads.ui.AdsSettingsActivity

sealed class AdsSettingsEvent {
    data object LoadAdsSettings : AdsSettingsEvent()
    data class AdsSettingChanged(val isEnabled : Boolean) : AdsSettingsEvent()
    data class OpenConsentForm(val activity : AdsSettingsActivity) : AdsSettingsEvent()
}