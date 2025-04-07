package com.d4rk.android.libs.apptoolkit.app.startup.domain.actions

import com.d4rk.android.libs.apptoolkit.core.ui.base.handling.ActionEvent

sealed class StartupAction : ActionEvent {
    data object NavigateToNextScreen : StartupAction()
}