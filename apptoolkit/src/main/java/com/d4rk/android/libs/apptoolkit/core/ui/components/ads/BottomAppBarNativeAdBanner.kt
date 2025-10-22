package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.bindArticleNativeAd
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.dp
import com.d4rk.android.libs.apptoolkit.core.utils.ads.NativeAdManager
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    var nativeAd by remember(adsConfig.bannerAdUnitId) { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(adsConfig.bannerAdUnitId , NativeAdManager.adQueue.size) {
        if (nativeAd == null) {
            nativeAd = NativeAdManager.adQueue.firstOrNull()?.also {
                NativeAdManager
            }
        }
        NativeAdManager.loadNativeAds(
            context = context ,
            unitId = adsConfig.bannerAdUnitId
        )
    }

    DisposableEffect(nativeAd) {
        onDispose { nativeAd?.destroy() }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth() ,
        factory = { ctx -> buildArticleNativeAdView(ctx) } ,
        update = { view ->
            nativeAd?.let { bindArticleNativeAd(view , it) }
        }
    )
}

private fun buildArticleNativeAdView(ctx: Context): NativeAdView {
    val resources = ctx.resources
    val nativeAdView = NativeAdView(ctx)

    // dims
    val horizontalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_horizontal_padding)
    val verticalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_vertical_padding)
    val elementSpacing = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_spacing)
    val iconSize = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_icon_size)
    val buttonHorizontalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_button_horizontal_padding)

    // colors from theme attrs
    val colorSurfaceContainer = MaterialColors.getColor(ctx , com.google.android.material.R.attr.colorSurfaceContainer , 0)
    val colorOutline = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOutline, 0)
    val colorOnSurface = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOnSurface, 0)

    // root row
    val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setBackgroundColor(colorSurfaceContainer)
        minimumHeight = dp(ctx , 56)
        gravity = Gravity.CENTER_VERTICAL
        setPaddingRelative(horizontalPadding , verticalPadding , horizontalPadding , verticalPadding)
    }

    // "Ad" label (not registered to NativeAdView – must not be clickable)
    val label = MaterialTextView(ctx).apply {
        id = View.generateViewId()
        setTextAppearance(ctx, com.google.android.material.R.style.TextAppearance_Material3_LabelSmall)
        setText(R.string.ad)
        setBackgroundResource(R.drawable.native_ad_label_background) // contains padding
        setPaddingRelative(buttonHorizontalPadding , dp(ctx , 4) , buttonHorizontalPadding , dp(ctx , 4))
    }

    // AdChoices
    val adChoices = AdChoicesView(ctx).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginStart = elementSpacing
        }
    }

    // Icon
    val icon = ShapeableImageView(ctx).apply {
        id = View.generateViewId()
        layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
            marginStart = elementSpacing
        }
        contentDescription = ctx.getString(R.string.ad)
        visibility = View.GONE
        // match ShapeAppearance.Material3.Corner.Small (approx 8dp)
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(dp(ctx , 8).toFloat()).build()
        strokeColor = android.content.res.ColorStateList.valueOf(colorOutline)
        strokeWidth = dp(ctx , 1).toFloat()
    }

    // Headline
    val headline = MaterialTextView(ctx).apply {
        id = View.generateViewId()
        setTextAppearance(ctx, com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
        setTextColor(colorOnSurface)
        setTypeface(typeface , Typeface.BOLD)
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        visibility = View.GONE
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, /*weight*/1f).apply {
            marginStart = elementSpacing
        }
    }

    // CTA
    val callToActionView = MaterialButton(ctx).apply {
        id = View.generateViewId()
        setTextAppearance(ctx, com.google.android.material.R.style.TextAppearance_Material3_LabelLarge)
        isAllCaps = false
        maxLines = 1
        ellipsize = android.text.TextUtils.TruncateAt.END
        setPaddingRelative(buttonHorizontalPadding , 0 , buttonHorizontalPadding , 0)
        visibility = View.GONE
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            marginStart = elementSpacing
        }
    }

    // order matters: label, adChoices, icon, headline (weight 1), CTA
    root.addView(label)
    root.addView(adChoices)
    root.addView(icon)
    root.addView(headline)
    root.addView(callToActionView)

    nativeAdView.addView(root)

    // Wire assets to NativeAdView
    nativeAdView.adChoicesView = adChoices
    nativeAdView.iconView = icon
    nativeAdView.headlineView = headline
    nativeAdView.callToActionView = callToActionView
    // (No body/media in this banner spec)

    return nativeAdView
}