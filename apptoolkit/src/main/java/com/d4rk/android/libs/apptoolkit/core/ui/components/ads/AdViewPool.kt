package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdView

object AdViewPool {
    private val pool = mutableMapOf<String, AdView>()

    fun acquire(context: Context, adUnitId: String): AdView {
        return pool.remove(adUnitId) ?: AdView(context).apply {
            this.adUnitId = adUnitId
        }
    }

    fun release(adUnitId: String, adView: AdView) {
        pool[adUnitId] = adView
    }
}
