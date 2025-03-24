package com.d4rk.android.libs.apptoolkit.app.startup.domain.model

import com.google.android.ump.ConsentInformation

data class StartupUiData(
    val consentRequired: Boolean = false,
    val consentFormLoaded: Boolean = false,
    val consentInformation: ConsentInformation? = null
)