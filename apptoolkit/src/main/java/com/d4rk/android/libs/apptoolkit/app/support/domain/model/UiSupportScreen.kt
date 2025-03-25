@file:Suppress("DEPRECATION")

package com.d4rk.android.libs.apptoolkit.app.support.domain.model

import com.android.billingclient.api.SkuDetails

data class UiSupportScreen(
    val skuDetails : Map<String , SkuDetails> = emptyMap()
)