package com.d4rk.android.libs.apptoolkit.app.about.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.actions.AboutEvents
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.events.AboutActions
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class AboutViewModel(
    private val deviceProvider: AboutSettingsProvider,
    private val configProvider: BuildInfoProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) :
    ScreenViewModel<UiAboutScreen, AboutEvents, AboutActions>(initialState = UiStateScreen(data = UiAboutScreen())) {

    init {
        loadAboutInfo()
    }

    override fun onEvent(event: AboutEvents) {
        when (event) {
            is AboutEvents.CopyDeviceInfo -> copyDeviceInfo()
            is AboutEvents.DismissSnackbar -> dismissSnack()
        }
    }

    private fun loadAboutInfo() {
        viewModelScope.launch {
            val info = withContext(dispatcher) {
                UiAboutScreen(
                    appVersion = configProvider.appVersion,
                    appVersionCode = configProvider.appVersionCode,
                    deviceInfo = deviceProvider.deviceInfo,
                )
            }
            screenState.successData { info }
        }
    }

    private fun copyDeviceInfo() {
        screenState.showSnackbar(
            snackbar = UiSnackbar(
                message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_copied),
                isError = false,
                timeStamp = System.nanoTime(),
                type = ScreenMessageType.SNACKBAR,
            ),
        )
    }

    private fun dismissSnack() {
        screenState.dismissSnackbar()
    }
}
