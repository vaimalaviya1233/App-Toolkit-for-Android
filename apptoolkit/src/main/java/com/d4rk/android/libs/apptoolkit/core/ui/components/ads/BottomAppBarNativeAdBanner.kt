package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    if (LocalInspectionMode.current) {
        debugNativeAds("Skipping BottomAppBarNativeAdBanner in inspection mode")
        return
    }

    val showAds = rememberAdsEnabled()
    if (!showAds) {
        debugNativeAds("Ads disabled. BottomAppBarNativeAdBanner will not be shown")
        return
    }

    val adUnitIdState = rememberUpdatedState(newValue = adsConfig.bannerAdUnitId)

    val nativeAd = rememberNativeAd(
        adUnitId = adUnitIdState.value,
        shouldLoad = adUnitIdState.value.isNotBlank(),
        onAdFailedToLoad = { error ->
            debugNativeAds("BottomAppBar native ad failed to load: ${error.message}")
        },
        onAdLoaded = { loadedAd ->
            debugNativeAds("BottomAppBar native ad loaded with headline=${loadedAd.headline}")
        },
        onAdImpression = {
            debugNativeAds("BottomAppBar native ad recorded an impression")
        },
        onAdClicked = {
            debugNativeAds("BottomAppBar native ad registered a click")
        },
    )

    if (nativeAd == null) {
        debugNativeAds("BottomAppBar native ad is not yet available")
        return
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            debugNativeAds("Creating NativeAdBannerView for BottomAppBar")
            NativeAdBannerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                setNativeAdLayout(R.layout.native_ad_bottom_app_bar)
                setNativeAdUnitId(adUnitIdState.value)
            }
        },
        update = { view ->
            debugNativeAds(
                "Updating NativeAdBannerView with ad=${nativeAd.hashCode()} headline=${nativeAd.headline} cta=${nativeAd.callToAction}"
            )
            view.setNativeAdLayout(R.layout.native_ad_bottom_app_bar)
            view.setNativeAdUnitId(adUnitIdState.value)
            view.renderNativeAd(nativeAd)
        },
        onRelease = { releasedView ->
            debugNativeAds("Releasing NativeAdBannerView for BottomAppBar")
            releasedView.clearAd()
        },
    )
}
