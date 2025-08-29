package com.d4rk.android.apps.apptoolkit.core.di.modules

import android.content.Context
import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.apps.apptoolkit.app.apps.list.data.repository.DeveloperAppsRepositoryImpl
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.repository.DeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.app.main.data.repository.MainRepositoryImpl
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.apps.apptoolkit.app.onboarding.utils.interfaces.providers.AppOnboardingProvider
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.apps.apptoolkit.core.data.favorites.FavoritesRepositoryImpl
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.libs.apptoolkit.app.onboarding.utils.interfaces.providers.OnboardingProvider
import com.d4rk.android.libs.apptoolkit.data.client.KtorClient
import com.d4rk.android.libs.apptoolkit.data.core.ads.AdsCoreManager
import com.d4rk.android.apps.apptoolkit.app.main.domain.repository.MainRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule : Module = module {
    single<CoroutineDispatcher>(qualifier = named("io")) { Dispatchers.IO }
    single<CoroutineDispatcher>(qualifier = named("default")) { Dispatchers.Default }
    single<CoroutineDispatcher>(qualifier = named("main")) { Dispatchers.Main }
    single<DataStore> { DataStore(context = get()) }
    single<AdsCoreManager> { AdsCoreManager(context = get(), buildInfoProvider = get(), ioDispatcher = get(named("io"))) }
    single { KtorClient().createClient(enableLogging = BuildConfig.DEBUG) }

    single<FavoritesRepository> { FavoritesRepositoryImpl(context = get(), dataStore = get()) }
    single { ObserveFavoritesUseCase(repository = get()) }
    single { ToggleFavoriteUseCase(repository = get(), dispatcher = get(named("io"))) }

    single<List<String>>(qualifier = named(name = "startup_entries")) {
        get<Context>().resources.getStringArray(R.array.preference_startup_entries).toList()
    }

    single<List<String>>(qualifier = named(name = "startup_values")) {
        get<Context>().resources.getStringArray(R.array.preference_startup_values).toList()
    }

    single<OnboardingProvider> { AppOnboardingProvider() }

    single<MainRepository> { MainRepositoryImpl(ioDispatcher = get(named("io"))) }

    viewModel { MainViewModel(repository = get()) }

    single<DeveloperAppsRepository> { DeveloperAppsRepositoryImpl(client = get()) }
    single { FetchDeveloperAppsUseCase(repository = get()) }
    viewModel {
        AppsListViewModel(
            fetchDeveloperAppsUseCase = get(),
            observeFavoritesUseCase = get(),
            toggleFavoriteUseCase = get()
        )
    }
    viewModel {
        FavoriteAppsViewModel(
            fetchDeveloperAppsUseCase = get(),
            observeFavoritesUseCase = get(),
            toggleFavoriteUseCase = get()
        )
    }
}

