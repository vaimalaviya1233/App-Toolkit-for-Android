package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * A CompositionLocal that can provide a `NativeAdView` to ad attributes such as `NativeHeadline`.
 */
internal val LocalNativeAdView = staticCompositionLocalOf<NativeAdView?> { null }

/**
 * This is the Compose wrapper for a NativeAdView.
 *
 * @param modifier The modifier to apply to the native ad.
 * @param content A composable function that defines the rest of the native ad view's elements.
 */
@Composable
fun NativeAdView(nativeAd: NativeAd, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val localContext = LocalContext.current
    val nativeAdView = remember { NativeAdView(localContext).apply { id = View.generateViewId() } }

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
                            CompositionLocalProvider(LocalNativeAdView provides nativeAdView) { content() }
                        }
                    }
                )
            }
        },
        modifier = modifier,
    )

    LaunchedEffect(nativeAd) {
        // Ensure child views are in place before binding the ad
        withFrameNanos { }
        nativeAdView.setNativeAd(nativeAd)
    }
}

/**
 * The ComposeWrapper container for an advertiserView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdAdvertiserView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                setContent(content)
                nativeAdView.advertiserView = this
            }
        },
        modifier = modifier,
        update = { view -> view.setContent(content) },
    )
}

/**
 * The ComposeWrapper container for a bodyView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdBodyView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.bodyView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a callToActionView inside a NativeAdView. This composable must
 * be invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdCallToActionView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ComposeView(context).apply { id = View.generateViewId() }
        },
        update = { view ->
            nativeAdView.callToActionView = view
            view.setContent(content)
        },
    )
}

/**
 * The ComposeWrapper for a adChoicesView inside a NativeAdView. This composable must be invoked
 * from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 */
@Composable
fun NativeAdChoicesView(modifier: Modifier = Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = {
            AdChoicesView(localContext).apply {
                minimumWidth = 15
                minimumHeight = 15
            }
        },
        update = { view -> nativeAdView.adChoicesView = view },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a headlineView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdHeadlineView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.headlineView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a iconView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdIconView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.iconView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper for a mediaView inside a NativeAdView. This composable must be invoked from
 * within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 */
@Composable
fun NativeAdMediaView(modifier: Modifier = Modifier) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    AndroidView(
        factory = { MediaView(localContext) },
        update = { view -> nativeAdView.mediaView = view },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a priceView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdPriceView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.priceView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a starRatingView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdStarRatingView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.starRatingView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The ComposeWrapper container for a storeView inside a NativeAdView. This composable must be
 * invoked from within a `NativeAdView`.
 *
 * @param modifier modify the native ad view element.
 * @param content A composable function that defines the content of this native asset.
 */
@Composable
fun NativeAdStoreView(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val nativeAdView = LocalNativeAdView.current ?: throw IllegalStateException("NativeAdView null")
    val localContext = LocalContext.current
    val localComposeView = remember { ComposeView(localContext).apply { id = View.generateViewId() } }
    AndroidView(
        factory = {
            nativeAdView.storeView = localComposeView
            localComposeView.apply { setContent(content) }
        },
        modifier = modifier,
    )
}

/**
 * The composable for a ad attribution inside a NativeAdView. This composable must be invoked from
 * within a `NativeAdView`.
 *
 * @param text The string identifying this view as an advertisement.
 * @param modifier modify the native ad view element.
 */
@Composable
fun NativeAdAttribution(modifier: Modifier = Modifier, text: String = stringResource(R.string.ad)) {
    Box(
        modifier =
            modifier
                .border(
                    width = 1.dp,
                    color = ButtonDefaults.buttonColors().containerColor,
                    shape = RoundedCornerShape(SizeConstants.ExtraSmallSize)
                )
                .clip(RoundedCornerShape(SizeConstants.ExtraSmallSize))
    ) {
        Text(
            modifier = Modifier.padding(horizontal = SizeConstants.ExtraTinySize),
            color = ButtonDefaults.buttonColors().containerColor,
            text = text,
        )
    }
}

/**
 * The composable for a button inside a NativeAdView. This composable must be invoked from within a
 * NativeAdView.
 *
 * The Jetpack Compose button implements a click handler which overrides the native ad click
 * handler, causing issues. The NativeAdButton does not implement a click handler. To handle native
 * ad clicks, use the NativeAd AdListener onAdClicked callback.
 *
 * @param text The string identifying this view as an advertisement.
 * @param modifier modify the native ad view element.
 */
@Composable
fun NativeAdButton(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(ButtonDefaults.shape)
                .background(ButtonDefaults.buttonColors().containerColor)
                .padding(ButtonDefaults.ContentPadding)
    ) {
        Text(color = ButtonDefaults.buttonColors().contentColor, text = text)
    }
}