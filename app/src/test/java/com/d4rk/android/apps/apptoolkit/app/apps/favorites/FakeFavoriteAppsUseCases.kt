package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoriteAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.FakeDeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import kotlinx.coroutines.flow.Flow

class FakeObserveFavoriteAppsUseCase(
    private val flow: Flow<DataState<List<AppInfo>, RootError>>,
    dispatchers: DispatcherProvider = TestDispatchers(),
) : ObserveFavoriteAppsUseCase(
    fetchDeveloperAppsUseCase = FetchDeveloperAppsUseCase(FakeDeveloperAppsRepository(emptyList())),
    observeFavoritesUseCase = ObserveFavoritesUseCase(FakeFavoritesRepository()),
    dispatchers = dispatchers,
) {
    var invocationCount = 0

    override suspend fun invoke(): Flow<DataState<List<AppInfo>, RootError>> {
        invocationCount++
        return flow
    }
}

class FakeObserveFavoritesUseCase(
    private val flow: Flow<Set<String>>,
) : ObserveFavoritesUseCase(FakeFavoritesRepository()) {
    var invocationCount = 0

    override suspend fun invoke(): Flow<Set<String>> {
        invocationCount++
        return flow
    }
}

class FakeToggleFavoriteUseCase(
    private val onToggle: suspend (String) -> Unit = {},
) : ToggleFavoriteUseCase(FakeFavoritesRepository()) {
    var lastPackageName: String? = null
    var error: Throwable? = null
    var invocationCount: Int = 0

    override suspend fun invoke(param: String) {
        invocationCount++
        lastPackageName = param
        error?.let { throw it }
        onToggle(param)
    }
}
