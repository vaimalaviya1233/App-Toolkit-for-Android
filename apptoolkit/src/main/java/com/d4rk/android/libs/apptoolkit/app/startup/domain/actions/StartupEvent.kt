package com.d4rk.android.libs.apptoolkit.app.startup.domain.actions

import android.app.Activity
import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class StartupEvent : UiEvent {
    data class OpenConsentForm(val activity : Activity) : StartupEvent()
    data object LoadConsentInfo : StartupEvent()
}