package com.d4rk.android.libs.apptoolkit.core.utils.ads

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ads.DebugAdsConstants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Singleton object to manage the loading and caching of native ads.
 * This manager pre-loads ads into a queue and provides them on demand.
 */
object NativeAdManager {

    val adQueue = SnapshotStateList<NativeAd>()

     var isLoading = false

    /**
     * Loads a batch of native ads into the adQueue.
     * This function is safe to call multiple times; it will not queue up new requests if ads are already being loaded.
     */
    fun loadNativeAds(context: Context , unitId: String = DebugAdsConstants.NATIVE_AD_UNIT_ID) {
        val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

        val nativeAdOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

        CoroutineScope(Dispatchers.IO).launch {
            if (isLoading) return@launch
            isLoading = true

            val adLoader = AdLoader.Builder(context, unitId)
                    .withNativeAdOptions(nativeAdOptions)
                    .forNativeAd { nativeAd ->
                        adQueue.add(nativeAd)
                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            println(message = "NativeAdsDebugging -> One NativeAd loaded successfully.")
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            println(message = "NativeAdsDebugging -> NativeAdLoader failed ${adError.message}")
                        }
                    })
                    .build()
            adLoader.loadAd(AdRequest.Builder().build())
            isLoading = false
        }
    }
}