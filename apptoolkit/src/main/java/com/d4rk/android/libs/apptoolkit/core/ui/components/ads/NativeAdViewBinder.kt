package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

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
        val headlineView = nativeAdView.findViewById<MaterialTextView?>(R.id.native_ad_headline)
        val iconView = nativeAdView.findViewById<ShapeableImageView?>(R.id.native_ad_icon)
        val callToActionView =
            nativeAdView.findViewById<MaterialButton?>(R.id.native_ad_call_to_action)
        val adChoicesView = nativeAdView.findViewById<AdChoicesView?>(R.id.native_ad_choices)

        headlineView.prepareNativeClickableAsset()
        iconView.prepareNativeClickableAsset()
        callToActionView.prepareNativeClickableAsset()
        adChoicesView.prepareNativeClickableAsset()

        nativeAdView.headlineView = headlineView
        nativeAdView.callToActionView = callToActionView
        nativeAdView.iconView = iconView
        nativeAdView.adChoicesView = adChoicesView

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

        nativeAdView.setNativeAd(nativeAd)
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

private fun ensureNativeFallbackClicks(
    headlineView: View?,
    iconView: View?,
    callToActionView: View?,
) {
    headlineView.ensureNativeFallbackClickTarget()
    iconView.ensureNativeFallbackClickTarget()
    callToActionView.ensureNativeFallbackClickTarget()
}

private fun View?.ensureNativeFallbackClickTarget() {
    this ?: return
    if (!hasOnClickListeners()) {
        val nativeAdView = findParentNativeAdView()
        if (nativeAdView != null) {
            setOnClickListener { nativeAdView.performClick() }
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
