package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

@Composable
fun NativeAdView(
    ad: NativeAd,
    adContent: @Composable (ad: NativeAd, contentView: View) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val context = LocalContext.current
    val adChoicesView = remember { AdChoicesView(context) }

    AndroidView(
        factory = { ctx ->
            val contentView = ComposeView(ctx).apply { id = contentViewId }
            GoogleNativeAdView(ctx).apply {
                id = adViewId
                addView(contentView)
            }
        },
        update = { view ->
            val adView = view.findViewById<GoogleNativeAdView>(adViewId)
            val contentView = view.findViewById<ComposeView>(contentViewId)

            adView.setNativeAd(ad)
            adView.callToActionView = contentView
            adView.adChoicesView = adChoicesView

            contentView.setContent {
                Box {
                    adContent(ad, contentView)
                    AndroidView(
                        factory = {
                            (adChoicesView.parent as? ViewGroup)?.removeView(adChoicesView)
                            adChoicesView
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(SizeConstants.SmallSize)
                    )
                }
            }
        }
    )
}

