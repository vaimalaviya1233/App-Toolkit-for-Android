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

        val builder = AdLoader.Builder(context, resolvedAdUnitId)
            .forNativeAd { nativeAd ->
                NativeAdViewBinder.bind(
                    context = context,
                    container = container,
                    layoutRes = layoutRes,
                    nativeAd = nativeAd,
                )
                onNativeAdLoaded?.invoke(nativeAd)
            }

        builder.withAdListener(createAdListener(container, listener))

        val adLoader = builder.build()
        adLoader.loadAd(adRequest)
    }

    private fun createAdListener(container: ViewGroup, delegate: AdListener?): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                container.visibility = View.VISIBLE
                delegate?.onAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.w(TAG, "Failed to load native ad: ${loadAdError.message}")
                container.removeAllViews()
                container.visibility = View.GONE
                delegate?.onAdFailedToLoad(loadAdError)
            }

            override fun onAdOpened() {
                super.onAdOpened()
                delegate?.onAdOpened()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                delegate?.onAdClicked()
            }

            override fun onAdClosed() {
                super.onAdClosed()
                delegate?.onAdClosed()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                delegate?.onAdImpression()
            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()
                delegate?.onAdSwipeGestureClicked()
            }
        }
    }
}
