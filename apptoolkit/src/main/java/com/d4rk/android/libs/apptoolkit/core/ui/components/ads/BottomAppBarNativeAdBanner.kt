package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.bindArticleNativeAd
import com.d4rk.android.libs.apptoolkit.core.utils.ads.NativeAdManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.flow.filter

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
) {
    val adsConfig = AdsConfig(bannerAdUnitId = "ca-app-pub-3940256099942544/2247696110")

    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        NativeAdManager.loadNativeAds(
            context = context,
            unitId = adsConfig.bannerAdUnitId
        )

        snapshotFlow { NativeAdManager.adQueue.size }
            .filter { it > 0 && nativeAd == null }
            .collect {
                nativeAd = NativeAdManager.adQueue.removeAt(0)
            }
    }

    DisposableEffect(nativeAd) {
        onDispose { nativeAd?.destroy() }
    }

    nativeAd?.let { ad ->
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { ctx -> createBottomAdView(ctx) },
                update = { view ->
                    view.visibility = View.VISIBLE
                    bindArticleNativeAd(view, ad)
                }
            )
        }
    } ?: run {
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            val horizontalPadding = dimensionResource(id = R.dimen.native_ad_bottom_bar_horizontal_padding)
            val verticalPadding = dimensionResource(id = R.dimen.native_ad_bottom_bar_vertical_padding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(
                        start = horizontalPadding,
                        top = verticalPadding,
                        end = horizontalPadding,
                        bottom = verticalPadding
                    ),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun createBottomAdView(ctx: Context): NativeAdView {
    val inflater = LayoutInflater.from(ctx)

    val parent = FrameLayout(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val container = inflater.inflate(R.layout.native_ad_bottom_bar, parent, false) as NativeAdView
    container.layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    container.headlineView = container.findViewById<TextView>(R.id.native_ad_headline)
    container.bodyView = container.findViewById<TextView>(R.id.native_ad_body)
    container.advertiserView = container.findViewById<TextView>(R.id.native_ad_advertiser)
    container.iconView = container.findViewById<ImageView>(R.id.native_ad_icon)
    container.callToActionView =
        container.findViewById<Button>(R.id.native_ad_call_to_action) // use Button if you dropped Material

    return container
}