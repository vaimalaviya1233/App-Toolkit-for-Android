package com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsAction
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState.*
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteAppsViewModel(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScreenViewModel<UiHomeScreen, FavoriteAppsEvent, FavoriteAppsAction>(
    initialState = UiStateScreen(screenState = IsLoading(), data = UiHomeScreen())
) {

    private val favoritesFlow = observeFavoritesUseCase()
        .catch { e -> if (e is CancellationException) throw e }
        .shareIn(viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val favorites: StateFlow<Set<String>> = favoritesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    init {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            favoritesFlow.first()
            onEvent(FavoriteAppsEvent.LoadFavorites)
        }
    }

    override fun onEvent(event: FavoriteAppsEvent) {
        when (event) {
            FavoriteAppsEvent.LoadFavorites -> loadFavorites()
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            combine(
                flow = fetchDeveloperAppsUseCase().flowOn(ioDispatcher),
                flow2 = favorites
            ) { dataState, favsSet ->
                dataState to favsSet
            }
                .catch { e ->
                    if (e is CancellationException) throw e
                    screenState.update { current ->
                        current.copy(screenState = Error("An error occurred"), data = null)
                    }
                }
                .collect { (result, savedFavs) ->
                    when (result) {
                        is DataState.Success -> {
                            val apps = result.data.filter { appInfo -> savedFavs.contains(appInfo.packageName) }
                            println("[ViewModel logic] Filtered apps size: ${apps.size}, savedFavs: $savedFavs, result.data size: ${result.data.size}")
                            if (apps.isEmpty()) {
                                screenState.update { current ->
                                    current.copy(screenState = NoData(), data = current.data?.copy(apps = emptyList()))
                                }
                            } else {
                                screenState.updateData(Success()) { current ->
                                    current.copy(apps = apps)
                                }
                            }
                        }

                        is DataState.Loading -> {
                            screenState.updateState(IsLoading())
                        }

                        is DataState.Error -> {
                            screenState.update { current ->
                                current.copy(screenState = Error("An error occurred"), data = null)
                            }
                        }
                    }
                }
        }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(packageName)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Ignored
            }
        }
    }
}
