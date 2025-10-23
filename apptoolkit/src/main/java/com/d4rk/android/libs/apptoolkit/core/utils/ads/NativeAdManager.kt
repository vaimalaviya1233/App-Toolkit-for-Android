package com.d4rk.android.libs.apptoolkit.core.utils.ads

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ads.DebugAdsConstants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

/**
 * Singleton object to manage the loading and caching of native ads.
 * This manager pre-loads ads into a queue and provides them on demand.
 */
object NativeAdManager {

    val adQueue = SnapshotStateList<NativeAd>()

    var isLoading by mutableStateOf(false)
        private set

    /**
     * Loads a batch of native ads into the adQueue.
     * This function is safe to call multiple times; it will not queue up new requests if ads are already being loaded.
     */
    fun loadNativeAds(context: Context , unitId: String = DebugAdsConstants.NATIVE_AD_UNIT_ID) {
        if (isLoading || adQueue.isNotEmpty()) {
            return
        }

        val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

        val nativeAdOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

        isLoading = true

        val adLoader = AdLoader.Builder(context, unitId)
            .withNativeAdOptions(nativeAdOptions)
            .forNativeAd { nativeAd ->
                adQueue.add(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isLoading = false
                    println(message = "NativeAdsDebugging -> One NativeAd loaded successfully.")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    isLoading = false
                    println(message = "NativeAdsDebugging -> NativeAdLoader failed ${adError.message}")
                }
            })
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
}