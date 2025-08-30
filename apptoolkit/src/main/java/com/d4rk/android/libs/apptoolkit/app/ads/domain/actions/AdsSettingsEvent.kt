package com.d4rk.android.libs.apptoolkit.app.ads.domain.actions

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

/** User interactions on the ads settings screen. */
sealed interface AdsSettingsEvent : UiEvent {
    data class SetAdsEnabled(val enabled: Boolean) : AdsSettingsEvent
}

