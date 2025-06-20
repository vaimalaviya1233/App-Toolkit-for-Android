package com.d4rk.android.apps.apptoolkit.app.apps.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.FavoriteAppsAction
import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class FavoriteAppsViewModel(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    val dataStore: DataStore,
    private val dispatcherProvider: DispatcherProvider
) : ScreenViewModel<UiHomeScreen, FavoriteAppsEvent, FavoriteAppsAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading(), data = UiHomeScreen())
) {

    val favorites = dataStore.favoriteApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    init {
        onEvent(FavoriteAppsEvent.LoadFavorites)
    }

    override fun onEvent(event: FavoriteAppsEvent) {
        when (event) {
            FavoriteAppsEvent.LoadFavorites -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        launch(context = dispatcherProvider.io) {
            combine(
                fetchDeveloperAppsUseCase().flowOn(dispatcherProvider.default),
                favorites
            ) { dataState, favs ->
                dataState to favs
            }.collect { (result, favs) ->
                if (result is DataState.Success) {
                    val apps = result.data.filter { favs.contains(it.packageName) }
                    if (apps.isEmpty()) {
                        screenState.update { current ->
                            current.copy(screenState = ScreenState.NoData(), data = current.data?.copy(apps = emptyList()))
                        }
                    } else {
                        screenState.updateData(ScreenState.Success()) { current ->
                            current.copy(apps = apps)
                        }
                    }
                }
            }
        }
    }

    fun toggleFavorite(packageName: String) {
        launch(context = dispatcherProvider.io) {
            dataStore.toggleFavoriteApp(packageName)
        }
    }
}

