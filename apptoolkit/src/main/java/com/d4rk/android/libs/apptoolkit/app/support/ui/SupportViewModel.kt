package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.billing.BillingRepository
import com.d4rk.android.libs.apptoolkit.app.support.billing.PurchaseResult
import com.d4rk.android.libs.apptoolkit.app.support.billing.SupportScreenUiState
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportAction
import com.d4rk.android.libs.apptoolkit.app.support.domain.actions.SupportEvent
import com.d4rk.android.libs.apptoolkit.app.support.utils.constants.DonationProductIds
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class SupportViewModel(
    private val billingRepository: BillingRepository,
) : ScreenViewModel<SupportScreenUiState, SupportEvent, SupportAction>(
    initialState = UiStateScreen(
        screenState = ScreenState.IsLoading(),
        data = SupportScreenUiState()
    )
) {

    init {
        billingRepository.productDetails
            .map { it.values.toList() }
            .onEach { products ->
                screenState.updateData(newState = ScreenState.Success()) { current ->
                    current.copy(error = null, products = products)
                }
            }
            .catch { e ->
                screenState.updateData(newState = ScreenState.Error()) { current ->
                    current.copy(error = e.message)
                }
                screenState.showSnackbar(
                    UiSnackbar(
                        message = UiTextHelper.DynamicString(e.message ?: ""),
                        isError = true,
                        timeStamp = System.currentTimeMillis(),
                        type = ScreenMessageType.SNACKBAR
                    )
                )
            }
            .launchIn(viewModelScope)

        billingRepository.purchaseResult
            .onEach { result ->
                when (result) {
                    PurchaseResult.Pending -> screenState.showSnackbar(
                        UiSnackbar(
                            message = UiTextHelper.StringResource(R.string.purchase_pending),
                            isError = false,
                            timeStamp = System.currentTimeMillis(),
                            type = ScreenMessageType.SNACKBAR
                        )
                    )

                    PurchaseResult.Success -> screenState.showSnackbar(
                        UiSnackbar(
                            message = UiTextHelper.StringResource(R.string.purchase_thank_you),
                            isError = false,
                            timeStamp = System.currentTimeMillis(),
                            type = ScreenMessageType.SNACKBAR
                        )
                    )

                    is PurchaseResult.Failed -> {
                        screenState.updateData(newState = ScreenState.Error()) { current ->
                            current.copy(error = result.error)
                        }
                        screenState.showSnackbar(
                            UiSnackbar(
                                message = UiTextHelper.DynamicString(result.error),
                                isError = true,
                                timeStamp = System.currentTimeMillis(),
                                type = ScreenMessageType.SNACKBAR
                            )
                        )
                    }

                    PurchaseResult.UserCancelled -> screenState.showSnackbar(
                        UiSnackbar(
                            message = UiTextHelper.StringResource(R.string.purchase_cancelled),
                            isError = false,
                            timeStamp = System.currentTimeMillis(),
                            type = ScreenMessageType.SNACKBAR
                        )
                    )
                }
            }
            .catch { e ->
                screenState.showSnackbar(
                    UiSnackbar(
                        message = UiTextHelper.DynamicString(e.message ?: ""),
                        isError = true,
                        timeStamp = System.currentTimeMillis(),
                        type = ScreenMessageType.SNACKBAR
                    )
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                billingRepository.queryProductDetails(
                    listOf(
                        DonationProductIds.LOW_DONATION,
                        DonationProductIds.NORMAL_DONATION,
                        DonationProductIds.HIGH_DONATION,
                        DonationProductIds.EXTREME_DONATION
                    )
                )
            } catch (e: Exception) {
                screenState.updateData(newState = ScreenState.Error()) { current ->
                    current.copy(error = e.message)
                }
                screenState.showSnackbar(
                    UiSnackbar(
                        message = UiTextHelper.DynamicString(e.message ?: ""),
                        isError = true,
                        timeStamp = System.currentTimeMillis(),
                        type = ScreenMessageType.SNACKBAR
                    )
                )
            }
        }
    }

    override fun onEvent(event: SupportEvent) {
        when (event) {
            is SupportEvent.QueryProductDetails -> viewModelScope.launch {
                try {
                    billingRepository.queryProductDetails(
                        listOf(
                            DonationProductIds.LOW_DONATION,
                            DonationProductIds.NORMAL_DONATION,
                            DonationProductIds.HIGH_DONATION,
                            DonationProductIds.EXTREME_DONATION
                        )
                    )
                } catch (e: Exception) {
                    screenState.updateData(newState = ScreenState.Error()) { current ->
                        current.copy(error = e.message)
                    }
                    screenState.showSnackbar(
                        UiSnackbar(
                            message = UiTextHelper.DynamicString(e.message ?: ""),
                            isError = true,
                            timeStamp = System.currentTimeMillis(),
                            type = ScreenMessageType.SNACKBAR
                        )
                    )
                }
            }

            SupportEvent.DismissSnackbar -> screenState.dismissSnackbar()
        }
    }

    fun onDonateClicked(activity: Activity, productDetails: ProductDetails) {
        billingRepository.launchPurchaseFlow(activity, productDetails)
    }
}

