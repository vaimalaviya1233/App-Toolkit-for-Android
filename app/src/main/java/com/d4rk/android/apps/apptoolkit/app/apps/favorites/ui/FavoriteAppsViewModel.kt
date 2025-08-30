package com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsAction
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoriteAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState.Error
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState.IsLoading
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState.NoData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState.Success
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoriteAppsViewModel(
    private val observeFavoriteAppsUseCase: ObserveFavoriteAppsUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScreenViewModel<UiHomeScreen, FavoriteAppsEvent, FavoriteAppsAction>(
    initialState = UiStateScreen(screenState = IsLoading(), data = UiHomeScreen())
) {

    private val loadFavoritesTrigger = MutableSharedFlow<Unit>(replay = 1)

    val favorites = flow { emitAll(observeFavoritesUseCase()) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    init {
        viewModelScope.launch {
            loadFavoritesTrigger
                .flatMapLatest { observeFavoriteAppsUseCase().flowOn(ioDispatcher) }
                .collect { result ->
                    when (result) {
                        is DataState.Success -> {
                            val apps = result.data
                            if (apps.isEmpty()) {
                                screenState.update { current ->
                                    current.copy(
                                        screenState = NoData(),
                                        data = UiHomeScreen(apps = emptyList())
                                    )
                                }
                            } else {
                                screenState.updateData(Success()) { current ->
                                    current.copy(apps = apps)
                                }
                            }
                        }

                        is DataState.Loading -> screenState.updateState(IsLoading())

                        is DataState.Error -> {
                            screenState.update { current ->
                                current.copy(screenState = Error("An error occurred"), data = null)
                            }
                        }
                    }
                }
        }
        loadFavoritesTrigger.tryEmit(Unit)
    }

    override fun onEvent(event: FavoriteAppsEvent) {
        when (event) {
            FavoriteAppsEvent.LoadFavorites -> loadFavoritesTrigger.tryEmit(Unit)
        }
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch(ioDispatcher) {
            runCatching { toggleFavoriteUseCase(packageName) }
        }
    }
}

