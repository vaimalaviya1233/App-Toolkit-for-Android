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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BillingRepository private constructor(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher),
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(externalScope.coroutineContext + SupervisorJob() + ioDispatcher)

    private val _productDetails = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetails: StateFlow<Map<String, ProductDetails>> = _productDetails.asStateFlow()

    private val _purchaseResult = MutableSharedFlow<PurchaseResult>()
    val purchaseResult = _purchaseResult.asSharedFlow()

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
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
            externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher),
        ): BillingRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BillingRepository(context.applicationContext, ioDispatcher, externalScope)
                    .also { INSTANCE = it }
            }
        }
    }

    init {

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    processPastPurchases()
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

    fun processPastPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchasesList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchasesList)
            }
        }
    }

    fun queryProductDetails(productIds: List<String>) {
        val products = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        billingClient.queryProductDetailsAsync(params) { billingResult, result ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = result.productDetailsList.associateBy { it.productId }
                scope.launch { _productDetails.emit(map) }
            } else {
                scope.launch { _purchaseResult.emit(PurchaseResult.Failed(billingResult.debugMessage)) }
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

