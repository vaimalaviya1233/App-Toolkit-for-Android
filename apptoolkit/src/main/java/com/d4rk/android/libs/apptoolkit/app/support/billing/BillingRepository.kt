package com.d4rk.android.libs.apptoolkit.app.support.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class BillingRepository private constructor(
    context: Context,
    private val dispatchers: DispatcherProvider,
    externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io),
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(externalScope.coroutineContext + SupervisorJob() + dispatchers.io)

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: Flow<Map<String, ProductDetails>> =
        _productDetails.asStateFlow()

    private val _purchaseResult = MutableSharedFlow<PurchaseResult>()
    val purchaseResult: Flow<PurchaseResult> =
        _purchaseResult.asSharedFlow()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams
                .newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    companion object {
        @Volatile
        private var INSTANCE: BillingRepository? = null

        fun getInstance(
            context: Context,
            dispatchers: DispatcherProvider,
            externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io),
        ): BillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingRepository(context.applicationContext, dispatchers, externalScope)
                    .also { INSTANCE = it }
            }
        }
    }

    init {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { processPastPurchases() }
                }
            }

            override fun onBillingServiceDisconnected() {
                // handled by auto reconnection
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            scope.launch { _purchaseResult.emit(PurchaseResult.UserCancelled) }
        } else if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            scope.launch { _purchaseResult.emit(PurchaseResult.Failed(billingResult.debugMessage)) }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    if (!purchase.isAcknowledged) {
                        consumePurchase(purchase)
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    scope.launch { _purchaseResult.emit(PurchaseResult.Pending) }
                }
                else -> {}
            }
        }
    }

    private fun consumePurchase(purchase: Purchase) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.consumeAsync(params) { billingResult: BillingResult, _: String? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                scope.launch { _purchaseResult.emit(PurchaseResult.Success) }
            } else {
                scope.launch { _purchaseResult.emit(PurchaseResult.Failed(billingResult.debugMessage)) }
            }
        }
    }

    suspend fun processPastPurchases() {
        withContext(dispatchers.io) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
            suspendCancellableCoroutine<Unit> { continuation ->
                billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        handlePurchases(purchasesList)
                    }
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    suspend fun queryProductDetails(productIds: List<String>) {
        withContext(dispatchers.io) {
            val products = productIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()
            suspendCancellableCoroutine<Unit> { continuation ->
                billingClient.queryProductDetailsAsync(params) { billingResult, result ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val map = result.productDetailsList.associateBy { it.productId }
                        scope.launch { _productDetails.emit(map) }
                    } else {
                        scope.launch {
                            _purchaseResult.emit(PurchaseResult.Failed(billingResult.debugMessage))
                        }
                    }
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, details: ProductDetails) {
        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        launchPurchaseFlow(activity, details)
                    } else {
                        scope.launch { _purchaseResult.emit(PurchaseResult.Failed("Billing is unavailable")) }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // handled by auto reconnection
                }
            })
            return
        }

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            ).build()
        val billingResult = billingClient.launchBillingFlow(activity, params)
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            val result = if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                PurchaseResult.UserCancelled
            } else {
                PurchaseResult.Failed(billingResult.debugMessage)
            }
            scope.launch { _purchaseResult.emit(result) }
            return
        }
    }

    fun close() {
        scope.cancel()
        billingClient.endConnection()
    }
}

