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
        debugNativeAds("NativeAdBannerView initialized with default layout=$layoutRes and adUnitId=$adUnitId")
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
        debugNativeAds("NativeAdBannerView attribute initialization layout=$layoutRes adUnitId=$adUnitId")
    }

    fun loadAd() {
        debugNativeAds("NativeAdBannerView loadAd() invoked with default AdRequest")
        loadAd(AdRequest.Builder().build(), null)
    }

    fun loadAd(listener: AdListener?) {
        debugNativeAds("NativeAdBannerView loadAd(listener) invoked")
        loadAd(AdRequest.Builder().build(), listener)
    }

    fun loadAd(request: AdRequest) {
        debugNativeAds("NativeAdBannerView loadAd(request) invoked")
        loadAd(request, null)
    }

    fun loadAd(request: AdRequest, listener: AdListener?) {
        debugNativeAds("NativeAdBannerView starting ad request for unitId=$adUnitId layout=$layoutRes")
        NativeAdLoader.load(
            context = context,
            container = this,
            layoutRes = layoutRes,
            adRequest = request,
            listener = listener,
            adUnitId = adUnitId,
            onNativeAdLoaded = { loadedAd ->
                debugNativeAds("NativeAdBannerView received native ad ${loadedAd.hashCode()}")
                destroyOwnedNativeAdIfNecessary(loadedAd)
                replaceNativeAd(loadedAd, ownsAd = true)
            },
        )
    }

    fun renderNativeAd(nativeAd: NativeAd, @LayoutRes layoutResOverride: Int? = null) {
        val desiredLayout = layoutResOverride ?: layoutRes
        if (this.nativeAd === nativeAd && layoutRes == desiredLayout) {
            debugNativeAds("NativeAdBannerView renderNativeAd skipped - ad already rendered")
            return
        }
        debugNativeAds("NativeAdBannerView rendering provided native ad ${nativeAd.hashCode()} with layout=$desiredLayout")
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
        debugNativeAds("NativeAdBannerView clearing current native ad")
        destroyOwnedNativeAdIfNecessary()
        replaceNativeAd(null, ownsAd = false)
        removeAllViews()
        visibility = GONE
    }

    fun setNativeAdLayout(@LayoutRes layoutRes: Int) {
        if (this.layoutRes == layoutRes) return
        this.layoutRes = layoutRes
        debugNativeAds("NativeAdBannerView layout changed to $layoutRes. Clearing ad to rebind")
        clearAd()
    }

    fun setNativeAdUnitId(adUnitId: String?) {
        val resolved = adUnitId.takeUnless { it.isNullOrBlank() }
            ?: context.getString(R.string.native_ad_fallback_unit_id)
        if (this.adUnitId == resolved) return
        debugNativeAds("NativeAdBannerView adUnitId changed from ${this.adUnitId} to $resolved")
        clearAd()
        this.adUnitId = resolved
    }

    fun setNativeAdUnitId(@StringRes adUnitIdRes: Int) {
        setNativeAdUnitId(context.getString(adUnitIdRes))
    }

    fun getNativeAdUnitId(): String = adUnitId

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        debugNativeAds("NativeAdBannerView detached from window - clearing ad")
        clearAd()
    }

    private fun destroyOwnedNativeAdIfNecessary(incomingAd: NativeAd? = null) {
        if (ownsNativeAd && nativeAd != null && nativeAd !== incomingAd) {
            debugNativeAds("NativeAdBannerView destroying owned native ad ${nativeAd?.hashCode()}")
            runCatching { nativeAd?.destroy() }
        }
    }

    private fun replaceNativeAd(newAd: NativeAd?, ownsAd: Boolean) {
        debugNativeAds("NativeAdBannerView replaceNativeAd with newAd=${newAd?.hashCode()} ownsAd=$ownsAd")
        nativeAd = newAd
        ownsNativeAd = ownsAd && newAd != null
    }
}
