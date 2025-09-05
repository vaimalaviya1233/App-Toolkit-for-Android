package com.d4rk.android.apps.apptoolkit.core.ui.components.ads

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.ads.NativeAdView
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

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

        nativeAd?.let { ad ->
            NativeAdView(ad = ad) { loadedAd, view ->
                Card(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SizeConstants.LargeSize),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        loadedAd.icon?.drawable?.let { drawable ->
                            Image(
                                painter = remember(drawable) {
                                    BitmapPainter(drawable.toBitmap().asImageBitmap())
                                },
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
                            Button(onClick = { view.performClick() }) {
                                Text(text = cta)
                            }
                        }
                    }
                }
            }
        }
    }
}