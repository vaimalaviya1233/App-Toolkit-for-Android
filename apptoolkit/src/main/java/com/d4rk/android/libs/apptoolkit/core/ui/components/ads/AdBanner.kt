package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(modifier: Modifier = Modifier, adsConfig: AdsConfig) {
    val context: Context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current) {
        val density = LocalDensity.current
        val adHeightInDp = with(density) { adsConfig.adSize.getHeightInPixels(context).toDp() }
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(adHeightInDp)
                .background(Color.LightGray)
        ) {
            Text(
                text = "Ad Banner Preview (${adsConfig.adSize.width}x${adsConfig.adSize.height})",
                modifier = Modifier.align(Alignment.Center)
            )
        }
        return
    }

    if (showAds) {
        val adRequest = remember { AdRequest.Builder().build() }

        val adView = remember(adsConfig.bannerAdUnitId, adsConfig.adSize) {
            AdView(context).apply {
                this.adUnitId = adsConfig.bannerAdUnitId
                setAdSize(adsConfig.adSize)
            }
        }

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(adsConfig.adSize.height.dp),
            factory = { adView }
        )

        LifecycleResumeEffect(key1 = adView) {
            adView.resume()
            onPauseOrDispose {
                adView.pause()
            }
        }

        DisposableEffect(key1 = adView) {
            onDispose {
                adView.destroy()
            }
        }

        LaunchedEffect(key1 = adView, key2 = adRequest) {
            adView.loadAd(adRequest)
        }
    }
}