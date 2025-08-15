package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import org.koin.compose.koinInject

@Composable
fun AdBanner(modifier : Modifier = Modifier , adsConfig : AdsConfig , buildInfoProvider : BuildInfoProvider = koinInject()) {
    val context: Context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean? by dataStore.ads(default = !buildInfoProvider.isDebugBuild)
        .map { it as Boolean? }
        .collectAsStateWithLifecycle(initialValue = null)

    if (showAds == true) {
        val adView = remember { AdView(context) }
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        LaunchedEffect(adView) {
            adView.loadAd(AdRequest.Builder().build())
        }

        DisposableEffect(lifecycle, adView) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> adView.pause()
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> adView.resume()
                    else -> Unit
                }
            }
            lifecycle.addObserver(observer)
            onDispose {
                lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(adsConfig.adSize.height.dp),
            factory = {
                adView.apply {
                    setAdSize(adsConfig.adSize)
                    adUnitId = adsConfig.bannerAdUnitId
                }
            }
        )
    }
}