package com.d4rk.android.libs.apptoolkit.app.support.domain.usecases

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.usecases.Repository
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.toError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class QueryProductDetailsUseCase : Repository<BillingClient , Flow<DataState<Map<String , ProductDetails> , Errors>>> {
    override suspend fun invoke(param : BillingClient) : Flow<DataState<Map<String , ProductDetails> , Errors>> = flow {
        runCatching {
            suspendCancellableCoroutine { continuation ->
                val productList = listOf(
                    "low_donation" , "normal_donation" , "high_donation" , "extreme_donation"
                ).map {
                    QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.INAPP).build()
                }

                val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

                param.queryProductDetailsAsync(params) { billingResult , productDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        productDetailsList.let {
                            val resultMap = it.associateBy { pd -> pd.productId }
                            continuation.resume(resultMap)
                        }
                    }
                    else {
                        continuation.resumeWithException(Exception("Failed to query: ${billingResult.debugMessage}"))
                    }
                }
            }
        }.onSuccess { result ->
            emit(value = DataState.Success(data = result))
        }.onFailure { error ->
            emit(value = DataState.Error(error = error.toError(default = Errors.UseCase.FAILED_TO_LOAD_SKU_DETAILS)))
        }
    }
}