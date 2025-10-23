package com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

fun dp(context: Context , dpValue: Int) =
        (dpValue * context.resources.displayMetrics.density).toInt()

fun bindArticleNativeAd(nativeAdView: NativeAdView, nativeAd: NativeAd) {
    nativeAdView.isClickable = true
    nativeAdView.isFocusable = true
    nativeAdView.isFocusableInTouchMode = true

    val headlineView = nativeAdView.findViewById<TextView?>(R.id.native_ad_headline).also {
        nativeAdView.headlineView = it
    }
    headlineView?.let { view ->
        val headline = nativeAd.headline
        val hasHeadline = !headline.isNullOrBlank()
        view.text = headline
        view.visibility = if (hasHeadline) View.VISIBLE else View.GONE
        view.isClickable = hasHeadline
        view.isFocusable = hasHeadline
    }

    nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent

    val bodyView = nativeAdView.findViewById<TextView?>(R.id.native_ad_body).also {
        nativeAdView.bodyView = it
    }
    bodyView?.let { view ->
        val bodyText = nativeAd.body
        val hasBody = !bodyText.isNullOrBlank()
        view.text = bodyText
        view.visibility = if (hasBody) View.VISIBLE else View.GONE
        view.isClickable = hasBody
        view.isFocusable = hasBody
    }

    val iconView = nativeAdView.findViewById<ImageView?>(R.id.native_ad_icon).also {
        nativeAdView.iconView = it
    }
    iconView?.let { view ->
        val icon = nativeAd.icon
        if (icon != null) {
            view.setImageDrawable(icon.drawable)
            view.visibility = View.VISIBLE
            view.isClickable = true
            view.isFocusable = true
        } else {
            view.setImageDrawable(null)
            view.visibility = View.GONE
            view.isClickable = false
            view.isFocusable = false
        }
    }

    val advertiserView = nativeAdView.findViewById<TextView?>(R.id.native_ad_advertiser).also {
        nativeAdView.advertiserView = it
    }
    advertiserView?.let { view ->
        val advertiser = nativeAd.advertiser
        val hasAdvertiser = !advertiser.isNullOrBlank()
        view.text = advertiser
        view.visibility = if (hasAdvertiser) View.VISIBLE else View.GONE
        view.isClickable = hasAdvertiser
        view.isFocusable = hasAdvertiser
    }

    val callToActionView = nativeAdView.findViewById<Button?>(R.id.native_ad_call_to_action).also {
        nativeAdView.callToActionView = it
    }
    callToActionView?.let { view ->
        val callToAction = nativeAd.callToAction
        val hasCallToAction = !callToAction.isNullOrBlank()
        (view as? TextView)?.text = callToAction
        view.visibility = if (hasCallToAction) View.VISIBLE else View.GONE
        view.isEnabled = hasCallToAction
        view.isClickable = hasCallToAction
        view.isFocusable = hasCallToAction
        view.isFocusableInTouchMode = hasCallToAction
    }

    nativeAdView.setNativeAd(nativeAd)
}
