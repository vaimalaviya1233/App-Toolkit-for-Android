package com.d4rk.android.libs.apptoolkit.app.startup.domain.actions

import com.d4rk.android.libs.apptoolkit.app.startup.ui.StartupActivity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class StartupEvent : UiEvent {
    data class OpenConsentForm(val activity : StartupActivity) : StartupEvent()
    data object LoadConsentInfo : StartupEvent()
}