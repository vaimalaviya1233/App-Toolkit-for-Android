package com.d4rk.android.libs.apptoolkit.app.support.ui

import com.android.billingclient.api.BillingClient
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportAction
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportEvent
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QuerySkuDetailsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SupportViewModel(private val querySkuDetailsUseCase : QuerySkuDetailsUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<UiSupportScreen , SupportEvent , SupportAction>(initialState = UiStateScreen(data = UiSupportScreen())) {

    override fun onEvent(event : SupportEvent) {
        when (event) {
            is SupportEvent.QuerySkuDetails -> querySkuDetails(event.billingClient)
        }
    }

    private fun querySkuDetails(billingClient : BillingClient) {
        launch(dispatcherProvider.io) {
            querySkuDetailsUseCase(billingClient).stateIn(this , SharingStarted.Lazily , DataState.Loading()).collect { result ->
                screenState.applyResult(result , "Failed to load SKU details") { sku , current ->
                    current.copy(skuDetails = sku)
                }
            }
        }
    }
}