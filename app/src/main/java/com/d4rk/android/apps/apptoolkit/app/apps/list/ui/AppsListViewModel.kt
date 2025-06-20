package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeAction
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class AppsListViewModel(
    private val fetchDeveloperAppsUseCase : FetchDeveloperAppsUseCase,
    private val dispatcherProvider : DispatcherProvider,
    private val dataStore: DataStore
) : ScreenViewModel<UiHomeScreen , HomeEvent , HomeAction>(initialState = UiStateScreen(screenState = ScreenState.IsLoading() , data = UiHomeScreen())) {

    val favorites = dataStore.favoriteApps.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptySet()
    )

    init {
        onEvent(event = HomeEvent.FetchApps)
    }

    override fun onEvent(event : HomeEvent) {
        when (event) {
            HomeEvent.FetchApps -> fetchDeveloperApps()
        }
    }

    private fun fetchDeveloperApps() {
        launch(context = dispatcherProvider.io) {
            fetchDeveloperAppsUseCase().flowOn(dispatcherProvider.default).collect { result : DataState<List<AppInfo> , RootError> ->
                when (result) {
                    is DataState.Success -> {
                        val apps = result.data
                        if (apps.isEmpty()) {
                            screenState.update { currentState ->
                                currentState.copy(screenState = ScreenState.NoData() , data = currentState.data?.copy(apps = emptyList()))
                            }
                        }
                        else {
                            screenState.updateData(ScreenState.Success()) { currentData ->
                                currentData.copy(apps = apps)
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun toggleFavorite(packageName: String) {
        launch(context = dispatcherProvider.io) { dataStore.toggleFavoriteApp(packageName) }
    }
}