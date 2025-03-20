package com.d4rk.android.libs.apptoolkit.app.privacy.routes.ads.domain.model

import com.google.android.ump.ConsentInformation

data class AdsSettingsData(
    val adsEnabled: Boolean = true,
    val consentInformation: ConsentInformation? = null
)