package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton

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
        val headlineView = nativeAdView.findViewById<TextView?>(R.id.native_ad_headline)
        val iconView = nativeAdView.findViewById<ImageView?>(R.id.native_ad_icon)
        val iconContainer = nativeAdView.findViewById<View?>(R.id.native_ad_icon_container)
        val callToActionView =
            nativeAdView.findViewById<MaterialButton?>(R.id.native_ad_call_to_action)
        val adChoicesView = nativeAdView.findViewById<AdChoicesView?>(R.id.native_ad_choices)

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
        iconContainer?.isVisible = hasIcon
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
        nativeAdView.tag = nativeAd
    }
}
