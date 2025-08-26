package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.FakeFavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.FakeDeveloperAppsRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.cancel
import androidx.lifecycle.viewModelScope
import org.junit.jupiter.api.AfterEach
import kotlinx.coroutines.flow.Flow
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
open class TestAppsListViewModelBase {

    protected lateinit var viewModel: AppsListViewModel
    protected fun setup(
        fetchFlow: Flow<DataState<List<AppInfo>, RootError>>,
        initialFavorites: Set<String> = emptySet(),
        favoritesFlow: Flow<Set<String>>? = null,
        toggleError: Throwable? = null,
        fetchThrows: Throwable? = null,
        dispatcher: CoroutineDispatcher = Dispatchers.Main,
    ) {
        println("\uD83E\uDDEA [SETUP] Initial favorites: $initialFavorites")
        val developerAppsRepository = FakeDeveloperAppsRepository(fetchFlow, fetchThrows)
        val fetchUseCase = FetchDeveloperAppsUseCase(developerAppsRepository)
        val favoritesRepository = FakeFavoritesRepository(initialFavorites, favoritesFlow, toggleError)
        val observeFavoritesUseCase = ObserveFavoritesUseCase(favoritesRepository)
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoritesRepository, dispatcher)
        viewModel = AppsListViewModel(fetchUseCase, observeFavoritesUseCase, toggleFavoriteUseCase)
        println("\u2705 [SETUP] ViewModel initialized")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testSuccess(
        expectedSize: Int
    ) {
        println("\uD83D\uDE80 [TEST START] testSuccess expecting $expectedSize items")
        this@testSuccess.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            if (first.screenState is ScreenState.IsLoading) {
                val second = awaitItem()
                println("\u2705 [EMISSION] $second")
                assertTrue(second.screenState is ScreenState.Success) { "Second emission should be Success but was ${second.screenState}" }
                assertThat(second.data?.apps?.size).isEqualTo(expectedSize)
            } else {
                assertTrue(first.screenState is ScreenState.Success) { "Expected Success state" }
                assertThat(first.data?.apps?.size).isEqualTo(expectedSize)
            }
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testSuccess")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testEmpty() {
        println("\uD83D\uDE80 [TEST START] testEmpty")
        this@testEmpty.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            if (first.screenState is ScreenState.IsLoading) {
                val second = awaitItem()
                println("\u2139\uFE0F [EMISSION 2] $second")
                assertTrue(second.screenState is ScreenState.NoData) { "Second emission should be NoData but was ${second.screenState}" }
            } else {
                assertTrue(first.screenState is ScreenState.NoData) { "Expected NoData state" }
            }
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testEmpty")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testError() {
        println("\uD83D\uDE80 [TEST START] testError")
        this@testError.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            assertTrue(first.screenState is ScreenState.IsLoading) { "First emission should be IsLoading but was ${first.screenState}" }
            expectNoEvents()
            println("checking state after dispatcher idle...")
            val current = viewModel.uiState.value
            // Error flow leaves state unchanged, so it should remain loading
            assertTrue(current.screenState is ScreenState.IsLoading) { "State should remain Loading on error" }
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testError")
    }

    protected suspend fun toggleAndAssert(packageName: String, expected: Boolean) {
        println("\uD83D\uDE80 [TEST START] toggleAndAssert for $packageName expecting $expected")
        println("Favorites before: ${viewModel.favorites.value}")
        viewModel.toggleFavorite(packageName)
        println("\uD83D\uDD04 [ACTION] toggled $packageName")
        advanceUntilIdle()
        val favorites = viewModel.favorites.value
        println("Favorites after: $favorites")
        if (favorites.contains(packageName) == expected) {
            println("\uD83D\uDC4D [ASSERTION PASSED] favorite state matches $expected")
        } else {
            println("\u274C [ASSERTION FAILED] expected $expected but was ${favorites.contains(packageName)}")
        }
        assertThat(favorites.contains(packageName)).isEqualTo(expected)
        println("\uD83C\uDFC1 [TEST END] toggleAndAssert")
    }

    @AfterEach
    fun tearDown() {
        if (this::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
    }
}
