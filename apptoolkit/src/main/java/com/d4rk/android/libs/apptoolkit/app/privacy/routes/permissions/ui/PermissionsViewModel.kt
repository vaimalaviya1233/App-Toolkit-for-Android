package com.d4rk.android.libs.apptoolkit.app.privacy.routes.permissions.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.libs.apptoolkit.app.privacy.routes.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


class PermissionsViewModel(private val settingsProvider : PermissionsProvider , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<SettingsConfig>> = MutableStateFlow(UiStateScreen(screenState = ScreenState.IsLoading() , data = SettingsConfig(title = "" , categories = emptyList())))
    val screenState : StateFlow<UiStateScreen<SettingsConfig>> = _screenState.asStateFlow()

    fun loadPermissions(context : Context) {
        viewModelScope.launch {
            flowOf(settingsProvider.providePermissionsConfig(context)).flowOn(dispatcherProvider.io).collect { result ->
                when {
                    result.categories.isNotEmpty() -> {
                        _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                            current.copy(title = result.title , categories = result.categories)
                        }
                    }

                    else -> {
                        _screenState.setErrors(errors = listOf(UiSnackbar(message = UiTextHelper.DynamicString("No settings found"))))
                        _screenState.updateState(ScreenState.NoData())
                    }
                }
            }
        }
    }
}