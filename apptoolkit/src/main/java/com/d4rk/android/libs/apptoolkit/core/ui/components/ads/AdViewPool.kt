package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdView
import kotlin.collections.ArrayDeque

object AdViewPool {
    private val pool: MutableMap<String, ArrayDeque<AdView>> = mutableMapOf()

    fun preload(context: Context, adUnitId: String, count: Int = 1) {
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
        repeat(count) {
            deque.add(AdView(context).apply { this.adUnitId = adUnitId })
        }
    }

    fun acquire(context: Context, adUnitId: String): AdView {
        val deque = pool[adUnitId]
        return if (deque != null && deque.isNotEmpty()) {
            deque.removeFirst()
        } else {
            AdView(context).apply { this.adUnitId = adUnitId }
        }
    }

    fun release(adUnitId: String, adView: AdView) {
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
        deque.add(adView)
    }
}
