package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.collections.ArrayDeque
import kotlin.concurrent.fixedRateTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_POOL_SIZE = 3
private const val TIMEOUT_MS = 5 * 60 * 1000L

private data class PooledAdView(val view: AdView, var lastUsed: Long)

object AdViewPool {
    private val pool: MutableMap<Pair<String, AdSize>, ArrayDeque<PooledAdView>> = mutableMapOf()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        fixedRateTimer("AdViewPoolCleanup", daemon = true, initialDelay = TIMEOUT_MS, period = TIMEOUT_MS) {
            cleanup()
        }
    }

    fun preload(
        context: Context,
        adUnitId: String,
        adSize: AdSize,
        adRequest: AdRequest,
        count: Int = 1,
    ) {
        val key = adUnitId to adSize
        repeat(count) {
            scope.launch {
                val view = try {
                    withContext(Dispatchers.Main) {
                        AdView(context.applicationContext).apply {
                            this.adUnitId = adUnitId
                            setAdSize(adSize)
                            loadAd(adRequest)
                        }
                    }
                } catch (_: Exception) {
                    null
                }
                if (view != null) {
                    synchronized(this@AdViewPool) {
                        val deque = pool.getOrPut(key) { ArrayDeque() }
                        if (deque.size < MAX_POOL_SIZE) {
                            deque.add(PooledAdView(view, System.currentTimeMillis()))
                        } else {
                            view.destroy()
                        }
                    }
                }
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
                    pooled.view.resume()
                    return pooled.view
                }
            }
        }
        return runCatching {
            AdView(context.applicationContext).apply {
                this.adUnitId = adUnitId
                setAdSize(adSize)
            }
        }.getOrElse {
            AdView(context.applicationContext)
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
            adView.pause()
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
