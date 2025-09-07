package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnNextLayout
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

/**
 * A CompositionLocal that can provide a [GoogleNativeAdView] to ad attributes such as
 * [NativeAdHeadlineView].
 */
internal val LocalNativeAdView = staticCompositionLocalOf<GoogleNativeAdView?> { null }

/**
 * Compose wrapper for a [GoogleNativeAdView]. It binds the provided [nativeAd] and allows [content]
 * to register individual asset views.
 */
@Composable
fun NativeAdView(
    nativeAd: NativeAd,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val nativeAdView = remember { GoogleNativeAdView(context).apply { id = View.generateViewId() } }

    AndroidView(
        factory = {
            nativeAdView.apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                addView(
                    ComposeView(context).apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                            )
                        setContent {
                            CompositionLocalProvider(LocalNativeAdView provides nativeAdView) {
                                content()
                            }
                        }
                    }
                )
            }
        },
        modifier = modifier,
    )

    DisposableEffect(nativeAd) {
        nativeAdView.doOnNextLayout {
            Log.d(TAG, "setNativeAd invoked")
            nativeAdView.setNativeAd(nativeAd)
        }
        onDispose { nativeAd.destroy() }
    }
}

/**
 * Container for the advertiser asset inside a native ad view.
 */
@Composable
fun NativeAdAdvertiserView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                isClickable = true
                setContent(content)
                adView.advertiserView = this
                Log.d(TAG, "advertiserView registered")
            }
        },
        modifier = modifier,
        update = { it.setContent(content) },
    )
}

/** Container for the body asset inside a native ad view. */
@Composable
fun NativeAdBodyView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.bodyView = composeView
            Log.d(TAG, "bodyView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Container for the call-to-action asset inside a native ad view. */
@Composable
fun NativeAdCallToActionView(
    modifier: Modifier = Modifier,
    register: Boolean = true,
    content: @Composable () -> Unit,
) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            if (register) {
                adView.callToActionView = composeView
                Log.d(TAG, "callToActionView registered")
            } else {
                Log.d(TAG, "callToActionView (visual)")
            }
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Compose wrapper for the AdChoices overlay. */
@Composable
fun NativeAdChoicesView(modifier: Modifier = Modifier) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    AndroidView(
        factory = {
            AdChoicesView(context).apply {
                minimumWidth = 15
                minimumHeight = 15
            }
        },
        update = {
            adView.adChoicesView = it
            Log.d(TAG, "adChoicesView registered")
        },
        modifier = modifier,
    )
}

/** Container for the headline asset inside a native ad view. */
@Composable
fun NativeAdHeadlineView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.headlineView = composeView
            Log.d(TAG, "headlineView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Container for the icon asset inside a native ad view. */
@Composable
fun NativeAdIconView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.iconView = composeView
            Log.d(TAG, "iconView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Compose wrapper for the media asset inside a native ad view. */
@Composable
fun NativeAdMediaView(modifier: Modifier = Modifier) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    AndroidView(
        factory = { MediaView(context) },
        update = {
            it.isClickable = true
            adView.mediaView = it
            Log.d(TAG, "mediaView registered")
        },
        modifier = modifier,
    )
}

/** Container for the price asset inside a native ad view. */
@Composable
fun NativeAdPriceView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.priceView = composeView
            Log.d(TAG, "priceView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Container for the star rating asset inside a native ad view. */
@Composable
fun NativeAdStarRatingView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.starRatingView = composeView
            Log.d(TAG, "starRatingView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Container for the store asset inside a native ad view. */
@Composable
fun NativeAdStoreView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId(); isClickable = true } }
    AndroidView(
        factory = {
            adView.storeView = composeView
            Log.d(TAG, "storeView registered")
            composeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/** Simple ad attribution label. */
@Composable
fun NativeAdAttribution(text: String = "Ad", modifier: Modifier = Modifier) {
    val colors = ButtonDefaults.buttonColors()
    Box(
        modifier = modifier.clip(ButtonDefaults.shape).background(colors.containerColor)
    ) {
        Text(color = colors.contentColor, text = text)
    }
}

/** Button styled for native ad call-to-action content. */
@Composable
fun NativeAdButton(text: String, modifier: Modifier = Modifier) {
    val colors = ButtonDefaults.buttonColors()
    Box(
        modifier =
            modifier
                .clip(ButtonDefaults.shape)
                .background(colors.containerColor)
                .padding(ButtonDefaults.ContentPadding)
    ) {
        Text(color = colors.contentColor, text = text)
    }
}

