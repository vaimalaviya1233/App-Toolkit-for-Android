package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
        val bindAd = object : Runnable {
            override fun run() {
                val hasHeadline = nativeAdView.headlineView != null
                val hasCta = nativeAdView.callToActionView != null
                val hasChoices = nativeAdView.adChoicesView != null
                // Optional assets such as body, icon or media may not be present in every layout or
                // ad creative. Requiring them prevents [setNativeAd] from ever being invoked when
                // they are missing, which results in non-clickable ads. Only the mandatory views
                // need to be ready before binding the ad.
                val ready = hasHeadline && hasCta && hasChoices
                if (ready) {
                    val cta = nativeAdView.callToActionView
                    if (cta != null && cta.width > 0 && cta.height > 0) {
                        Log.d(TAG, "cta bounds ${cta.width}x${cta.height}")
                        //Log.d(TAG, "before bind ad.isDestroyed=${nativeAd.isDestroyed}")
                        nativeAdView.setNativeAd(nativeAd)
                        Log.d(TAG, "setNativeAd invoked hasClick=${nativeAdView.hasOnClickListeners()}")
                    } else {
                        nativeAdView.post(this)
                    }
                } else {
                    nativeAdView.post(this)
                }
            }
        }
        nativeAdView.post(bindAd)
        onDispose {
            nativeAdView.removeCallbacks(bindAd)
            //Log.d(TAG, "disposing, ad.isDestroyed=${nativeAd.isDestroyed}")
            nativeAd.destroy()
            //Log.d(TAG, "destroyed, ad.isDestroyed=${nativeAd.isDestroyed}")
        }
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
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
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
    content: @Composable () -> Unit,
) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            adView.callToActionView = composeView
            composeView.setOnTouchListener { _, event ->
                Log.d(TAG, "cta onTouch action=${event.action}")
                false
            }
            Log.d(TAG, "callToActionView registered")
            composeView.setContent {
                val interaction = remember { MutableInteractionSource() }
                Box(modifier = Modifier.clickable(interactionSource = interaction, indication = null) {
                    Log.d(TAG, "cta performClick via wrapper")
                    composeView.performClick()
                }) {
                    content()
                }
            }
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
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            adView.headlineView = composeView
            composeView.setOnTouchListener { _, event ->
                Log.d(TAG, "headline onTouch action=${event.action}")
                false
            }
            Log.d(TAG, "headlineView registered")
            composeView.setContent {
                val interaction = remember { MutableInteractionSource() }
                Box(modifier = Modifier.clickable(interactionSource = interaction, indication = null) {
                    Log.d(TAG, "headline performClick via wrapper")
                    composeView.performClick()
                }) {
                    content()
                }
            }
        },
        modifier = modifier,
    )
}

/** Container for the icon asset inside a native ad view. */
@Composable
fun NativeAdIconView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val adView = LocalNativeAdView.current ?: error("NativeAdView null")
    val context = LocalContext.current
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            adView.iconView = composeView
            composeView.setOnTouchListener { _, event ->
                Log.d(TAG, "icon onTouch action=${event.action}")
                false
            }
            Log.d(TAG, "iconView registered")
            composeView.setContent {
                val interaction = remember { MutableInteractionSource() }
                Box(modifier = Modifier.clickable(interactionSource = interaction, indication = null) {
                    Log.d(TAG, "icon performClick via wrapper")
                    composeView.performClick()
                }) {
                    content()
                }
            }
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
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
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
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
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
    val composeView = remember { ComposeView(context).apply { id = View.generateViewId() } }
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

