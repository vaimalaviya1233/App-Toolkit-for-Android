@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.app.support.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QuerySkuDetailsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class SupportViewModel(private val querySkuDetailsUseCase : QuerySkuDetailsUseCase , private val dispatcherProvider : DispatcherProvider) : ViewModel() {

    private val _screenState : MutableStateFlow<UiStateScreen<UiSupportScreen>> = MutableStateFlow(UiStateScreen(screenState = ScreenState.IsLoading() , data = UiSupportScreen()))
    val screenState : StateFlow<UiStateScreen<UiSupportScreen>> = _screenState.asStateFlow()

    fun querySkuDetails(billingClient : BillingClient) {
        viewModelScope.launch(dispatcherProvider.io) {
            querySkuDetailsUseCase(billingClient).flowOn(dispatcherProvider.io).collect { result : DataState<Map<String , SkuDetails> , Errors> ->
                        when (result) {
                            is DataState.Success -> _screenState.updateData(newDataState = ScreenState.Success()) { current ->
                                current.copy(skuDetails = result.data)
                            }

                            is DataState.Error -> _screenState.updateState(ScreenState.Error())
                            else -> Unit
                        }
                    }
        }
    }
}
