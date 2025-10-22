package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.debugNativeAds
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ADCHOICES_CONTAINER_VIEW
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ADVERTISER
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_CALL_TO_ACTION
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_HEADLINE
import com.google.android.gms.ads.nativead.NativeAdAssetNames.ASSET_ICON
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * A CompositionLocal that can provide a `NativeAdView` to ad attributes such as `NativeHeadline`.
 */
internal val LocalNativeAdView = staticCompositionLocalOf<NativeAdView?> { null }

/**
 * This is the Compose wrapper for a NativeAdView.
 *
 * @param nativeAd The `NativeAd` object containing the ad assets to be displayed in this view.
 * @param modifier The modifier to apply to the native ad.
 * @param content A composable function that defines the rest of the native ad view's elements.
 */
@Composable
fun NativeAdView(nativeAd: NativeAd, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val localContext = LocalContext.current
    val contentState = rememberUpdatedState(content)
    val nativeAdView : NativeAdView =
        remember(localContext) {
            NativeAdView(localContext).apply {
                id = View.generateViewId()
                prepareNativeClickableAsset()
            }
        }
    val composeContentView =
        remember(localContext) {
            ComposeView(localContext).apply {
                id = View.generateViewId()
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            }
        }
    val relayoutRunnable =
        remember(nativeAdView) {
            Runnable {
                nativeAdView.requestLayout()
                nativeAdView.invalidate()
            }
        }
    val boundNativeAd = remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(nativeAdView) {
        onDispose {
            boundNativeAd.value = null
            nativeAdView.removeCallbacks(relayoutRunnable)
            composeContentView.disposeComposition()
            nativeAdView.removeAllViews()
        }
    }

    AndroidView(
        factory = {
            nativeAdView.apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )

                if (composeContentView.parent != this) {
                    (composeContentView.parent as? ViewGroup)?.removeView(composeContentView)
                    addView(composeContentView)
                }

                composeContentView.setContent {
                    CompositionLocalProvider(LocalNativeAdView provides this) {
                        contentState.value()
                    }
                }
            }
        },
        modifier = modifier,
        update = { view ->
            if (composeContentView.parent != view) {
                (composeContentView.parent as? ViewGroup)?.removeView(composeContentView)
                view.addView(composeContentView)
            }

            if (boundNativeAd.value !== nativeAd) {
                boundNativeAd.value = nativeAd
                view.removeCallbacks(relayoutRunnable)
                view.dispatchNativeLayoutPass(relayoutRunnable)
            }
        },
    )

    SideEffect {
        val headlineView = nativeAdView.headlineView
        val callToActionView = nativeAdView.callToActionView
        val iconView = nativeAdView.iconView
        val advertiserView = nativeAdView.advertiserView
        val adChoicesView = nativeAdView.adChoicesView

        val clickableAssetViews = mutableMapOf<String, View>()
        headlineView?.takeIf { it.isVisible }
            ?.let { clickableAssetViews[ASSET_HEADLINE] = it }
        callToActionView?.takeIf { it.isVisible }
            ?.let { clickableAssetViews[ASSET_CALL_TO_ACTION] = it }
        iconView?.takeIf { it.isVisible }
            ?.let { clickableAssetViews[ASSET_ICON] = it }

        val nonClickableAssetViews = mutableMapOf<String, View>()
        advertiserView?.let { nonClickableAssetViews[ASSET_ADVERTISER] = it }
        adChoicesView?.let {
            nonClickableAssetViews[ASSET_ADCHOICES_CONTAINER_VIEW] = it
        }

        val registered = nativeAd.registerNativeAdViewCompat(
            nativeAdView = nativeAdView,
            clickableAssets = clickableAssetViews,
            nonClickableAssets = nonClickableAssetViews,
        )

        if (registered) {
            debugNativeAds(
                "Compose NativeAdView registered clickable=${clickableAssetViews.keys} " + "nonClickable=${nonClickableAssetViews.keys}"
            )
            runCatching { nativeAdView.setNativeAd(nativeAd) }
                .onFailure { error ->
                    debugNativeAds(
                        "Compose NativeAdView setNativeAd post-register failed: ${error.message}"
                    )
                }
        } else {
            debugNativeAds(
                "Compose NativeAdView registerNativeAdViewCompat unavailable - falling back to setNativeAd"
            )
            nativeAdView.setNativeAd(nativeAd)
        }

        ensureNativeFallbackClicks(
            headlineView = headlineView,
            iconView = iconView,
            callToActionView = callToActionView,
        )
        nativeAdView.setClickConfirmingView(callToActionView)
        nativeAdView.tag = nativeAd

        nativeAdView.removeCallbacks(relayoutRunnable)
        nativeAdView.post(relayoutRunnable)
        nativeAdView.postDelayed(relayoutRunnable, 250L)
    }
}

private const val COMPOSE_VIEW_CLASS_SIMPLE_NAME = "AndroidComposeView"

private fun NativeAdView.dispatchNativeLayoutPass(relayoutRunnable: Runnable) {
    removeCallbacks(relayoutRunnable)
    doOnLayout {
        requestNativeRootRelayout()
        post(relayoutRunnable)
        postDelayed(relayoutRunnable, 250L)
    }
}

private fun NativeAdView.requestNativeRootRelayout() {
    requestLayout()
    invalidate()

    (parent as? View)?.let { parentView ->
        parentView.requestLayout()
        parentView.invalidate()
    }

    val composeParent = parent.findAndroidComposeViewParent()
    if (composeParent != null) {
        composeParent.requestLayout()
        composeParent.invalidate()
    } else {
        postDelayed({ parent.findAndroidComposeViewParent()?.requestLayout() }, 400L)
    }

    rootView?.let { root ->
        if (root !== this) {
            root.requestLayout()
            root.invalidate()
        }
    }
}

private tailrec fun ViewParent?.findAndroidComposeViewParent(): ViewGroup? =
    when (this) {
        null -> null
        is ViewGroup ->
            if (this::class.java.simpleName == COMPOSE_VIEW_CLASS_SIMPLE_NAME) {
                this
            } else {
                this.parent.findAndroidComposeViewParent()
            }
        is View -> this.parent.findAndroidComposeViewParent()
        else -> null
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
    val contentState = rememberUpdatedState(content)
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                prepareNativeClickableAsset()
                setContent { contentState.value() }
                nativeAdView.bodyView = this
            }
        },
        modifier = modifier,
        update = { view ->
            nativeAdView.bodyView = view
            view.prepareNativeClickableAsset()
        },
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
    val contentState = rememberUpdatedState(content)
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                prepareNativeClickableAsset()
                setContent { contentState.value() }
                nativeAdView.callToActionView = this
            }
        },
        modifier = modifier,
        update = { view ->
            nativeAdView.callToActionView = view
            view.prepareNativeClickableAsset()
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
                prepareNativeClickableAsset()
                nativeAdView.adChoicesView = this
            }
        },
        update = { view ->
            nativeAdView.adChoicesView = view
            view.prepareNativeClickableAsset()
        },
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
    val contentState = rememberUpdatedState(content)
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                prepareNativeClickableAsset()
                setContent { contentState.value() }
                nativeAdView.headlineView = this
            }
        },
        modifier = modifier,
        update = { view ->
            nativeAdView.headlineView = view
            view.prepareNativeClickableAsset()
        },
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
    val contentState = rememberUpdatedState(content)
    AndroidView(
        factory = { context ->
            ComposeView(context).apply {
                id = View.generateViewId()
                prepareNativeClickableAsset()
                setContent { contentState.value() }
                nativeAdView.iconView = this
            }
        },
        modifier = modifier,
        update = { view ->
            nativeAdView.iconView = view
            view.prepareNativeClickableAsset()
        },
    )
}

private fun View.prepareNativeClickableAsset() {
    isClickable = true
    isFocusable = true
    isFocusableInTouchMode = true
    isEnabled = true
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
        Text(color = ButtonDefaults.buttonColors().contentColor, text = text, maxLines = 1)
    }
}