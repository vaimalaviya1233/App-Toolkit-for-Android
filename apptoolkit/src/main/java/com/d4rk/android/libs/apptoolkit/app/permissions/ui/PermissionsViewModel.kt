package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.permissions.data.PermissionsRepository
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsAction
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


/** ViewModel responsible for exposing the permissions configuration to the UI layer. */
class PermissionsViewModel(
    private val permissionsRepository: PermissionsRepository,
) :
    ScreenViewModel<SettingsConfig, PermissionsEvent, PermissionsAction>(
        initialState = UiStateScreen(data = SettingsConfig(title = "", categories = emptyList()))
    ) {

    override fun onEvent(event : PermissionsEvent) {
        when (event) {
            PermissionsEvent.Load -> loadPermissions()
        }
    }

    private fun loadPermissions() {
        viewModelScope.launch {
            permissionsRepository.getPermissionsConfig()
                .catch { error ->
                    screenState.setErrors(
                        listOf(
                            UiSnackbar(message = UiTextHelper.DynamicString(error.message ?: "Something went wrong"))
                        )
                    )
                    screenState.updateState(ScreenState.Error())
                }
                .collect { result: SettingsConfig ->
                    if (result.categories.isNotEmpty()) {
                        screenState.successData {
                            copy(title = result.title, categories = result.categories)
                        }
                    } else {
                        screenState.setErrors(
                            listOf(
                                UiSnackbar(message = UiTextHelper.DynamicString("No settings found"))
                            )
                        )
                        screenState.updateState(ScreenState.NoData())
                    }
                }
        }
    }
}
