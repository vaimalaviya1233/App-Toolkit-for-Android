package com.d4rk.android.libs.apptoolkit.app.startup.ui

import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupAction
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.domain.model.ui.UiStartupScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel

class StartupViewModel : ScreenViewModel<UiStartupScreen , StartupEvent , StartupAction>(
    initialState = UiStateScreen(data = UiStartupScreen())
) {
    override fun onEvent(event : StartupEvent) {
        when (event) {
            StartupEvent.ConsentFormLoaded -> screenState.updateData(
                newState = ScreenState.Success()
            ) { current -> current.copy(consentFormLoaded = true) }

            StartupEvent.Continue -> sendAction(StartupAction.NavigateNext)
        }
    }
}
