package com.d4rk.android.libs.apptoolkit.app.about.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.actions.AboutAction
import com.d4rk.android.libs.apptoolkit.app.about.domain.actions.AboutEvent
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.about.domain.repository.AboutRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

open class AboutViewModel(
    private val repository: AboutRepository,
) :
    ScreenViewModel<UiAboutScreen, AboutEvent, AboutAction>(initialState = UiStateScreen(data = UiAboutScreen())) {

    init {
        loadAboutInfo()
    }

    override fun onEvent(event: AboutEvent) {
        when (event) {
            is AboutEvent.CopyDeviceInfo -> copyDeviceInfo(event.label)
            is AboutEvent.DismissSnackbar -> dismissSnack()
        }
    }

    private fun loadAboutInfo() {
        repository.getAboutInfoStream()
            .onEach { info ->
                screenState.successData { info }
            }
            .catch { error ->
                if (error is CancellationException) {
                    throw error
                }
                screenState.showSnackbar(
                    snackbar = UiSnackbar(
                        message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_failed),
                        isError = true,
                        timeStamp = System.nanoTime(),
                        type = ScreenMessageType.SNACKBAR,
                    ),
                )
            }
            .launchIn(viewModelScope)
    }

    private fun copyDeviceInfo(label: String) {
        screenData?.let { data ->
            viewModelScope.launch {
                try {
                    repository.copyDeviceInfo(label = label, deviceInfo = data.deviceInfo)
                    screenState.showSnackbar(
                        snackbar = UiSnackbar(
                            message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_copied),
                            isError = false,
                            timeStamp = System.nanoTime(),
                            type = ScreenMessageType.SNACKBAR,
                        ),
                    )
                } catch (error: Throwable) {
                    if (error is CancellationException) {
                        throw error
                    }
                    screenState.showSnackbar(
                        snackbar = UiSnackbar(
                            message = UiTextHelper.StringResource(resourceId = R.string.snack_device_info_failed),
                            isError = true,
                            timeStamp = System.nanoTime(),
                            type = ScreenMessageType.SNACKBAR,
                        ),
                    )
                }
            }
        }
    }

    private fun dismissSnack() {
        screenState.dismissSnackbar()
    }
}
