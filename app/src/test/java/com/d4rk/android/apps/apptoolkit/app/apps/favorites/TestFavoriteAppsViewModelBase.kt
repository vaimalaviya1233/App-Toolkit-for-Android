package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.repository.FavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
open class TestFavoriteAppsViewModelBase {

    protected lateinit var viewModel: FavoriteAppsViewModel
    private lateinit var fetchUseCase: FetchDeveloperAppsUseCase
    private lateinit var favoritesRepository: FavoritesRepository

    protected fun setup(
        fetchFlow: Flow<DataState<List<AppInfo>, RootError>>,
        initialFavorites: Set<String> = emptySet(),
        testDispatcher: TestDispatcher,
        favoritesFlow: Flow<Set<String>>? = null,
        toggleError: Throwable? = null
    ) {
        println("\uD83E\uDDEA [SETUP] Initial favorites: $initialFavorites")
        fetchUseCase = mockk()
        favoritesRepository = mockk(relaxed = true)

        val favFlow = favoritesFlow ?: MutableStateFlow(initialFavorites)
        every { favoritesRepository.observeFavorites() } returns favFlow

        if (toggleError != null) {
            coEvery { favoritesRepository.toggleFavorite(any()) } throws toggleError
        } else if (favFlow is MutableStateFlow<Set<String>> || favFlow is MutableSharedFlow<Set<String>>) {
            coEvery { favoritesRepository.toggleFavorite(any()) } coAnswers {
                val pkg = it.invocation.args[0] as String
                val current = when (favFlow) {
                    is MutableStateFlow -> favFlow.value
                    else -> favFlow.replayCache.lastOrNull() ?: emptySet()
                }.toMutableSet()
                if (!current.add(pkg)) current.remove(pkg)
                when (favFlow) {
                    is MutableStateFlow -> favFlow.value = current
                    else -> favFlow.emit(current)
                }
            }
        } else {
            coEvery { favoritesRepository.toggleFavorite(any()) } returns Unit
        }

        coEvery { fetchUseCase.invoke() } returns fetchFlow

        val observeFavoritesUseCase = ObserveFavoritesUseCase(favoritesRepository)
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoritesRepository)

        viewModel = FavoriteAppsViewModel(
            fetchDeveloperAppsUseCase = fetchUseCase,
            observeFavoritesUseCase = observeFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase
        )
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
            assertTrue(first.screenState is ScreenState.IsLoading)
            println("advancing dispatcher...")
            testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            println("\u2705 [EMISSION] $second")
            assertTrue(second.screenState is ScreenState.Success)
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
            assertTrue(first.screenState is ScreenState.IsLoading)
            println("advancing dispatcher...")
            testDispatcher.scheduler.advanceUntilIdle()

            val second = awaitItem()
            println("\u2139\uFE0F [EMISSION 2] $second")
            assertTrue(second.screenState is ScreenState.NoData)
            println("\uD83D\uDC4D [ASSERTION PASSED] NoData state observed")
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testEmpty")
    }

    protected suspend fun Flow<UiStateScreen<UiHomeScreen>>.testError(testDispatcher: TestDispatcher) {
        println("\uD83D\uDE80 [TEST START] testError")
        this@testError.test {
            val first = awaitItem()
            println("\u23F3 [EMISSION 1] $first")
            assertTrue(first.screenState is ScreenState.IsLoading)
            println("advancing dispatcher...")
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
            println("checking state after dispatcher idle...")
            // Error flow doesn't update state, so it should remain loading
            val current = viewModel.uiState.value
            assertTrue(current.screenState is ScreenState.IsLoading)
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST END] testError")
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
