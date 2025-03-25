@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.app.support.domain.usecases

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.Repository
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QuerySkuDetailsUseCase : Repository<BillingClient , Flow<DataState<Map<String , SkuDetails> , Errors>>> {
    override suspend fun invoke(param : BillingClient) : Flow<DataState<Map<String , SkuDetails> , Errors>> = flow {
        runCatching {
            suspendCancellableCoroutine { continuation ->
                val skuList : List<String> = listOf("low_donation" , "normal_donation" , "high_donation" , "extreme_donation")
                val params : SkuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build()

                param.querySkuDetailsAsync(params) { billingResult : BillingResult , skuDetailsList : MutableList<SkuDetails>? ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        skuDetailsList?.let {
                            val detailsMap : Map<String , SkuDetails> = it.associateBy { skuId -> skuId.sku }
                            continuation.resume(value = detailsMap)
                        } ?: run {
                            continuation.resumeWithException(exception = Exception("SkuDetailsList was null"))
                        }
                    }
                    else {
                        continuation.resumeWithException(exception = Exception("Failed to query SKU details: ${billingResult.debugMessage}"))
                    }
                }
            }
        }.onSuccess { skuMap : Map<String , SkuDetails> ->
            emit(value = DataState.Success(data = skuMap))
        }.onFailure { throwable : Throwable ->
            emit(value = DataState.Error(error = throwable.toError(default = Errors.UseCase.FAILED_TO_LOAD_SKU_DETAILS)))
        }
    }
}