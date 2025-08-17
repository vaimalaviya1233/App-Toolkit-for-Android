package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlin.collections.ArrayDeque

object AdViewPool {
    private val pool: MutableMap<String, ArrayDeque<AdView>> = mutableMapOf()

    fun preload(context: Context, adUnitId: String, adRequest: AdRequest, count: Int = 1) {
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
        repeat(count) {
            deque.add(
                AdView(context).apply {
                    this.adUnitId = adUnitId
                    loadAd(adRequest)
                }
            )
        }
    }

    fun acquire(context: Context, adUnitId: String): AdView {
        val deque = pool[adUnitId]
        if (deque != null && deque.isNotEmpty()) {
            val iterator = deque.iterator()
            while (iterator.hasNext()) {
                val adView = iterator.next()
                if (adView.responseInfo != null) {
                    iterator.remove()
                    return adView
                }
            }
            return deque.removeFirst()
        }
        return AdView(context).apply { this.adUnitId = adUnitId }
    }

    fun release(adUnitId: String, adView: AdView) {
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
        deque.add(adView)
    }
}
