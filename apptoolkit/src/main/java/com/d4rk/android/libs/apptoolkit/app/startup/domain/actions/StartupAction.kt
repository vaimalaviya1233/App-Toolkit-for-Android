package com.d4rk.android.libs.apptoolkit.app.startup.domain.actions

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.ActionEvent

sealed interface StartupAction : ActionEvent {
    data object NavigateNext : StartupAction
}
