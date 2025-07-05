package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.ProductDetails
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.billing.PurchaseResult
import kotlinx.coroutines.flow.collect

@Composable
fun SupportScreen(viewModel: SupportViewModel, activity: Activity) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = rememberSnackbarHostState()

    LaunchedEffect(Unit) {
        viewModel.purchaseResult.collect { result ->
            when (result) {
                PurchaseResult.Pending -> snackbarHostState.showSnackbar(
                    message = stringResource(id = R.string.purchase_pending)
                )
                PurchaseResult.Success -> snackbarHostState.showSnackbar(
                    message = stringResource(id = R.string.purchase_thank_you)
                )
                is PurchaseResult.Failed -> snackbarHostState.showSnackbar(result.error)
                PurchaseResult.UserCancelled -> snackbarHostState.showSnackbar(
                    message = stringResource(id = R.string.purchase_cancelled)
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(text = uiState.error!!, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    items(uiState.products) { details ->
                        DonationItem(details = details) {
                            viewModel.onDonateClicked(activity, details)
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun DonationItem(details: ProductDetails, onClick: () -> Unit) {
    val price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
    Button(onClick = onClick, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = details.name)
            Text(text = price)
        }
    }
}

