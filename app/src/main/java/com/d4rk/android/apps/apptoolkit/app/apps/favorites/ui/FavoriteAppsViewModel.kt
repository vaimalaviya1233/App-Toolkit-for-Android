package com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsAction
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoriteAppsViewModel(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    val dataStore: DataStore,
    private val dispatcherProvider: DispatcherProvider
) : ScreenViewModel<UiHomeScreen, FavoriteAppsEvent, FavoriteAppsAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading(), data = UiHomeScreen())
) {

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    private val favoritesLoaded = MutableStateFlow(false)

    val favorites = _favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    init {
        viewModelScope.launch(context = dispatcherProvider.io, start = CoroutineStart.UNDISPATCHED) {
            runCatching {
                dataStore.favoriteApps
                    .onEach {
                        favoritesLoaded.value = true
                        _favorites.value = it
                    }
                    .collect()
            }
        }

        // ensure favorites are loaded before fetching apps
        viewModelScope.launch(context = dispatcherProvider.io, start = CoroutineStart.UNDISPATCHED) {
            favoritesLoaded
                .filter { it }
                .first()
            onEvent(FavoriteAppsEvent.LoadFavorites)
        }
    }

    override fun onEvent(event: FavoriteAppsEvent) {
        when (event) {
            FavoriteAppsEvent.LoadFavorites -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch(
            context = dispatcherProvider.io,
            start = CoroutineStart.UNDISPATCHED
        ) {
            combine(
                flow = fetchDeveloperAppsUseCase().flowOn(dispatcherProvider.default),
                flow2 = favorites
            ) { dataState, favorites ->
                dataState to favorites
            }.collect { (result, saved) ->
                if (!favoritesLoaded.value) return@collect
                if (result is DataState.Success) {
                    val apps = result.data.filter { saved.contains(it.packageName) }
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
        viewModelScope.launch(context = dispatcherProvider.io) {
            runCatching {
                dataStore.toggleFavoriteApp(packageName)
            }
        }
    }
}

