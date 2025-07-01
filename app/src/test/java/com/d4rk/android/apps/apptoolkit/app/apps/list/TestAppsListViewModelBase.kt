package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.app.core.TestDispatchers
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.google.common.truth.Truth.assertThat
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
open class TestAppsListViewModelBase {

    protected lateinit var dispatcherProvider: TestDispatchers
    protected lateinit var viewModel: AppsListViewModel
    private lateinit var fetchUseCase: FetchDeveloperAppsUseCase
    private lateinit var dataStore: DataStore

    protected fun setup(
        fetchFlow: Flow<DataState<List<AppInfo>, RootError>>,
        initialFavorites: Set<String> = emptySet(),
        testDispatcher: TestDispatcher
    ) {
        println("\uD83E\uDDEA [SETUP] Initial favorites: $initialFavorites")
        dispatcherProvider = TestDispatchers(testDispatcher)
        fetchUseCase = mockk()
        dataStore = mockk(relaxed = true)
        val favoritesFlow = MutableStateFlow(initialFavorites)
        every { dataStore.favoriteApps } returns favoritesFlow
        coEvery { dataStore.toggleFavoriteApp(any()) } coAnswers {
            val pkg = it.invocation.args[0] as String
            println("\uD83D\uDD04 [DATASTORE MOCK] toggleFavoriteApp($pkg)")
            val current = favoritesFlow.value.toMutableSet()
            if (!current.add(pkg)) {
                current.remove(pkg)
            }
            favoritesFlow.value = current
        }
        coEvery { fetchUseCase.invoke() } returns fetchFlow

        viewModel = AppsListViewModel(fetchUseCase, dispatcherProvider, dataStore)
        println("\u2705 [SETUP] ViewModel initialized")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testSuccess(
        expectedSize: Int,
        testDispatcher: TestDispatcher
    ) {
        println("\uD83D\uDE80 [TEST START] testSuccess expecting $expectedSize items")
        this@testSuccess.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            assertTrue(first.screenState is ScreenState.IsLoading) { "First emission should be IsLoading but was ${first.screenState}" }
            println("advancing dispatcher...")
            testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            println("\u2705 [EMISSION] $second")
            assertTrue(second.screenState is ScreenState.Success) { "Second emission should be Success but was ${second.screenState}" }
            assertThat(second.data?.apps?.size).isEqualTo(expectedSize)
            println("\uD83D\uDC4D [ASSERTION PASSED] Success with ${second.data?.apps?.size} items")
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testSuccess")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testEmpty(testDispatcher: TestDispatcher) {
        println("\uD83D\uDE80 [TEST START] testEmpty")
        this@testEmpty.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            assertTrue(first.screenState is ScreenState.IsLoading) { "First emission should be IsLoading but was ${first.screenState}" }
            println("advancing dispatcher...")
            testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            println("\u2139\uFE0F [EMISSION 2] $second")
            assertTrue(second.screenState is ScreenState.NoData) { "Second emission should be NoData but was ${second.screenState}" }
            println("\uD83D\uDC4D [ASSERTION PASSED] NoData state observed")
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testEmpty")
    }

    protected fun toggleAndAssert(packageName: String, expected: Boolean, testDispatcher: TestDispatcher) {
        println("\uD83D\uDE80 [TEST START] toggleAndAssert for $packageName expecting $expected")
        println("Favorites before: ${viewModel.favorites.value}")
        viewModel.toggleFavorite(packageName)
        println("\uD83D\uDD04 [ACTION] toggled $packageName")
        println("advancing dispatcher...")
        testDispatcher.scheduler.advanceUntilIdle()
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
}
