package com.d4rk.android.apps.apptoolkit.app.home.domain.action

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.UiEvent

sealed class HomeEvent : UiEvent {
    data object FetchApps : HomeEvent()
}