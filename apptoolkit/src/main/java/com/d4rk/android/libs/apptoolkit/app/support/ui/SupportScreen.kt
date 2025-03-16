@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.app.support.ui

import android.content.Context
import android.view.SoundEffectConstants
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.support.domain.model.UiSupportScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.LoadingScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.NoDataScreen
import com.d4rk.android.libs.apptoolkit.core.ui.components.layouts.ScreenStateHandler
import com.d4rk.android.libs.apptoolkit.core.ui.components.modifiers.bounceClick
import com.d4rk.android.libs.apptoolkit.core.ui.components.navigation.LargeTopAppBarWithScaffold
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ButtonIconSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportComposable(viewModel : SupportViewModel , activity : SupportActivity) {
    val screenState by viewModel.screenState.collectAsState()

    LargeTopAppBarWithScaffold(
        title = stringResource(id = R.string.support_us) , onBackClicked = { activity.finish() }) { paddingValues ->
        ScreenStateHandler(
            screenState = screenState ,
            onLoading = { LoadingScreen() } ,
            onEmpty = { NoDataScreen() } ,
            onSuccess = { supportData ->
                SupportScreenContent(
                    paddingValues = paddingValues , activity = activity , supportData = supportData , viewModel = viewModel
                )
            } ,
        )
    }
}

@Composable
fun SupportScreenContent(paddingValues : PaddingValues , activity : SupportActivity , supportData : UiSupportScreen , viewModel : SupportViewModel) {
    val context : Context = LocalContext.current
    val view : View = LocalView.current
    val billingClient : BillingClient = rememberBillingClient(context , viewModel)

    Box(
        modifier = Modifier
                .padding(paddingValues)
                .fillMaxHeight()
    ) {
        LazyColumn {
            item {
                Text(
                    text = stringResource(id = R.string.paid_support) ,
                    modifier = Modifier.padding(start = SizeConstants.LargeSize , top = SizeConstants.LargeSize) ,
                    style = MaterialTheme.typography.titleLarge ,
                )
            }
            item {
                OutlinedCard(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = SizeConstants.LargeSize)
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.summary_donations) , modifier = Modifier.padding(16.dp)
                        )
                        LazyRow(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SizeConstants.LargeSize) , horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            item {
                                FilledTonalButton(
                                    modifier = Modifier
                                            .fillMaxWidth()
                                            .bounceClick() , onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        activity.initiatePurchase(
                                            sku = "low_donation" ,
                                            skuDetailsMap = supportData.skuDetails , billingClient = billingClient
                                        )
                                    }) {
                                    Icon(
                                        Icons.Outlined.Paid , contentDescription = null , modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    ButtonIconSpacer()
                                    Text(
                                        text = supportData.skuDetails["low_donation"]?.price ?: ""
                                    )
                                }
                            }
                            item {
                                FilledTonalButton(
                                    modifier = Modifier
                                            .fillMaxWidth()
                                            .bounceClick() , onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        activity.initiatePurchase(
                                            sku = "normal_donation" , skuDetailsMap = supportData.skuDetails , billingClient = billingClient
                                        )
                                    }) {
                                    Icon(
                                        Icons.Outlined.Paid , contentDescription = null , modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    ButtonIconSpacer()
                                    Text(
                                        text = supportData.skuDetails["normal_donation"]?.price ?: ""
                                    )
                                }
                            }
                        }
                        LazyRow(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp) , horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            item {
                                FilledTonalButton(
                                    modifier = Modifier
                                            .fillMaxWidth()
                                            .bounceClick() , onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        activity.initiatePurchase(
                                            sku = "high_donation" , skuDetailsMap = supportData.skuDetails , billingClient = billingClient
                                        )
                                    }) {
                                    Icon(
                                        Icons.Outlined.Paid , contentDescription = null , modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    ButtonIconSpacer()
                                    Text(
                                        text = supportData.skuDetails["high_donation"]?.price ?: ""
                                    )
                                }
                            }
                            item {
                                FilledTonalButton(
                                    modifier = Modifier
                                            .fillMaxWidth()
                                            .bounceClick() , onClick = {
                                        view.playSoundEffect(SoundEffectConstants.CLICK)
                                        activity.initiatePurchase(
                                            sku = "extreme_donation" , skuDetailsMap = supportData.skuDetails , billingClient = billingClient
                                        )
                                    }) {
                                    Icon(
                                        Icons.Outlined.Paid , contentDescription = null , modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                    ButtonIconSpacer()
                                    Text(
                                        text = supportData.skuDetails["extreme_donation"]?.price ?: ""
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    text = stringResource(id = R.string.non_paid_support) ,
                    modifier = Modifier.padding(start = 16.dp) ,
                    style = MaterialTheme.typography.titleLarge ,
                )
            }
            item {
                FilledTonalButton(onClick = {
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    IntentsHelper.openUrl(
                        context = context , url = "https://direct-link.net/548212/agOqI7123501341"
                    )
                } , modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick()
                        .padding(16.dp)) {
                    Icon(
                        Icons.Outlined.Paid , contentDescription = null , modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    ButtonIconSpacer()
                    Text(text = stringResource(id = R.string.web_ad))
                }
            }
            item {
                //AdBanner(modifier = Modifier.padding(bottom = 12.dp), adSize = AdSize.LARGE_BANNER)
            }
        }
    }
}

@Composable
fun rememberBillingClient(context : Context , viewModel : SupportViewModel) : BillingClient {
    val billingClient : BillingClient = remember {
        BillingClient.newBuilder(context).setListener { _ , _ -> }.enablePendingPurchases().build()
    }

    DisposableEffect(billingClient) {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult : BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    viewModel.querySkuDetails(billingClient)
                }
            }

            override fun onBillingServiceDisconnected() {}
        })

        onDispose {
            billingClient.endConnection()
        }
    }
    return billingClient
}