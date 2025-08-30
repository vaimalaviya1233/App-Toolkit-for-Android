package com.d4rk.android.apps.apptoolkit.core.di.modules

import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppAboutSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppAdvancedSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppBuildInfoProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppDisplaySettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppPrivacySettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.PermissionsSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.about.data.DefaultAboutRepository
import com.d4rk.android.libs.apptoolkit.app.about.domain.repository.AboutRepository
import com.d4rk.android.libs.apptoolkit.app.about.ui.AboutViewModel
import com.d4rk.android.libs.apptoolkit.app.advanced.data.CacheRepository
import com.d4rk.android.libs.apptoolkit.app.advanced.data.DefaultCacheRepository
import com.d4rk.android.libs.apptoolkit.app.advanced.ui.AdvancedSettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.diagnostics.ui.UsageAndDiagnosticsViewModel
import com.d4rk.android.libs.apptoolkit.app.permissions.ui.PermissionsViewModel
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.repository.PermissionsRepository
import com.d4rk.android.libs.apptoolkit.app.settings.general.ui.GeneralSettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.general.data.DefaultGeneralSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.settings.general.domain.repository.GeneralSettingsRepository
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AdvancedSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.DisplaySettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.GeneralSettingsContentProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.PrivacySettingsProvider
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsProvider> { AppSettingsProvider() }

    viewModel {
        SettingsViewModel(
            settingsProvider = get(),
            dispatcher = get(named("io"))
        )
    }

    single<AboutSettingsProvider> { AppAboutSettingsProvider(context = get()) }
    single<AdvancedSettingsProvider> { AppAdvancedSettingsProvider(context = get()) }
    single<DisplaySettingsProvider> { AppDisplaySettingsProvider(context = get()) }
    single<PrivacySettingsProvider> { AppPrivacySettingsProvider(context = get()) }
    single<BuildInfoProvider> { AppBuildInfoProvider(context = get()) }
    single<GeneralSettingsContentProvider> { GeneralSettingsContentProvider(displayProvider = get(), privacyProvider = get()) }
    single<CacheRepository> { DefaultCacheRepository(context = get(), ioDispatcher = get(named("io"))) }
    single<AboutRepository> {
        DefaultAboutRepository(
            deviceProvider = get(),
            configProvider = get(),
            context = get(),
            ioDispatcher = get(named("io")),
            mainDispatcher = get(named("main")),
        )
    }
    single<GeneralSettingsRepository> {
        DefaultGeneralSettingsRepository(dispatcher = get(named("default")))
    }
    viewModel {
        GeneralSettingsViewModel(repository = get())
    }

    single<PermissionsRepository> { PermissionsSettingsRepository(context = get(), dispatcher = get(named("io"))) }
    viewModel {
        PermissionsViewModel(
            permissionsRepository = get(),
        )
    }

    viewModel { AdvancedSettingsViewModel(repository = get()) }

    viewModel {
        AboutViewModel(
            repository = get(),
        )
    }

    viewModel {
        UsageAndDiagnosticsViewModel(
            dataStore = CommonDataStore.getInstance(get()),
            configProvider = get(),
            dispatcher = get(named("io")),
        )
    }
}
