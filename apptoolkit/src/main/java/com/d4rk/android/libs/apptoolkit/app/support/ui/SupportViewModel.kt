package com.d4rk.android.libs.apptoolkit.app.support.ui

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportAction
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportEvent
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QueryProductDetailsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield

class SupportViewModel(
    private val queryProductDetailsUseCase : QueryProductDetailsUseCase,
    private val dispatcherProvider : DispatcherProvider
) : ScreenViewModel<UiSupportScreen , SupportEvent , SupportAction>(
    initialState = UiStateScreen(data = UiSupportScreen())
) {

    private var queryJob: Job? = null

    override fun onEvent(event : SupportEvent) {
        when (event) {
            is SupportEvent.QueryProductDetails -> queryProductDetails(event.billingClient)
        }
    }

    private fun queryProductDetails(billingClient : BillingClient) {
        queryJob?.cancel()
        queryJob = launch(context = dispatcherProvider.io) {
            screenState.updateState(ScreenState.IsLoading())
            // ensure observers see the loading state before work continues
            yield()

            try {
                queryProductDetailsUseCase(billingClient)
                    .flowOn(dispatcherProvider.default)
                    .collect { result : DataState<Map<String , ProductDetails> , Errors> ->
                        screenState.applyResult(
                            result = result,
                            errorMessage = UiTextHelper.StringResource(R.string.error_failed_to_load_sku_details)
                        ) { productMap , current ->
                            current.copy(productDetails = productMap)
                        }
                    }
            } catch (throwable: Throwable) {
                screenState.setErrors(listOf(UiSnackbar(message = UiTextHelper.StringResource(R.string.error_failed_to_load_sku_details))))
                screenState.updateState(ScreenState.Error())
            }
        }
    }
}