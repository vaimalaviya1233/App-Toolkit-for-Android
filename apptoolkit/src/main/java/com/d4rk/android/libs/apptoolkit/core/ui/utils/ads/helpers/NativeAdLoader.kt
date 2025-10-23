package com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton

fun dp(context: Context , dpValue: Int) =
        (dpValue * context.resources.displayMetrics.density).toInt()

fun bindArticleNativeAd(nativeAdView: NativeAdView , nativeAd: NativeAd) {
    (nativeAdView.headlineView as? TextView)?.let { headlineView ->
        val headline = nativeAd.headline
        headlineView.text = headline
        headlineView.visibility = if (headline.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent

    (nativeAdView.bodyView as? TextView)?.let {
        val text = nativeAd.body
        it.text = text
        it.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    (nativeAdView.iconView as? ImageView)?.let { iconView ->
        val icon = nativeAd.icon
        if (icon != null) {
            iconView.setImageDrawable(icon.drawable)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = View.GONE
        }
    }

    (nativeAdView.advertiserView as? TextView)?.let {
        val advertiser = nativeAd.advertiser
        it.text = advertiser
        it.visibility = if (advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    (nativeAdView.callToActionView as? MaterialButton)?.let { callToActionButton ->
        val callToAction = nativeAd.callToAction
        callToActionButton.text = callToAction
        callToActionButton.visibility = if (callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    nativeAdView.setNativeAd(nativeAd)
}
