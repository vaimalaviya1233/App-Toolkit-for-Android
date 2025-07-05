package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.app.support.billing.BillingRepository
import com.d4rk.android.libs.apptoolkit.app.support.billing.PurchaseResult
import com.d4rk.android.libs.apptoolkit.app.support.billing.SupportScreenUiState
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

class SupportViewModel(
    private val billingRepository: BillingRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    val purchaseResult = billingRepository.purchaseResult

    private val _uiState = MutableStateFlow(SupportScreenUiState(isLoading = true))
    val uiState: StateFlow<SupportScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(dispatcherProvider.io) {
            billingRepository.productDetails.collectLatest { map ->
                _uiState.update { current ->
                    current.copy(isLoading = false, error = null, products = map.values.toList())
                }
            }
        }

        viewModelScope.launch {
            billingRepository.purchaseResult.collectLatest { result ->
                if (result is PurchaseResult.Failed) {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }

        viewModelScope.launch(dispatcherProvider.io) {
            billingRepository.queryProductDetails(
                listOf(
                    "low_donation",
                    "normal_donation",
                    "high_donation",
                    "extreme_donation"
                )
            )
        }
    }

    fun onDonateClicked(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchPurchaseFlow(activity, productDetails)
    }
}

