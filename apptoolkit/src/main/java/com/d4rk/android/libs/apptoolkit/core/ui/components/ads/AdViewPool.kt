package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.collections.ArrayDeque
import kotlin.concurrent.fixedRateTimer

private const val MAX_POOL_SIZE = 3
private const val TIMEOUT_MS = 5 * 60 * 1000L

private data class PooledAdView(val view: AdView, var lastUsed: Long)

object AdViewPool {
    private val pool: MutableMap<Pair<String, AdSize>, ArrayDeque<PooledAdView>> = mutableMapOf()

    init {
        fixedRateTimer("AdViewPoolCleanup", daemon = true, initialDelay = TIMEOUT_MS, period = TIMEOUT_MS) {
            cleanup()
        }
    }

    @Synchronized
    fun preload(
        context: Context,
        adUnitId: String,
        adSize: AdSize,
        adRequest: AdRequest,
        count: Int = 1,
    ) {
        val key = adUnitId to adSize
        val deque = pool.getOrPut(key) { ArrayDeque() }
        repeat(count) {
            if (deque.size < MAX_POOL_SIZE) {
                val view = AdView(context).apply {
                    this.adUnitId = adUnitId
                    setAdSize(adSize)
                    loadAd(adRequest)
                }
                deque.add(PooledAdView(view, System.currentTimeMillis()))
            }
        }
    }

    @Synchronized
    fun acquire(context: Context, adUnitId: String, adSize: AdSize): AdView {
        val key = adUnitId to adSize
        val deque = pool[key]
        if (deque != null && deque.isNotEmpty()) {
            val iterator = deque.iterator()
            while (iterator.hasNext()) {
                val pooled = iterator.next()
                if (pooled.view.responseInfo != null) {
                    iterator.remove()
                    return pooled.view
                }
            }
        }
        return AdView(context).apply {
            this.adUnitId = adUnitId
            setAdSize(adSize)
        }
    }

    @Synchronized
    fun release(adUnitId: String, adSize: AdSize, adView: AdView) {
        cleanup()
        val key = adUnitId to adSize
        val deque = pool.getOrPut(key) { ArrayDeque() }
        if (deque.size >= MAX_POOL_SIZE) {
            adView.destroy()
        } else {
            deque.add(PooledAdView(adView, System.currentTimeMillis()))
        }
    }

    @Synchronized
    private fun cleanup() {
        val now = System.currentTimeMillis()
        val iterator = pool.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val deque = entry.value
            val viewIterator = deque.iterator()
            while (viewIterator.hasNext()) {
                val pooled = viewIterator.next()
                if (now - pooled.lastUsed > TIMEOUT_MS) {
                    pooled.view.destroy()
                    viewIterator.remove()
                }
            }
            if (deque.isEmpty()) {
                iterator.remove()
            }
        }
    }
}
