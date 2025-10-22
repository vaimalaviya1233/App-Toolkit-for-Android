package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.ConstraintLayout
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.bindArticleNativeAd
import com.d4rk.android.libs.apptoolkit.core.ui.utils.ads.helpers.dp
import com.d4rk.android.libs.apptoolkit.core.utils.ads.NativeAdManager
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
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
            factory = { ctx -> buildArticleNativeAdView(ctx) },
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

private fun buildArticleNativeAdView(ctx: Context): NativeAdView {
    val container = NativeAdView(ctx)

    val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val pad = dp(ctx , 12)
        setPadding(pad, pad, pad, pad)
        gravity = Gravity.CENTER_VERTICAL
    }

    val left = ConstraintLayout(ctx).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, /*weight*/1f)
    }

    val mediaCard = MaterialCardView(ctx).apply {
        id = View.generateViewId()
        radius = dp(ctx , 16).toFloat()
        cardElevation = 0f
        useCompatPadding = false
        preventCornerOverlap = true
        layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            dimensionRatio = "16:9"
        }
    }

    val media = MediaView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    mediaCard.addView(media)
    left.addView(mediaCard)

    val centerCol = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, /*weight*/2f).apply {
            leftMargin = dp(ctx , 12)
            rightMargin = dp(ctx , 12)
        }
    }

    val headline = TextView(ctx).apply {
        id = View.generateViewId()
        setTextAppearance(android.R.style.TextAppearance_Material_Subhead)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    val body = TextView(ctx).apply {
        id = View.generateViewId()
        setTextAppearance(android.R.style.TextAppearance_Material_Display1)
        maxLines = 3
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(ctx , 6) }
    }

    centerCol.addView(headline)
    centerCol.addView(body)

    val rightCol = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    val icon = ImageView(ctx).apply {
        id = View.generateViewId()
        layoutParams = FrameLayout.LayoutParams(
            dp(ctx , 52) , dp(ctx , 52) ,
            Gravity.CENTER
        )
        scaleType = ImageView.ScaleType.CENTER_CROP
        adjustViewBounds = true
        clipToOutline = true
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
        }
    }

    val cta = MaterialButton(ctx).apply {
        id = View.generateViewId()
        isAllCaps = false
        insetTop = 0; insetBottom = 0
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = dp(ctx , 12) }
    }

    rightCol.addView(cta)

    root.addView(left)
    root.addView(centerCol)
    root.addView(rightCol)
    container.addView(root)

    container.mediaView = media
    container.headlineView = headline
    container.bodyView = body
    container.iconView = icon
    container.callToActionView = cta

    return container
}