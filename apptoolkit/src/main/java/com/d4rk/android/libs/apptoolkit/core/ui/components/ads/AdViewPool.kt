package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlin.collections.ArrayDeque
import kotlin.concurrent.fixedRateTimer

private const val MAX_POOL_SIZE = 3
private const val TIMEOUT_MS = 5 * 60 * 1000L

private data class PooledAdView(val view: AdView, var lastUsed: Long)

object AdViewPool {
    private val pool: MutableMap<String, ArrayDeque<PooledAdView>> = mutableMapOf()

    init {
        fixedRateTimer("AdViewPoolCleanup", daemon = true, initialDelay = TIMEOUT_MS, period = TIMEOUT_MS) {
            cleanup()
        }
    }

    @Synchronized
    fun preload(
        context: Context,
        adUnitId: String,
        count: Int = 1,
        adRequest: AdRequest = AdRequest.Builder().build()
    ) {
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
        repeat(count) {
            if (deque.size < MAX_POOL_SIZE) {
                deque.add(
                    PooledAdView(
                        AdView(context).apply {
                            this.adUnitId = adUnitId
                            loadAd(adRequest)
                        },
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }

    @Synchronized
    fun acquire(
        context: Context,
        adUnitId: String,
        adRequest: AdRequest = AdRequest.Builder().build()
    ): AdView {
        val deque = pool[adUnitId]
        return if (deque != null && deque.isNotEmpty()) {
            deque.removeFirst().view
        } else {
            AdView(context).apply {
                this.adUnitId = adUnitId
                loadAd(adRequest)
            }
        }
    }

    @Synchronized
    fun release(adUnitId: String, adView: AdView) {
        cleanup()
        val deque = pool.getOrPut(adUnitId) { ArrayDeque() }
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
