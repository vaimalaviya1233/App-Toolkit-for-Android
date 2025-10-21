package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current || !showAds) return

    rememberNativeAd(
        adUnitId = adsConfig.bannerAdUnitId,
        shouldLoad = adsConfig.bannerAdUnitId.isNotBlank(),
    )?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { context ->
                NativeAdBannerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    setNativeAdLayout(R.layout.native_ad_bottom_app_bar)
                    setNativeAdUnitId(adsConfig.bannerAdUnitId)
                }
            },
            update = { view ->
                view.setNativeAdLayout(R.layout.native_ad_bottom_app_bar)
                view.setNativeAdUnitId(adsConfig.bannerAdUnitId)
                view.renderNativeAd(ad)
            },
        )
    }
}
