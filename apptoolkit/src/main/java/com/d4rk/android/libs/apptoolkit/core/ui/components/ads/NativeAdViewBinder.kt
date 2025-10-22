package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ADCHOICES_CONTAINER_VIEW
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ADVERTISER
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_CALL_TO_ACTION
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_HEADLINE
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ICON
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.lang.ref.WeakReference
import java.util.Map

/**
 * Helper responsible for inflating a [NativeAdView] layout and binding the provided [NativeAd]
 * instance to it.
 */
object NativeAdViewBinder {

    /**
     * Inflates the given [layoutRes] into the supplied [container] and binds [nativeAd] to the
     * resulting [NativeAdView]. Existing children inside [container] are removed prior to binding.
     */
    fun bind(
        context: Context,
        container: ViewGroup,
        @LayoutRes layoutRes: Int,
        nativeAd: NativeAd,
    ) {
        debugNativeAds(
            "NativeAdViewBinder.bind layout=$layoutRes container=${container::class.simpleName} nativeAd=${nativeAd.hashCode()}"
        )
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(layoutRes, container, false)
        val nativeAdView = view as? NativeAdView
            ?: error("The provided layout must be a NativeAdView root.")

        container.removeAllViews()
        container.addView(nativeAdView)
        container.visibility = View.VISIBLE

        populateNativeAdView(nativeAd = nativeAd, nativeAdView = nativeAdView)
    }

    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
        debugNativeAds("Populating NativeAdView ${nativeAdView.hashCode()} with nativeAd=${nativeAd.hashCode()}")
        val labelView = nativeAdView.findViewById<View?>(R.id.native_ad_label)
        val headlineView = nativeAdView.findViewById<MaterialTextView?>(R.id.native_ad_headline)
        val iconView = nativeAdView.findViewById<ShapeableImageView?>(R.id.native_ad_icon)
        val callToActionView =
            nativeAdView.findViewById<MaterialButton?>(R.id.native_ad_call_to_action)
        val adChoicesView = nativeAdView.findViewById<AdChoicesView?>(R.id.native_ad_choices)

        logClickableState("labelView (initial state)", labelView)
        headlineView.prepareNativeClickableAsset()
        iconView.prepareNativeClickableAsset()
        callToActionView.prepareNativeClickableAsset()
        adChoicesView.prepareNativeClickableAsset()

        logClickableState("headlineView (after prepare)", headlineView)
        logClickableState("iconView (after prepare)", iconView)
        logClickableState("ctaView (after prepare)", callToActionView)
        logClickableState("adChoices (after prepare)", adChoicesView)

        nativeAdView.headlineView = headlineView
        nativeAdView.callToActionView = callToActionView
        nativeAdView.iconView = iconView
        nativeAdView.adChoicesView = adChoicesView
        nativeAdView.advertiserView = labelView
        callToActionView?.let(nativeAdView::setClickConfirmingView)

        adChoicesView?.isVisible = true

        headlineView?.let { view ->
            view.text = nativeAd.headline
            view.isVisible = !nativeAd.headline.isNullOrBlank()
        }

        val hasIcon = nativeAd.icon != null
        iconView?.let { view ->
            view.setImageDrawable(nativeAd.icon?.drawable)
            view.isVisible = hasIcon
        }

        callToActionView?.let { view ->
            val callToAction = nativeAd.callToAction
            if (callToAction.isNullOrBlank()) {
                view.text = null
                view.isVisible = false
            } else {
                view.text = callToAction
                view.isVisible = true
            }
        }

        val clickableAssetViews = mutableMapOf<String, View>()
        headlineView?.takeIf { it.isVisible }
            ?.let { clickableAssetViews[ASSET_HEADLINE] = it }
        callToActionView?.takeIf { it.isVisible }?.let {
            clickableAssetViews[ASSET_CALL_TO_ACTION] = it
        }
        iconView?.takeIf { it.isVisible }?.let { clickableAssetViews[ASSET_ICON] = it }

        val nonClickableAssetViews = mutableMapOf<String, View>()
        labelView?.let { nonClickableAssetViews[ASSET_ADVERTISER] = it }
        adChoicesView?.let { nonClickableAssetViews[ASSET_ADCHOICES_CONTAINER_VIEW] = it }

        val registered = nativeAd.registerNativeAdViewCompat(
            nativeAdView = nativeAdView,
            clickableAssets = clickableAssetViews,
            nonClickableAssets = nonClickableAssetViews,
        )

        if (registered) {
            debugNativeAds(
                "Registered native ad view using registerNativeAdViewCompat clickable=${clickableAssetViews.keys} nonClickable=${nonClickableAssetViews.keys}"
            )
            runCatching {
                nativeAdView.setNativeAd(nativeAd)
                debugNativeAds("setNativeAd invoked after registerNativeAdViewCompat for compatibility")
            }.onFailure { error ->
                debugNativeAds("setNativeAd post-registerNativeAdViewCompat failed: ${error.message}")
            }
        } else {
            debugNativeAds("registerNativeAdViewCompat unavailable - falling back to setNativeAd")
            nativeAdView.setNativeAd(nativeAd)
        }
        logClickableState("labelView (after setNativeAd)", labelView)
        logClickableState("headlineView (after setNativeAd)", headlineView)
        logClickableState("ctaView (after setNativeAd)", callToActionView)
        logClickableState("iconView (after setNativeAd)", iconView)
        logClickableState("adChoices (after setNativeAd)", adChoicesView)
        nativeAdView.logBoundsAsync("nativeAdView")
        labelView.logBoundsAsync("labelView")
        headlineView.logBoundsAsync("headlineView")
        iconView.logBoundsAsync("iconView")
        callToActionView.logBoundsAsync("ctaView")
        adChoicesView.logBoundsAsync("adChoicesView")
        headlineView.checkOverlap("headlineView", listOfNotNull(callToActionView, adChoicesView))
        iconView.checkOverlap("iconView", listOfNotNull(callToActionView, adChoicesView))
        callToActionView.checkOverlap(
            "ctaView",
            listOfNotNull(adChoicesView, headlineView, iconView)
        )
        ensureNativeFallbackClicks(
            headlineView = headlineView,
            iconView = iconView,
            callToActionView = callToActionView,
        )
        nativeAdView.tag = nativeAd
    }
}

private fun View?.prepareNativeClickableAsset() {
    this ?: return
    isClickable = true
    isFocusable = true
    isFocusableInTouchMode = true
    isEnabled = true
}

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

private fun logClickableState(label: String, view: View?) {
    if (view == null) {
        debugNativeAds("$label is null")
        return
    }
    debugNativeAds(
        "$label -> visible=${view.isVisible} enabled=${view.isEnabled} clickable=${view.isClickable} " +
                "hasOnClick=${view.hasOnClickListeners()} onClickListener=${view.describeOnClickListener()}"
    )
}

private fun View?.logBoundsAsync(label: String) {
    this ?: return
    post {
        val rect = Rect()
        val globalVisible = getGlobalVisibleRect(rect)
        debugNativeAds(
            "$label bounds=$rect visibleInWindow=$globalVisible width=$width height=$height " +
                    "alpha=$alpha translation=($translationX,$translationY) elevation=$elevation parentChain=${parent.describeParentChain()}"
        )
    }
}

private fun View?.checkOverlap(label: String, others: List<View>) {
    this ?: return
    post {
        val rect = Rect()
        if (!getGlobalVisibleRect(rect)) return@post
        others.forEach { other ->
            if (!other.isVisible) return@forEach
            val otherRect = Rect()
            if (other.getGlobalVisibleRect(otherRect) && Rect.intersects(rect, otherRect)) {
                debugNativeAds(
                    "$label overlaps with ${other::class.java.simpleName} viewRect=$rect otherRect=$otherRect"
                )
            }
        }
    }
}

private fun View?.describeOnClickListener(): String? = getExistingOnClickListener()?.javaClass?.name

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
                "NativeAdFallbackClickListener($label) invoked. delegate=${delegate != null} " +
                        "nativeAdView=${nativeAdView.hashCode()}"
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

private fun ViewParent?.describeParentChain(): String {
    if (this == null) return "<none>"
    val chain = mutableListOf<String>()
    var current: ViewParent? = this
    while (current != null) {
        chain += current::class.java.simpleName
        current = current.parent
    }
    return chain.joinToString(separator = " -> ")
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
