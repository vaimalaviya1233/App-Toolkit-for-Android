package com.d4rk.android.apps.apptoolkit.core.di.modules

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.apps.apptoolkit.app.onboarding.utils.interfaces.providers.AppOnboardingProvider
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.app.main.domain.usecases.PerformInAppUpdateUseCase
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.data.client.KtorClient
import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule : Module = module {
    single<DataStore> { DataStore.getInstance(context = get()) }
    single<AdsCoreManager> { AdsCoreManager(context = get() , get()) }
    single { KtorClient().createClient(isDebug = BuildConfig.DEBUG) }

    single<List<String>>(qualifier = named(name = "startup_entries")) {
        get<Context>().resources.getStringArray(R.array.preference_startup_entries).toList()
    }

    single<List<String>>(qualifier = named(name = "startup_values")) {
        get<Context>().resources.getStringArray(R.array.preference_startup_values).toList()
    }

    single<OnboardingProvider> { AppOnboardingProvider() }

    single<AppUpdateManager> { AppUpdateManagerFactory.create(get()) }
    factory { (launcher : ActivityResultLauncher<IntentSenderRequest>) ->
        PerformInAppUpdateUseCase(appUpdateManager = get() , updateResultLauncher = launcher)
    }

    viewModel { (launcher : ActivityResultLauncher<IntentSenderRequest>) ->
        MainViewModel(performInAppUpdateUseCase = get { parametersOf(launcher) } , dispatcherProvider = get())
    }

    single { FetchDeveloperAppsUseCase(client = get()) }
    viewModel {
        AppsListViewModel(
            fetchDeveloperAppsUseCase = get(),
            dispatcherProvider = get(),
            dataStore = get()
        )
    }
    viewModel {
        FavoriteAppsViewModel(
            fetchDeveloperAppsUseCase = get(),
            dataStore = get(),
            dispatcherProvider = get()
        )
    }
}