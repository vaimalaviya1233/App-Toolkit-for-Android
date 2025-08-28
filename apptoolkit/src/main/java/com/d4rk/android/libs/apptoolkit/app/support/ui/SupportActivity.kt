package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.app.support.billing.BillingRepository
import com.d4rk.android.libs.apptoolkit.app.theme.style.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class SupportActivity : AppCompatActivity() {
    private val viewModel : SupportViewModel by viewModel()

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SupportComposable(viewModel = viewModel , activity = this@SupportActivity)
            }
        }
    }

    fun initiatePurchase(productId : String , productDetailsMap : Map<String , ProductDetails>) { // FIXME: Function "initiatePurchase" is never used
        productDetailsMap[productId]?.let { productDetails : ProductDetails ->
            BillingRepository.getInstance(this).launchPurchaseFlow(this , productDetails)
        }
    }
}