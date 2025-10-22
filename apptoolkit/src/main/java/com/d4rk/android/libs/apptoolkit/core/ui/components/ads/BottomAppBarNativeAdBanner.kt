package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.flow.filter

@Composable
fun BottomAppBarNativeAdBanner(
    modifier: Modifier = Modifier,
    adsConfig: AdsConfig,
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        NativeAdManager.loadNativeAds(
            context = context,
            unitId = adsConfig.bannerAdUnitId
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { NativeAdManager.adQueue.size }
                .filter { it > 0 && nativeAd == null }
                .collect {
                    nativeAd = NativeAdManager.adQueue.removeAt(0)
                }
    }

    DisposableEffect(nativeAd) {
        onDispose { nativeAd?.destroy() }
    }

    val currentAd = nativeAd

    currentAd?.let { ad ->
        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx -> buildBottomAppBarNativeAdView(ctx) },
            update = { view ->
                view.visibility = View.VISIBLE
                bindArticleNativeAd(view, ad)
            }
        )
    } ?: run {
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 2.dp
        ) {
            val horizontalPadding = dimensionResource(id = R.dimen.native_ad_bottom_bar_horizontal_padding)
            val verticalPadding = dimensionResource(id = R.dimen.native_ad_bottom_bar_vertical_padding)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(
                        start = horizontalPadding,
                        top = verticalPadding,
                        end = horizontalPadding,
                        bottom = verticalPadding
                    ),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun buildBottomAppBarNativeAdView(ctx: Context): NativeAdView {
    val resources = ctx.resources
    val nativeAdView = NativeAdView(ctx)

    nativeAdView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    val horizontalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_horizontal_padding)
    val verticalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_vertical_padding)
    val elementSpacing = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_spacing)
    val iconSize = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_icon_size)
    val buttonHorizontalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_button_horizontal_padding)
    val labelHorizontalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_label_horizontal_padding)
    val labelVerticalPadding = resources.getDimensionPixelSize(R.dimen.native_ad_bottom_bar_label_vertical_padding)
    val iconCornerRadius = resources.getDimension(R.dimen.native_ad_bottom_bar_icon_corner_radius)

    val colorSurfaceContainer = MaterialColors.getColor(ctx , com.google.android.material.R.attr.colorSurfaceContainer , 0)
    val colorOutline = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOutline, 0)
    val colorOnSurface = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorOnSurface, 0)

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
        setPaddingRelative(labelHorizontalPadding, labelVerticalPadding, labelHorizontalPadding, labelVerticalPadding)
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
        shapeAppearanceModel = shapeAppearanceModel.toBuilder().setAllCornerSizes(iconCornerRadius).build()
        strokeColor = ColorStateList.valueOf(colorOutline)
        strokeWidth = dp(ctx , 1).toFloat()
        scaleType = ImageView.ScaleType.CENTER_CROP
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