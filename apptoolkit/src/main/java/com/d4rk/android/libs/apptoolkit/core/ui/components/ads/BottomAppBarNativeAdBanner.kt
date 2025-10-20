package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current || !showAds) return

    rememberNativeAd(adUnitId = adsConfig.bannerAdUnitId , shouldLoad = adsConfig.bannerAdUnitId.isNotBlank())?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                val view = LayoutInflater.from(context).inflate(R.layout.native_ad_bottom_app_bar , /* root = */ FrameLayout(context) , /* attachToRoot = */ false) as NativeAdView
                if (view.layoutParams == null) {
                    view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                view
            },
            update = { nativeAdView ->
                populateBottomAppBarNativeAdView(nativeAd = ad, nativeAdView = nativeAdView)
            }
        )
        DisposableEffect(ad) {
            onDispose {
                runCatching { ad.destroy() }
            }
        }
    }
}

private fun populateBottomAppBarNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView) {
    val headlineView = nativeAdView.findViewById<TextView>(R.id.native_ad_headline)
    val iconView = nativeAdView.findViewById<ImageView>(R.id.native_ad_icon)
    val callToActionView = nativeAdView.findViewById<MaterialButton>(R.id.native_ad_call_to_action)
    val adChoicesView = nativeAdView.findViewById<AdChoicesView>(R.id.native_ad_choices)
    val iconContainer = nativeAdView.findViewById<View>(R.id.native_ad_icon_container)

    nativeAdView.headlineView = headlineView
    nativeAdView.adChoicesView = adChoicesView
    adChoicesView?.visibility = View.VISIBLE

    headlineView?.let {
        it.text = nativeAd.headline
        it.visibility = if (nativeAd.headline.isNullOrBlank()) View.GONE else View.VISIBLE
    }

    if (nativeAd.icon != null) {
        iconView?.setImageDrawable(nativeAd.icon?.drawable)
        iconView?.visibility = View.VISIBLE
        iconContainer?.visibility = View.VISIBLE
        nativeAdView.iconView = iconView
    } else {
        iconView?.setImageDrawable(null)
        iconView?.visibility = View.GONE
        iconContainer?.visibility = View.GONE
        nativeAdView.iconView = null
    }

    if (nativeAd.callToAction.isNullOrBlank()) {
        callToActionView?.text = null
        callToActionView?.visibility = View.GONE
        nativeAdView.callToActionView = null
    } else {
        callToActionView?.text = nativeAd.callToAction
        callToActionView?.visibility = View.VISIBLE
        nativeAdView.callToActionView = callToActionView
    }

    nativeAdView.setNativeAd(nativeAd)
    nativeAdView.tag = nativeAd
}
