package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.widget.Button
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

data class NativeAdViewAssets(
    val headlineView: ComposeView,
    val bodyView: ComposeView,
    val iconView: ImageView,
    val mediaView: MediaView,
    val callToActionView: Button,
    val adChoicesView: AdChoicesView,
    val contentView: ComposeView,
)

@Composable
fun NativeAdView(
    ad: NativeAd,
    adContent: @Composable (ad: NativeAd, assetViews: NativeAdViewAssets) -> Unit,
) {
    val context = LocalContext.current
    val assets = remember {
        NativeAdViewAssets(
            headlineView = ComposeView(context),
            bodyView = ComposeView(context),
            iconView = ImageView(context),
            mediaView = MediaView(context),
            callToActionView = Button(context),
            adChoicesView = AdChoicesView(context),
            contentView = ComposeView(context),
        )
    }

    AndroidView(
        factory = {
            GoogleNativeAdView(context).apply {
                addView(assets.contentView)
            }
        },
        update = { adView ->
            adView.headlineView = assets.headlineView
            adView.bodyView = assets.bodyView
            adView.iconView = assets.iconView
            adView.mediaView = assets.mediaView
            adView.callToActionView = assets.callToActionView
            adView.adChoicesView = assets.adChoicesView
            adView.setNativeAd(ad)
            assets.contentView.setContent { adContent(ad, assets) }
        }
    )
}

