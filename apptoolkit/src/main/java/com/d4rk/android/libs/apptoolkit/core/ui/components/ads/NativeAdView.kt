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
import com.google.android.material.button.MaterialButton
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

@Composable
fun NativeAdView(
    ad: NativeAd,
    adContent: @Composable (ad: NativeAd, ctaView: View, contentView: View) -> Unit,
) {
    val contentViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val adViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val headlineViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val iconViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val bodyViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val ctaViewId by remember { mutableIntStateOf(View.generateViewId()) }
    val context = LocalContext.current
    val adChoicesView = remember { AdChoicesView(context) }
    val ctaView = remember {
        MaterialButton(context).apply {
            id = ctaViewId
            isAllCaps = false
            visibility = View.GONE
        }
    }

    AndroidView(
        factory = { ctx ->
            val contentView = ComposeView(ctx).apply { id = contentViewId }
            val headlineView = ComposeView(ctx).apply {
                id = headlineViewId
                visibility = View.GONE
            }
            val iconView = ComposeView(ctx).apply {
                id = iconViewId
                visibility = View.GONE
            }
            val bodyView = ComposeView(ctx).apply {
                id = bodyViewId
                visibility = View.GONE
            }

            GoogleNativeAdView(ctx).apply {
                id = adViewId
                addView(contentView)
                addView(headlineView)
                addView(iconView)
                addView(bodyView)
                // ctaView will be added via Compose `AndroidView`
            }
        },
        update = { view ->
            val adView = view.findViewById<GoogleNativeAdView>(adViewId)
            val contentView = view.findViewById<ComposeView>(contentViewId)
            val headlineView = view.findViewById<ComposeView>(headlineViewId)
            val iconView = view.findViewById<ComposeView>(iconViewId)
            val bodyView = view.findViewById<ComposeView>(bodyViewId)

            adView.headlineView = headlineView
            adView.iconView = iconView
            adView.bodyView = bodyView
            adView.callToActionView = ctaView
            adView.adChoicesView = adChoicesView

            if (adView.tag != ad) {
                val bindAd = {
                    adView.setNativeAd(ad)
                    adView.tag = ad
                }

                if (ctaView.isAttachedToWindow) {
                    bindAd()
                } else {
                    ctaView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                        override fun onViewAttachedToWindow(v: View) {
                            bindAd()
                            ctaView.removeOnAttachStateChangeListener(this)
                        }

                        override fun onViewDetachedFromWindow(v: View) = Unit
                    })
                }
            }

            contentView.setContent {
                Box {
                    adContent(ad, ctaView, contentView)
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

