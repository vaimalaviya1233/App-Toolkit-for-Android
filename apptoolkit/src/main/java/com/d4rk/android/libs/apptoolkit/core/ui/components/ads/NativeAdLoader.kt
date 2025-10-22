package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd

const val DEBUGGING_NATIVE_ADS_TAG: String = "DebuggingNative"

internal fun debugNativeAds(message: String) {
    println(message = "$DEBUGGING_NATIVE_ADS_TAG -> Here is $message")
}

object NativeAdLoader {

    private const val TAG = "NativeAdLoader"

    fun load(
        context: Context,
        container: ViewGroup,
        @LayoutRes layoutRes: Int,
        adRequest: AdRequest,
        listener: AdListener? = null,
        adUnitId: String? = null,
        onNativeAdLoaded: ((NativeAd) -> Unit)? = null,
    ) {
        val resolvedAdUnitId = adUnitId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.native_ad_support_unit_id)
        debugNativeAds("Preparing to load native ad for unit id: $resolvedAdUnitId")

        val builder = AdLoader.Builder(context, resolvedAdUnitId)
            .forNativeAd { nativeAd ->
                debugNativeAds("Native ad loaded. Binding layout=$layoutRes to container=${container::class.simpleName}")
                NativeAdViewBinder.bind(
                    context = context,
                    container = container,
                    layoutRes = layoutRes,
                    nativeAd = nativeAd,
                )
                debugNativeAds("Native ad bound to container. invoking onNativeAdLoaded")
                onNativeAdLoaded?.invoke(nativeAd)
            }

        builder.withAdListener(createAdListener(container, listener))

        val adLoader = builder.build()
        debugNativeAds("Starting native ad load request")
        adLoader.loadAd(adRequest)
    }

    private fun createAdListener(container: ViewGroup, delegate: AdListener?): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                debugNativeAds("AdLoader callback onAdLoaded - making container visible")
                container.visibility = View.VISIBLE
                delegate?.onAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                debugNativeAds("AdLoader callback onAdFailedToLoad: ${loadAdError.message}")
                Log.w(TAG, "Failed to load native ad: ${loadAdError.message}")
                container.removeAllViews()
                container.visibility = View.GONE
                delegate?.onAdFailedToLoad(loadAdError)
            }

            override fun onAdOpened() {
                super.onAdOpened()
                debugNativeAds("AdLoader callback onAdOpened")
                delegate?.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                debugNativeAds("AdLoader callback onAdClicked")
                delegate?.onAdClicked()
            }

            override fun onAdClosed() {
                super.onAdClosed()
                debugNativeAds("AdLoader callback onAdClosed")
                delegate?.onAdClosed()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                debugNativeAds("AdLoader callback onAdImpression")
                delegate?.onAdImpression()
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                debugNativeAds("AdLoader callback onAdSwipeGestureClicked")
                delegate?.onAdSwipeGestureClicked()
            }
        }
    }
}
