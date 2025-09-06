package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.button.MaterialButton
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "HelpNativeAd"

/**
 * Help screen specific native ad banner composable.
 */
@Composable
fun HelpNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = SizeConstants.MediumSize)
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
            val loader =
                AdLoader.Builder(context, adsConfig.bannerAdUnitId)
                    .forNativeAd { ad -> nativeAd = ad }
                    .withAdListener(
                        object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.e(TAG, "Native ad failed to load: ${error.message}")
                            }

                            override fun onAdLoaded() {
                                Log.d(TAG, "Native ad was loaded.")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Native ad recorded an impression.")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "Native ad was clicked.")
                            }
                        }
                    )
                    .build()
            loader.loadAd(AdRequest.Builder().build())
        }
        val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
        val colorOnPrimary = MaterialTheme.colorScheme.onPrimary.toArgb()

        nativeAd?.let { ad ->
            NativeAdView(ad = ad) { loadedAd, ctaView, _ ->
                Card(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = SizeConstants.MediumSize)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SizeConstants.LargeSize)
                    ) {
                        AdLabel()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            loadedAd.icon?.let { icon ->
                                AsyncImage(
                                    model = icon.uri ?: icon.drawable,
                                    contentDescription = loadedAd.headline,
                                    modifier = Modifier
                                        .size(SizeConstants.ExtraLargeIncreasedSize)
                                        .clip(RoundedCornerShape(size = SizeConstants.SmallSize))
                                )
                                LargeHorizontalSpacer()
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = loadedAd.headline ?: "",
                                    fontWeight = FontWeight.Bold
                                )
                                loadedAd.body?.let { body ->
                                    Text(
                                        text = body,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            loadedAd.callToAction?.let { cta ->
                                LargeHorizontalSpacer()
                                AndroidView(
                                    factory = {
                                        (ctaView.parent as? ViewGroup)?.removeView(ctaView)
                                        ctaView
                                    },
                                    update = { view ->
                                        (view as MaterialButton).apply {
                                            text = cta
                                            setBackgroundColor(colorPrimary)
                                            setTextColor(colorOnPrimary)
                                            visibility = View.VISIBLE
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

