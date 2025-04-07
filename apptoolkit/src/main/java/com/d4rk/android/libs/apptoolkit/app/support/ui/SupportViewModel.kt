package com.d4rk.android.libs.apptoolkit.app.support.ui

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportAction
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportEvent
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QuerySkuDetailsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@Suppress("DEPRECATION")
class SupportViewModel(private val querySkuDetailsUseCase : QuerySkuDetailsUseCase , private val dispatcherProvider : DispatcherProvider) : ScreenViewModel<UiSupportScreen , SupportEvent , SupportAction>(initialState = UiStateScreen(data = UiSupportScreen())) {

    override fun onEvent(event : SupportEvent) {
        when (event) {
            is SupportEvent.QuerySkuDetails -> querySkuDetails(billingClient = event.billingClient)
        }
    }

    private fun querySkuDetails(billingClient : BillingClient) {
        launch(context = dispatcherProvider.io) {
            querySkuDetailsUseCase(billingClient).stateIn(
                scope = this , started = SharingStarted.Lazily , initialValue = DataState.Loading()
            ).collect { result : DataState<Map<String , SkuDetails> , Errors> ->
                screenState.applyResult(result = result , errorMessage = UiTextHelper.StringResource(R.string.error_failed_to_load_sku_details)) { sku : Map<String , SkuDetails> , current : UiSupportScreen ->
                    current.copy(skuDetails = sku)
                }
            }
        }
    }
}