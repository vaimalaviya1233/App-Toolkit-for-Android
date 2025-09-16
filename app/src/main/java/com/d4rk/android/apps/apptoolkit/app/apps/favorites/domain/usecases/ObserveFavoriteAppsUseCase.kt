package com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * Use case that combines the list of developer apps with the current set of
 * favorites and emits only those apps that are marked as favorite.
 */
open class ObserveFavoriteAppsUseCase(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val dispatchers: DispatcherProvider,
) {
    open suspend operator fun invoke(): Flow<DataState<List<AppInfo>, RootError>> {
        return combine(
            fetchDeveloperAppsUseCase().flowOn(dispatchers.io),
            observeFavoritesUseCase()
        ) { dataState, favorites ->
            when (dataState) {
                is DataState.Success -> {
                    val apps = dataState.data.filter { favorites.contains(it.packageName) }
                    DataState.Success(apps)
                }
                is DataState.Error -> dataState
                is DataState.Loading -> DataState.Loading()
            }
        }.flowOn(dispatchers.io)
    }
}

