package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.LargeHorizontalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.LoadAdError

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    val dataStore: CommonDataStore = remember { CommonDataStore.getInstance(context = context) }
    val showAds: Boolean by dataStore.adsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    if (LocalInspectionMode.current) {
        NavigationBar(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "Native Ad")
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
            NativeAdView(nativeAd = ad, modifier = modifier.fillMaxWidth()) {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = SizeConstants.LargeSize),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AdLabel()
                        LargeHorizontalSpacer()
                        NativeAdChoicesView()
                        LargeHorizontalSpacer()
                        ad.icon?.let { icon ->
                            NativeAdIconView(
                                modifier = Modifier
                                    .size(SizeConstants.ExtraLargeIncreasedSize)
                                    .clip(RoundedCornerShape(size = SizeConstants.SmallSize)),
                            ) {
                                AsyncImage(
                                    model = icon.uri ?: icon.drawable,
                                    contentDescription = ad.headline,
                                )
                            }
                            LargeHorizontalSpacer()
                        }
                        ad.headline?.let {
                            NativeAdHeadlineView {
                                Text(
                                    text = it,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
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
