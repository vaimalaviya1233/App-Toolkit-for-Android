package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewParent
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.debugNativeAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import java.lang.ref.WeakReference
import java.util.Map

internal fun ensureNativeFallbackClicks(
    headlineView: View?,
    iconView: View?,
    callToActionView: View?,
) {
    headlineView.ensureNativeFallbackClickTarget("headline")
    iconView.ensureNativeFallbackClickTarget("icon")
    callToActionView.ensureNativeFallbackClickTarget("callToAction")
}

private fun View?.ensureNativeFallbackClickTarget(label: String) {
    this ?: return
    val nativeAdView = findParentNativeAdView()
    if (nativeAdView == null) {
        debugNativeAds("Unable to locate NativeAdView parent for fallback target $label")
        return
    }

    val existingListener = getExistingOnClickListener()
    when (existingListener) {
        null -> {
            debugNativeAds("Assigning fallback click listener for $label (no existing listener)")
            setOnClickListener(NativeAdFallbackClickListener(label, nativeAdView, null))
        }

        is NativeAdFallbackClickListener -> {
            debugNativeAds("NativeAdFallbackClickListener already attached for $label - refreshing reference")
            existingListener.updateNativeAdView(nativeAdView)
        }

        else -> {
            debugNativeAds(
                "Wrapping existing click listener ${existingListener.javaClass.name} for $label with fallback"
            )
            setOnClickListener(NativeAdFallbackClickListener(label, nativeAdView, existingListener))
        }
    }
}

private fun View.findParentNativeAdView(): NativeAdView? {
    var current: ViewParent? = parent
    while (current != null && current !is NativeAdView) {
        current = current.parent
    }
    return current
}

@SuppressLint("PrivateApi")
private fun View?.getExistingOnClickListener(): View.OnClickListener? {
    this ?: return null
    return runCatching {
        val listenerInfoField = View::class.java.getDeclaredField("mListenerInfo").apply {
            isAccessible = true
        }
        val listenerInfo = listenerInfoField.get(this) ?: return null
        val listenerField = listenerInfo.javaClass.getDeclaredField("mOnClickListener").apply {
            isAccessible = true
        }
        listenerField.get(listenerInfo) as? View.OnClickListener
    }.getOrNull()
}

private class NativeAdFallbackClickListener(
    private val label: String,
    nativeAdView: NativeAdView,
    delegate: View.OnClickListener?,
) : View.OnClickListener {

    private var nativeAdViewRef: WeakReference<NativeAdView> = WeakReference(nativeAdView)
    private var isHandlingClick: Boolean = false
    private val delegateRef: WeakReference<View.OnClickListener>? = delegate?.let(::WeakReference)

    override fun onClick(view: View) {
        if (isHandlingClick) {
            debugNativeAds("NativeAdFallbackClickListener($label) skipping re-entrant click")
            return
        }

        val nativeAdView = nativeAdViewRef.get()
        if (nativeAdView == null) {
            debugNativeAds("NativeAdFallbackClickListener($label) lost NativeAdView reference")
            return
        }

        isHandlingClick = true
        try {
            val delegate = delegateRef?.get()
            debugNativeAds(
                "NativeAdFallbackClickListener($label) invoked. delegate=${delegate != null} " + "nativeAdView=${nativeAdView.hashCode()}"
            )
            runCatching { delegate?.onClick(view) }
                .onFailure { error ->
                    debugNativeAds("Delegate click for $label failed: ${error.message}")
                }
            val forwarded = nativeAdView.performClick()
            debugNativeAds(
                "NativeAdFallbackClickListener($label) forwarded to NativeAdView.performClick() -> $forwarded"
            )
        } finally {
            isHandlingClick = false
        }
    }

    fun updateNativeAdView(nativeAdView: NativeAdView) {
        nativeAdViewRef = WeakReference(nativeAdView)
    }
}

internal fun NativeAd.registerNativeAdViewCompat(
    nativeAdView: NativeAdView,
    clickableAssets: MutableMap<String, View>,
    nonClickableAssets: MutableMap<String, View>,
): Boolean {
    val methods = NativeAd::class.java.methods.filter { it.name == "registerNativeAdView" }

    val threeParamResult = methods.firstOrNull { method ->
        val params = method.parameterTypes
        params.size == 3 &&
                NativeAdView::class.java.isAssignableFrom(params[0]) &&
                Map::class.java.isAssignableFrom(params[1]) &&
                Map::class.java.isAssignableFrom(params[2])
    }?.let { method ->
        runCatching {
            debugNativeAds(
                "Invoking registerNativeAdView(nativeAdView, clickable=${clickableAssets.keys}, nonClickable=${nonClickableAssets.keys})"
            )
            method.invoke(this, nativeAdView, clickableAssets, nonClickableAssets)
            true
        }.onFailure { error ->
            debugNativeAds("registerNativeAdView with asset maps failed: ${error.message}")
        }.getOrNull()
    }

    if (threeParamResult == true) {
        return true
    }

    val singleParamResult = methods.firstOrNull { method ->
        val params = method.parameterTypes
        params.size == 1 && NativeAdView::class.java.isAssignableFrom(params[0])
    }?.let { method ->
        runCatching {
            debugNativeAds("Invoking registerNativeAdView(nativeAdView) without asset maps")
            method.invoke(this, nativeAdView)
            true
        }.onFailure { error ->
            debugNativeAds("registerNativeAdView single parameter failed: ${error.message}")
        }.getOrNull()
    }

    if (singleParamResult == true) {
        return true
    }

    debugNativeAds("registerNativeAdView method unavailable on ${javaClass.name}")
    return false
}
