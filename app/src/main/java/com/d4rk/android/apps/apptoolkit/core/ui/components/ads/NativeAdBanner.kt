package com.d4rk.android.apps.apptoolkit.core.ui.components.ads

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.d4rk.android.apps.apptoolkit.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdAssetNames
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.NativeAdViewHolder
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.material.button.MaterialButton

@Composable
fun NativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.LightGray)
            ) {
                Text(text = "Native Ad", modifier = Modifier.align(Alignment.Center))
            }
        }
        return
    }

    if (showAds) {
        var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

        DisposableEffect(key1 = nativeAd) {
            onDispose { nativeAd?.destroy() }
        }

        LaunchedEffect(key1 = adsConfig.bannerAdUnitId) {
            val loader = AdLoader.Builder(context, adsConfig.bannerAdUnitId)
                .forNativeAd { ad -> nativeAd = ad }
                .build()
            loader.loadAd(AdRequest.Builder().build())
        }
        val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
        val colorOnPrimary = MaterialTheme.colorScheme.onPrimary.toArgb()

        nativeAd?.let { ad ->
            AndroidView(
                modifier = modifier.fillMaxWidth(),
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(
                        R.layout.native_ad_banner,
                        FrameLayout(ctx),
                        false
                    )
                },
                update = { view ->
                    val adView = view.findViewById<NativeAdView>(R.id.native_ad_view)
                    val mediaView = adView.findViewById<MediaView>(R.id.ad_media)
                    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val ctaView = adView.findViewById<MaterialButton>(R.id.ad_cta)
                    val adChoicesView = adView.findViewById<AdChoicesView>(R.id.ad_choices)

                    headlineView.text = ad.headline
                    ad.body?.let {
                        bodyView.visibility = View.VISIBLE
                        bodyView.text = it
                    } ?: run { bodyView.visibility = View.GONE }

                    val clickableAssets = mutableMapOf<String, View>(
                        NativeAdAssetNames.ASSET_HEADLINE to headlineView,
                        NativeAdAssetNames.ASSET_CALL_TO_ACTION to ctaView
                    )
                    val nonClickableAssets = mutableMapOf<String, View>(
                        NativeAdAssetNames.ASSET_BODY to bodyView
                    )

                    ad.callToAction?.let {
                        ctaView.visibility = View.VISIBLE
                        ctaView.text = it
                        ctaView.setBackgroundColor(colorPrimary)
                        ctaView.setTextColor(colorOnPrimary)
                    } ?: run { ctaView.visibility = View.GONE }

                    if (ad.mediaContent != null && (ad.mediaContent!!.hasVideoContent() || ad.mediaContent!!.mainImage != null)) {
                        mediaView.visibility = View.VISIBLE
                        mediaView.mediaContent = ad.mediaContent
                        iconView.visibility = View.GONE
                        clickableAssets[NativeAdAssetNames.ASSET_MEDIA_VIDEO] = mediaView
                    } else {
                        mediaView.visibility = View.GONE
                        ad.icon?.let {
                            iconView.visibility = View.VISIBLE
                            iconView.setImageDrawable(it.drawable)
                            clickableAssets[NativeAdAssetNames.ASSET_ICON] = iconView
                        } ?: run { iconView.visibility = View.GONE }
                    }

                    NativeAdViewHolder(adView, clickableAssets, nonClickableAssets).setNativeAd(ad)
                    adView.adChoicesView = adChoicesView
                }
            )
        }
    }
}