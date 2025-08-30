package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeAction
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.showSnackbar
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppsListViewModel(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    observeFavoritesUseCase: ObserveFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ScreenViewModel<UiHomeScreen, HomeEvent, HomeAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading(), data = UiHomeScreen())
) {

    private val fetchAppsTrigger = MutableSharedFlow<Unit>(replay = 1)

    val favorites = flow { emitAll(observeFavoritesUseCase()) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet()
    )

    init {
        viewModelScope.launch {
            fetchAppsTrigger
                .flatMapLatest { fetchDeveloperAppsUseCase().flowOn(ioDispatcher) }
                .collect { result ->
                    when (result) {
                        is DataState.Success -> {
                            val apps = result.data
                            if (apps.isEmpty()) {
                                screenState.update { current ->
                                    current.copy(
                                        screenState = ScreenState.NoData(),
                                        data = UiHomeScreen(apps = emptyList())
                                    )
                                }
                            } else {
                                screenState.updateData(newState = ScreenState.Success()) { currentData ->
                                    currentData.copy(apps = apps)
                                }
                            }
                        }

                        is DataState.Error -> showLoadAppsError()

                        is DataState.Loading -> screenState.updateState(ScreenState.IsLoading())
                    }
                }
        }

        fetchAppsTrigger.tryEmit(Unit)
    }

    override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.FetchApps -> fetchAppsTrigger.tryEmit(Unit)
        }
    }

    private fun showLoadAppsError() {
        screenState.updateState(ScreenState.Error())
        screenState.showSnackbar(
            UiSnackbar(
                message = UiTextHelper.DynamicString("Failed to load apps"),
                isError = true,
                timeStamp = System.currentTimeMillis(),
                type = ScreenMessageType.SNACKBAR,
            )
        )
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch(ioDispatcher) {
            runCatching { toggleFavoriteUseCase(packageName) }
                .onFailure { error ->
                    error.printStackTrace()
                    screenState.update { currentState ->
                        currentState.copy(screenState = ScreenState.Error())
                    }
                }
        }
    }
}
