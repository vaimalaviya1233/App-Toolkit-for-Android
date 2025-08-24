package com.d4rk.android.libs.apptoolkit.app.about.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.actions.AboutEvents
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.events.AboutActions
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.launch

open class AboutViewModel :
    ScreenViewModel<UiAboutScreen, AboutEvents, AboutActions>(initialState = UiStateScreen(data = UiAboutScreen())) {

    override fun onEvent(event : AboutEvents) {
        when (event) {
            is AboutEvents.CopyDeviceInfo -> copyDeviceInfo()
            is AboutEvents.DismissSnackbar -> dismissSnack()
        }
    }

    private fun copyDeviceInfo() {
        updateUi {
            copy(showDeviceInfoCopiedSnackbar = true)
        }
        viewModelScope.launch {
            screenState.showSnackbar(
                snackbar = UiSnackbar(
                    message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_copied),
                    isError = false,
                    timeStamp = System.nanoTime(),
                    type = ScreenMessageType.SNACKBAR
                )
            )
        }
    }

    private fun dismissSnack() {
        updateUi { copy(showDeviceInfoCopiedSnackbar = false) }
        viewModelScope.launch {
            screenState.dismissSnackbar()
        }
    }

    private inline fun updateUi(crossinline transform: UiAboutScreen.() -> UiAboutScreen) {
        viewModelScope.launch {
            screenState.updateData(newState = screenState.value.screenState) { current: UiAboutScreen -> transform(current) }
        }
    }
}