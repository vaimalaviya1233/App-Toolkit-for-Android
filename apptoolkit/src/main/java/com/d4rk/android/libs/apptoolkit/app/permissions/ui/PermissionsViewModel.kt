package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsAction
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.successData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper


import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PermissionsViewModel(private val settingsProvider : PermissionsProvider) : ScreenViewModel<SettingsConfig , PermissionsEvent , PermissionsAction>(initialState = UiStateScreen(data = SettingsConfig(title = "" , categories = emptyList()))) {

    override fun onEvent(event : PermissionsEvent) {
        when (event) {
            is PermissionsEvent.Load -> loadPermissions(context = event.context)
        }
    }

    private fun loadPermissions(context : Context) {
        viewModelScope.launch(context = Dispatchers.IO) {
            runCatching {
                settingsProvider.providePermissionsConfig(context = context)
            }.onSuccess { result : SettingsConfig ->
                withContext(Dispatchers.Main) {
                    if (result.categories.isNotEmpty()) {
                        screenState.successData {
                            copy(title = result.title , categories = result.categories)
                        }
                    } else {
                        screenState.setErrors(listOf(UiSnackbar(message = UiTextHelper.DynamicString("No settings found"))))
                        screenState.updateState(ScreenState.NoData())
                    }
                }
            }
        }
    }
}