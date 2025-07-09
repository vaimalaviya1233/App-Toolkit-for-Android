package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestFavoriteAppsViewModel : TestFavoriteAppsViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `load favorites - success`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] load favorites - success")
        val apps = listOf(AppInfo("App1", "pkg1", "url1"), AppInfo("App2", "pkg2", "url2"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1"), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 1, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] load favorites - success")
    }

    @Test
    fun `load favorites - empty`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] load favorites - empty")
        val apps = listOf(AppInfo("App1", "pkg1", "url1"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testEmpty(testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] load favorites - empty")
    }

    @Test
    fun `load favorites - error`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] load favorites - error")
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Error<List<AppInfo>, Error>(error = object : Error {}))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testError(testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] load favorites - error")
    }

    @Test
    fun `toggle favorite`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg"), testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = false, testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = true, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite")
    }

    @Test
    fun `mismatched favorites filtered`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] mismatched favorites filtered")
        val apps = listOf(AppInfo("App1", "pkg1", "url1"), AppInfo("App2", "pkg2", "url2"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1", "pkg2", "pkg3"), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 2, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] mismatched favorites filtered")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `favorites change during loading`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] favorites change during loading")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            delay(100) // Ensures that the data is not emitted immediately
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        dispatcherExtension.testDispatcher.scheduler.advanceTimeBy(50)
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
        println("\uD83C\uDFC1 [TEST DONE] favorites change during loading")
    }

    @Test
    fun `state recovers after reload`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] state recovers after reload")
        val shared = MutableSharedFlow<DataState<List<AppInfo>, Error>>()
        setup(fetchFlow = shared, initialFavorites = setOf("pkg"), testDispatcher = dispatcherExtension.testDispatcher)

        shared.emit(DataState.Loading())
        shared.emit(DataState.Error(error = object : Error {}))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.IsLoading)

        viewModel.onEvent(FavoriteAppsEvent.LoadFavorites)
        val apps = listOf(AppInfo("App", "pkg", "url"))
        shared.emit(DataState.Loading())
        shared.emit(DataState.Success(apps))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
        println("\uD83C\uDFC1 [TEST DONE] state recovers after reload")
    }

    @Test
    fun `datastore flow error leaves loading state`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] datastore flow error leaves loading state")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val fetchFlow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        val failingFavs = flow<Set<String>> { throw RuntimeException("fail") }
        setup(fetchFlow = fetchFlow, testDispatcher = dispatcherExtension.testDispatcher, favoritesFlow = failingFavs)

        viewModel.uiState.test {
            val first = awaitItem()
            assertTrue(first.screenState is ScreenState.IsLoading)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
            assertTrue(viewModel.uiState.value.screenState is ScreenState.IsLoading)
        }
        println("\uD83C\uDFC1 [TEST DONE] datastore flow error leaves loading state")
    }

    @Test
    fun `toggle favorite after removal`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite after removal")
        val shared = MutableSharedFlow<DataState<List<AppInfo>, Error>>()
        val favorites = MutableSharedFlow<Set<String>>(replay = 1).apply { tryEmit(setOf("pkg")) }
        setup(fetchFlow = shared, testDispatcher = dispatcherExtension.testDispatcher, favoritesFlow = favorites)

        // allow view model initialization to complete before emitting values
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        // initial list contains the app
        shared.emit(DataState.Success(listOf(AppInfo("App", "pkg", "url"))))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)

        // list updates without the app
        shared.emit(DataState.Success(emptyList()))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.NoData)

        // toggle favorite on removed app
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite after removal")
    }

    @Test
    fun `duplicate apps kept in favorites list`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] duplicate apps kept in favorites list")
        val apps = listOf(
            AppInfo("App", "pkg", "url"),
            AppInfo("App", "pkg", "url")
        )
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg"), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 2, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] duplicate apps kept in favorites list")
    }

    @Test
    fun `toggle favorite throws after load`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite throws after load")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(
            fetchFlow = flow,
            initialFavorites = emptySet(),
            testDispatcher = dispatcherExtension.testDispatcher,
            toggleError = RuntimeException("fail")
        )

        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite throws after load")
    }
}
