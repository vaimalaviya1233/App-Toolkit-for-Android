package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
                Surface(modifier = Modifier.fillMaxSize() , color = MaterialTheme.colorScheme.background) {
                    SupportComposable(viewModel = viewModel , activity = this@SupportActivity)
                }
            }
        }
    }

    fun initiatePurchase(productId : String , productDetailsMap : Map<String , ProductDetails>) {
        productDetailsMap[productId]?.let { productDetails : ProductDetails ->
            BillingRepository.getInstance(this).launchPurchaseFlow(this , productDetails)
        }
    }
}