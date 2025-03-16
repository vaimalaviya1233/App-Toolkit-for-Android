package com.d4rk.android.libs.apptoolkit.app.support.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QuerySkuDetailsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupportViewModel(private val querySkuDetailsUseCase : QuerySkuDetailsUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<UiSupportScreen>> = MutableStateFlow(value = UiStateScreen(screenState = ScreenState.IsLoading() , data = UiSupportScreen()))
    val screenState : StateFlow<UiStateScreen<UiSupportScreen>> = _screenState.asStateFlow()

    fun querySkuDetails(billingClient : BillingClient) {
        viewModelScope.launch {
            querySkuDetailsUseCase(billingClient).flowOn(context = dispatcherProvider.io).stateIn(scope = viewModelScope , started = SharingStarted.Lazily , initialValue = DataState.Loading()).collect { result ->
                when (result) {
                    is DataState.Success -> {
                        _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                            current.copy(skuDetails = result.data)
                        }
                    }

                    is DataState.Error -> {
                        _screenState.updateState(newValues = ScreenState.Error())
                    }

                    is DataState.Loading -> {
                        _screenState.updateState(newValues = ScreenState.IsLoading())
                    }

                    else -> {}
                }
            }
        }
    }
}