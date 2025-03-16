@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.app.support.domain.usecases

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QuerySkuDetailsUseCase {
    operator fun invoke(billingClient : BillingClient) : Flow<DataState<Map<String , SkuDetails> , Errors>> = flow {
        runCatching {
            suspendCancellableCoroutine<Map<String , SkuDetails>> { continuation ->
                val skuList = listOf("low_donation" , "normal_donation" , "high_donation" , "extreme_donation")
                val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build()
                billingClient.querySkuDetailsAsync(params) { billingResult , skuDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        val detailsMap = skuDetailsList.associateBy { it.sku }
                        continuation.resume(detailsMap)
                    }
                    else {
                        continuation.resumeWithException(Exception("Failed to query SKU details"))
                    }
                }
            }
        }.onSuccess { skuMap ->
            emit(DataState.Success(data = skuMap))
        }.onFailure { throwable ->
            emit(DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_FAQS)))
        }
    }
}