package com.d4rk.android.apps.apptoolkit.app.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.home.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setLoading
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val fetchDeveloperAppsUseCase : FetchDeveloperAppsUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<UiHomeScreen>> = MutableStateFlow(UiStateScreen(screenState = ScreenState.IsLoading() , data = UiHomeScreen(apps = emptyList())))
    val screenState : StateFlow<UiStateScreen<UiHomeScreen>> = _screenState.asStateFlow()

    init {
        fetchDeveloperApps()
    }

    fun fetchDeveloperApps() {
        viewModelScope.launch {
            fetchDeveloperAppsUseCase().flowOn(dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result ->
                when (result) {
                    is DataState.Success -> {
                        if (result.data.isNotEmpty()) {
                            _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                                current.copy(apps = result.data)
                            }
                        }
                        else {
                            _screenState.updateData(newDataState = ScreenState.NoData()) { current -> current }
                        }
                    }

                    is DataState.Error -> {
                        _screenState.setErrors(
                            errors = listOf(
                                UiSnackbar(message = UiTextHelper.DynamicString(content = result.error.toString()))
                            )
                        )
                    }

                    is DataState.Loading -> {
                        _screenState.setLoading()
                    }

                    else -> {}
                }
            }
        }
    }
}