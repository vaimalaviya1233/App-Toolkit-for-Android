package com.d4rk.android.libs.apptoolkit.core.utils.extensions

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun BillingClient.queryProductDetails(params: QueryProductDetailsParams): Result<QueryProductDetailsResult> =
    suspendCancellableCoroutine { continuation ->
        queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            val result = runCatching {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetailsResult
                } else {
                    throw Exception(
                        "Failed to query product details: ${billingResult.debugMessage} (Response Code: ${billingResult.responseCode})"
                    )
                }
            }
            continuation.resume(result)
        }
    }