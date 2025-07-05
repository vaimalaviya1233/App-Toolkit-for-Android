package com.d4rk.android.libs.apptoolkit.core.utils.extensions

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

suspend fun BillingClient.queryProductDetails(params: QueryProductDetailsParams): Result<List<ProductDetails>> =
    suspendCancellableCoroutine { continuation ->
        queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            val result = runCatching {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetailsList
                } else {
                    throw Exception("Failed to query product details: ${billingResult.debugMessage} (Response Code: ${billingResult.responseCode})")
                }
            }
            continuation.resume(result) // FIXME: Argument type mismatch: actual type is 'Result<QueryProductDetailsResult>', but 'Result<List<ProductDetails>>' was expected.
        }
    }