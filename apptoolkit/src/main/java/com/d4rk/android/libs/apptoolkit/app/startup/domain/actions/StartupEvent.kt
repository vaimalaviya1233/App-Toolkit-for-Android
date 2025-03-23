package com.d4rk.android.libs.apptoolkit.app.startup.domain.actions

sealed class StartupEvent {
    data object OpenConsentForm : StartupEvent()
}