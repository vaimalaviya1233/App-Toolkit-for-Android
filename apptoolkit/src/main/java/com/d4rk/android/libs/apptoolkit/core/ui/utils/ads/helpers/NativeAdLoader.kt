package com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton

const val DEBUGGING_NATIVE_ADS_TAG: String = "DebuggingNative"

internal fun debugNativeAds(message: String) {
    println(message = "$DEBUGGING_NATIVE_ADS_TAG -> Here is $message")
}

fun dp(ctx: Context, value: Int) =
        (value * ctx.resources.displayMetrics.density).toInt()

fun bindArticleNativeAd(view: NativeAdView, ad: NativeAd) {
    (view.headlineView as? TextView)?.let { textView ->
        val headline = ad.headline
        textView.text = headline
        textView.visibility = if (headline.isNullOrBlank()) View.GONE else View.VISIBLE
    }
    view.mediaView?.mediaContent = ad.mediaContent

    (view.bodyView as? TextView)?.let {
        val text = ad.body
        it.text = text
        it.visibility = if (text.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    (view.iconView as? ImageView)?.let { iv ->
        val icon = ad.icon
        if (icon != null) {
            iv.setImageDrawable(icon.drawable)
            iv.visibility = View.VISIBLE
        } else {
            iv.visibility = View.GONE
        }
    }

    (view.advertiserView as? TextView)?.let {
        val adv = ad.advertiser
        it.text = adv
        it.visibility = if (adv.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    (view.callToActionView as? MaterialButton)?.let { btn ->
        val cta = ad.callToAction
        btn.text = cta
        btn.visibility = if (cta.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    view.setNativeAd(ad)
}
