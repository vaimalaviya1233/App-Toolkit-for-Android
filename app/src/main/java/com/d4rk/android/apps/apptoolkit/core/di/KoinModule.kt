package com.d4rk.android.apps.apptoolkit.core.di

import android.content.Context
import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppSettingsProvider
import com.d4rk.android.apps.apptoolkit.core.utils.constants.ads.AdsConstants
import com.d4rk.android.libs.apptoolkit.app.help.domain.model.ui.HelpScreenConfig
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.GetFAQsUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.LaunchReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.app.help.domain.usecases.RequestReviewFlowUseCase
import com.d4rk.android.libs.apptoolkit.app.help.ui.HelpViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.support.domain.usecases.QuerySkuDetailsUseCase
import com.d4rk.android.libs.apptoolkit.app.support.ui.SupportViewModel
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.di.StandardDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.ads.AdsConfig
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import com.d4rk.cartcalculator.core.data.datastore.DataStore
import com.google.android.gms.ads.AdSize
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dispatchersModule : Module = module {
    single<DispatcherProvider> { StandardDispatchers() }
}

/*val providersModule : Module = module {
    single<AppAboutSettingsProvider> { AppAboutSettingsProvider(context = get()) }
    single<AppAdvancedSettingsProvider> { AppAdvancedSettingsProvider(context = get()) }
    single<AppDisplaySettingsProvider> { AppDisplaySettingsProvider(context = get()) }
    single<AppPrivacySettingsProvider> { AppPrivacySettingsProvider(context = get()) }
}*/

val settingsModule = module {
    single<SettingsProvider> { AppSettingsProvider() }

    viewModel {
        SettingsViewModel(settingsProvider = get(), dispatcherProvider = get())
    }
}

val appModule : Module = module {
    single<DataStore> { DataStore.getInstance(context = get()) }
    single<AdsCoreManager> { AdsCoreManager(context = get()) }

    viewModel {
        MainViewModel()
    }
}

val adsModule : Module = module {
    single<AdsConfig> { AdsConfig(bannerAdUnitId = AdsConstants.BANNER_AD_UNIT_ID , adSize = AdSize.LARGE_BANNER) }
}

val appToolkitModule : Module = module {
    single<QuerySkuDetailsUseCase> { QuerySkuDetailsUseCase() }
    viewModel { SupportViewModel(querySkuDetailsUseCase = get() , dispatcherProvider = get()) }

    single<HelpScreenConfig> { HelpScreenConfig(versionName = BuildConfig.VERSION_NAME , versionCode = BuildConfig.VERSION_CODE) }
    single<GetFAQsUseCase> { GetFAQsUseCase(application = get()) }
    single<RequestReviewFlowUseCase> { RequestReviewFlowUseCase(application = get()) }
    single<LaunchReviewFlowUseCase> { LaunchReviewFlowUseCase() }

    viewModel {
        HelpViewModel(getFAQsUseCase = get() , requestReviewFlowUseCase = get() , launchReviewFlowUseCase = get() , dispatcherProvider = get())
    }
}

fun initializeKoin(context : Context) {
    startKoin {
        androidContext(androidContext = context)
        modules(modules = listOf(dispatchersModule, appModule , settingsModule , adsModule , appToolkitModule))
    }
}