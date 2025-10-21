package com.d4rk.android.libs.apptoolkit.core.ui.components.ads

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.content.res.use
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd

class NativeAdBannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    @LayoutRes
    private var layoutRes: Int = R.layout.native_ad_bottom_app_bar
    private var adUnitId: String = context.getString(R.string.native_ad_fallback_unit_id)
    private var nativeAd: NativeAd? = null
    private var ownsNativeAd: Boolean = false

    init {
        visibility = GONE
        context.obtainStyledAttributes(attrs, R.styleable.NativeAdBannerView, defStyleAttr, 0)
            .use { typedArray ->
                layoutRes = typedArray.getResourceId(
                    R.styleable.NativeAdBannerView_nativeAdLayout,
                    layoutRes,
                )
                val adUnitValue =
                    typedArray.getString(R.styleable.NativeAdBannerView_nativeAdUnitId)
                if (!adUnitValue.isNullOrBlank()) {
                    adUnitId = adUnitValue
                }
            }
    }

    fun loadAd() {
        loadAd(AdRequest.Builder().build(), null)
    }

    fun loadAd(listener: AdListener?) {
        loadAd(AdRequest.Builder().build(), listener)
    }

    fun loadAd(request: AdRequest) {
        loadAd(request, null)
    }

    fun loadAd(request: AdRequest, listener: AdListener?) {
        NativeAdLoader.load(
            context = context,
            container = this,
            layoutRes = layoutRes,
            adRequest = request,
            listener = listener,
            adUnitId = adUnitId,
            onNativeAdLoaded = { loadedAd ->
                destroyOwnedNativeAdIfNecessary(loadedAd)
                replaceNativeAd(loadedAd, ownsAd = true)
            },
        )
    }

    fun renderNativeAd(nativeAd: NativeAd, @LayoutRes layoutResOverride: Int? = null) {
        val desiredLayout = layoutResOverride ?: layoutRes
        if (this.nativeAd === nativeAd && layoutRes == desiredLayout) {
            return
        }
        destroyOwnedNativeAdIfNecessary(nativeAd)
        NativeAdViewBinder.bind(
            context = context,
            container = this,
            layoutRes = desiredLayout,
            nativeAd = nativeAd,
        )
        replaceNativeAd(nativeAd, ownsAd = false)
    }

    fun clearAd() {
        destroyOwnedNativeAdIfNecessary()
        replaceNativeAd(null, ownsAd = false)
        removeAllViews()
        visibility = GONE
    }

    fun setNativeAdLayout(@LayoutRes layoutRes: Int) {
        if (this.layoutRes == layoutRes) return
        this.layoutRes = layoutRes
        clearAd()
    }

    fun setNativeAdUnitId(adUnitId: String?) {
        val resolved = adUnitId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.native_ad_fallback_unit_id)
        if (this.adUnitId == resolved) return
        clearAd()
        this.adUnitId = resolved
    }

    fun setNativeAdUnitId(@StringRes adUnitIdRes: Int) {
        setNativeAdUnitId(context.getString(adUnitIdRes))
    }

    fun getNativeAdUnitId(): String = adUnitId

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAd()
    }

    private fun destroyOwnedNativeAdIfNecessary(incomingAd: NativeAd? = null) {
        if (ownsNativeAd && nativeAd != null && nativeAd !== incomingAd) {
            runCatching { nativeAd?.destroy() }
        }
    }

    private fun replaceNativeAd(newAd: NativeAd?, ownsAd: Boolean) {
        nativeAd = newAd
        ownsNativeAd = ownsAd && newAd != null
    }
}
