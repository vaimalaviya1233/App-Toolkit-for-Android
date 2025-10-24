package com.d4rk.android.apps.apptoolkit.core.di.modules

import com.d4rk.android.apps.apptoolkit.core.utils.constants.ads.AdsConstants
import com.d4rk.android.libs.apptoolkit.app.ads.data.DefaultAdsSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.ads.domain.repository.AdsSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.ads.ui.AdsSettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import com.google.android.gms.ads.AdSize
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val adsModule : Module = module {

    single<AdsSettingsRepository> {
        DefaultAdsSettingsRepository(
            dataStore = CommonDataStore.getInstance(get()),
            buildInfoProvider = get<BuildInfoProvider>(),
            dispatchers = get()
        )
    }

    viewModel {
        AdsSettingsViewModel(repository = get())
    }

    single<AdsConfig>(named(name = "native_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.NATIVE_AD_UNIT_ID)
    }

    single<AdsConfig>(named(name = "apps_list_native_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.NATIVE_AD_UNIT_ID)
    }

    single<AdsConfig>(named(name = "no_data_native_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.NATIVE_AD_UNIT_ID)
    }

    single<AdsConfig>(named(name = "bottom_nav_bar_native_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.NATIVE_AD_UNIT_ID)
    }

    single<AdsConfig>(named(name = "help_large_banner_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.HELP_LARGE_BANNER_AD_UNIT_ID , adSize = AdSize.LARGE_BANNER)
    }

    single<AdsConfig>(named(name = "support_banner_ad")) {
        AdsConfig(bannerAdUnitId = AdsConstants.SUPPORT_MEDIUM_RECTANGLE_BANNER_AD_UNIT_ID , adSize = AdSize.MEDIUM_RECTANGLE)
    }
}