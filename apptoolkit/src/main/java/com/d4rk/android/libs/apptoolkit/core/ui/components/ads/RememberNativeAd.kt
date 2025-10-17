package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import com.d4rk.android.libs.apptoolkit.core.ui.effects.ActivityLifecycleEffect
import com.d4rk.android.libs.apptoolkit.core.utils.extensions.findActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Remembers and manages the lifecycle of a [NativeAd] for Compose screens.
 */
@Composable
fun rememberNativeAd(
    adUnitId: String,
    shouldLoad: Boolean,
    configureBuilder: (AdLoader.Builder.() -> Unit)? = null,
    onAdFailedToLoad: (LoadAdError) -> Unit = {},
    onAdLoaded: (NativeAd) -> Unit = {},
    onAdImpression: () -> Unit = {},
    onAdClicked: () -> Unit = {},
): NativeAd? {
    val context = LocalContext.current
    val hostActivity = remember(context) { context.findActivity() }
    var nativeAd by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }
    val updatedConfigureBuilder = rememberUpdatedState(configureBuilder)
    val updatedOnAdFailedToLoad = rememberUpdatedState(onAdFailedToLoad)
    val updatedOnAdLoaded = rememberUpdatedState(onAdLoaded)
    val updatedOnAdImpression = rememberUpdatedState(onAdImpression)
    val updatedOnAdClicked = rememberUpdatedState(onAdClicked)

    fun destroyCurrentAd() {
        nativeAd?.destroy()
        nativeAd = null
    }

    DisposableEffect(adUnitId) {
        onDispose { destroyCurrentAd() }
    }

    ActivityLifecycleEffect(lifecycleEvent = Lifecycle.Event.ON_DESTROY) {
        destroyCurrentAd()
    }

    LaunchedEffect(adUnitId, shouldLoad) {
        if (!shouldLoad || adUnitId.isBlank()) {
            destroyCurrentAd()
            return@LaunchedEffect
        }

        val loader = withContext(Dispatchers.IO) {
            AdLoader.Builder(context, adUnitId)
                .apply { updatedConfigureBuilder.value?.invoke(this) }
                .forNativeAd { loadedAd ->
                    destroyCurrentAd()
                    val activity = hostActivity
                    if (activity?.isDestroyed == true || activity?.isFinishing == true) {
                        loadedAd.destroy()
                    } else {
                        nativeAd = loadedAd
                        updatedOnAdLoaded.value(loadedAd)
                    }
                }
                .withAdListener(
                    object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            destroyCurrentAd()
                            updatedOnAdFailedToLoad.value(error)
                        }

                        override fun onAdImpression() {
                            updatedOnAdImpression.value()
                        }

                        override fun onAdClicked() {
                            updatedOnAdClicked.value()
                        }
                    },
                )
                .build()
        }

        loader.loadAd(AdRequest.Builder().build())
    }

    return nativeAd
}
