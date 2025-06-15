package com.d4rk.android.apps.apptoolkit.app.apps.ui

import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.HomeAction
import com.d4rk.android.apps.apptoolkit.app.apps.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.d4rk.android.libs.apptoolkit.core.ui.base.ScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AppsListViewModel(
    private val fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val dataStore: DataStore
) : ScreenViewModel<UiHomeScreen, HomeEvent, HomeAction>(
    initialState = UiStateScreen(screenState = ScreenState.IsLoading(), data = UiHomeScreen())
) {

    private var allApps: List<AppInfo> = emptyList()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortAscending = MutableStateFlow(true)
    val sortAscending = _sortAscending.asStateFlow()

    init {
        onEvent(event = HomeEvent.FetchApps)
    }

    override fun onEvent(event : HomeEvent) {
        when (event) {
            HomeEvent.FetchApps -> fetchDeveloperApps()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun toggleSortOrder() {
        _sortAscending.value = !_sortAscending.value
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = allApps.filter { it.name.contains(searchQuery.value, ignoreCase = true) }
        val sorted = if (sortAscending.value) {
            filtered.sortedBy { it.name.lowercase() }
        } else {
            filtered.sortedByDescending { it.name.lowercase() }
        }
        screenState.updateData { currentData ->
            currentData.copy(apps = sorted)
        }
    }

    private fun fetchDeveloperApps() {
        launch(context = dispatcherProvider.io) {
            fetchDeveloperAppsUseCase().flowOn(dispatcherProvider.default).collect { result: DataState<List<AppInfo>, RootError> ->
                when (result) {
                    is DataState.Success -> {
                        val apps = result.data
                        allApps = apps
                        dataStore.saveCachedApps(Json.encodeToString(apps))
                        if (apps.isEmpty()) {
                            screenState.update { currentState ->
                                currentState.copy(screenState = ScreenState.NoData(), data = currentState.data?.copy(apps = emptyList()))
                            }
                        } else {
                            applyFilters()
                            screenState.update { currentState ->
                                currentState.copy(screenState = ScreenState.Success())
                            }
                        }
                    }

                    else -> {
                        val cached = dataStore.cachedApps.first()
                        if (cached != null) {
                            runCatching {
                                Json.decodeFromString<List<AppInfo>>(cached)
                            }.onSuccess { cachedApps ->
                                if (cachedApps.isNotEmpty()) {
                                    allApps = cachedApps
                                    screenState.updateData(ScreenState.Success()) { currentData ->
                                        currentData.copy(apps = cachedApps)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}