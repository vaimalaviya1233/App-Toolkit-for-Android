package com.d4rk.android.apps.apptoolkit.app.home.ui

import com.d4rk.android.apps.apptoolkit.app.home.domain.action.HomeAction
import com.d4rk.android.apps.apptoolkit.app.home.domain.action.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.home.domain.model.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.home.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.applyResult
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateState
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val fetchDeveloperAppsUseCase : FetchDeveloperAppsUseCase , private val dispatcherProvider : DispatcherProvider
) : ScreenViewModel<UiHomeScreen , HomeEvent , HomeAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading() , data = UiHomeScreen(apps = emptyList()))
) {

    init {
        fetchDeveloperApps()
    }

    override fun onEvent(event : HomeEvent) {
        when (event) {
            HomeEvent.FetchApps -> fetchDeveloperApps()
        }
    }

    private fun fetchDeveloperApps() {
        launch(dispatcherProvider.io) {
            fetchDeveloperAppsUseCase().stateIn(this , SharingStarted.Lazily , DataState.Loading()).collect { result ->
                        screenState.applyResult(result , "Failed to fetch apps") { apps , current ->
                            if (apps.isNotEmpty()) {
                                current.copy(apps = apps)
                            }
                            else {
                                // Show empty state if apps list is empty
                                screenState.updateState(ScreenState.NoData())
                                current
                            }
                        }
                    }
        }
    }
}
