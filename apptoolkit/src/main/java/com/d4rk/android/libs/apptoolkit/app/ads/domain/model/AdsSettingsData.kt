package com.d4rk.android.libs.apptoolkit.app.ads.domain.model

import com.google.android.ump.ConsentInformation

data class AdsSettingsData(
    val adsEnabled : Boolean = true , val consentInformation : ConsentInformation? = null
)