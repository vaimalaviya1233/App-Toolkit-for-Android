package com.d4rk.android.libs.apptoolkit.core.utils.extensions

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun BillingClient.queryProductDetails(
    params: QueryProductDetailsParams,
): Result<QueryProductDetailsResult> = suspendCancellableCoroutine { continuation ->
    if (!isReady) {
        continuation.resume(Result.failure(IllegalStateException("BillingClient is not ready")))
        return@suspendCancellableCoroutine
    }

    try {
        queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            val result = if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(productDetailsResult)
            } else {
                Result.failure(
                    Exception(
                        "Failed to query product details: ${billingResult.debugMessage} " +
                            "(Response Code: ${billingResult.responseCode})"
                    )
                )
            }
            continuation.resume(result)
        }
    } catch (e: Exception) {
        continuation.resume(Result.failure(e))
    }
}

