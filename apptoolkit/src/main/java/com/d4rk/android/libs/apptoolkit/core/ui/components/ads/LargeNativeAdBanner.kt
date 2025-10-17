package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.util.Log
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.google.android.gms.ads.LoadAdError

@Composable
fun LargeNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    if (LocalInspectionMode.current) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.LightGray),
            ) {
                Text(text = "Native Ad", modifier = Modifier.align(Alignment.Center))
            }
        }
        return
    }

    if (showAds) {
        val nativeAd = rememberNativeAd(
            adUnitId = adsConfig.bannerAdUnitId,
            shouldLoad = adsConfig.bannerAdUnitId.isNotBlank(),
            onAdFailedToLoad = { error: LoadAdError ->
                Log.e(TAG, "Native ad failed to load: ${error.message}")
            },
            onAdLoaded = {
                Log.d(TAG, "Native ad was loaded.")
            },
            onAdImpression = {
                Log.d(TAG, "Native ad recorded an impression.")
            },
            onAdClicked = {
                Log.d(TAG, "Native ad was clicked.")
            },
        )

        nativeAd?.let { ad ->
            NativeAdView(nativeAd = ad) {
                OutlinedCard(
                    modifier = modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(size = SizeConstants.ExtraLargeSize),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(SizeConstants.LargeSize),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AdLabel()
                            NativeAdChoicesView()
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ad.icon?.let { icon ->
                                NativeAdIconView(
                                    modifier = Modifier
                                        .size(SizeConstants.ExtraExtraLargeSize)
                                        .clip(RoundedCornerShape(size = SizeConstants.SmallSize)),
                                ) {
                                    AsyncImage(
                                        model = icon.uri ?: icon.drawable,
                                        contentDescription = ad.headline,
                                    )
                                }
                                LargeHorizontalSpacer()
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                ad.headline?.let {
                                    NativeAdHeadlineView {
                                        Text(
                                            text = it,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                }
                                ad.body?.let { body ->
                                    NativeAdBodyView {
                                        Text(
                                            text = body,
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                }
                            }
                            ad.callToAction?.let { cta ->
                                LargeHorizontalSpacer()
                                NativeAdCallToActionView {
                                    NativeAdButton(text = cta)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
