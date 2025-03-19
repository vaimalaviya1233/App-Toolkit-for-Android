package com.d4rk.android.apps.apptoolkit.core.di.modules

import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppAboutSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppAdvancedSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppDisplaySettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppPrivacySettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppSettingsProvider
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.AppUsageAndDiagnosticsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.general.ui.GeneralSettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsViewModel
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AdvancedSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.DisplaySettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.GeneralSettingsContentProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.PrivacySettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.UsageAndDiagnosticsSettingsProvider
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    single<SettingsProvider> { AppSettingsProvider() }

    single<AboutSettingsProvider> { AppAboutSettingsProvider(get()) }
    single<AdvancedSettingsProvider> { AppAdvancedSettingsProvider(get()) }
    single<DisplaySettingsProvider> { AppDisplaySettingsProvider(get()) }
    single<PrivacySettingsProvider> { AppPrivacySettingsProvider(get()) }
    single<UsageAndDiagnosticsSettingsProvider> { AppUsageAndDiagnosticsProvider() }

    single {
        GeneralSettingsContentProvider(
            aboutProvider = get(),
            advancedProvider = get(),
            displayProvider = get(),
            privacyProvider = get(),
            usageProvider = get(),
        )
    }

    viewModel {
        GeneralSettingsViewModel()
    }

    viewModel {
        SettingsViewModel(settingsProvider = get(), dispatcherProvider = get())
    }
}