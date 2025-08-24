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
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1"))
        viewModel.uiState.testSuccess(expectedSize = 1)
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
        setup(fetchFlow = flow, initialFavorites = emptySet())
        viewModel.uiState.testEmpty()
        println("\uD83C\uDFC1 [TEST DONE] load favorites - empty")
    }

    @Test
    fun `load favorites - error`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] load favorites - error")
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Error<List<AppInfo>, Error>(error = object : Error {}))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet())
        viewModel.uiState.testError()
        println("\uD83C\uDFC1 [TEST DONE] load favorites - error")
    }

    @Test
    fun `toggle favorite`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg"))
        toggleAndAssert(packageName = "pkg", expected = false)
        toggleAndAssert(packageName = "pkg", expected = true)
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
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1", "pkg2", "pkg3"))
        viewModel.uiState.testSuccess(expectedSize = 2)
        println("\uD83C\uDFC1 [TEST DONE] mismatched favorites filtered")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `favorites change during loading`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] favorites change during loading")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            delay(10)
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet())
        delay(5)
        viewModel.toggleFavorite("pkg")
        delay(20)
        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
        println("\uD83C\uDFC1 [TEST DONE] favorites change during loading")
    }

    @Test
    fun `state recovers after reload`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] state recovers after reload")
        val shared = MutableSharedFlow<DataState<List<AppInfo>, Error>>()
        setup(fetchFlow = shared, initialFavorites = setOf("pkg"))

        shared.emit(DataState.Loading())
        shared.emit(DataState.Error(error = object : Error {}))
        delay(10)
        assertTrue(viewModel.uiState.value.screenState is ScreenState.IsLoading)

        viewModel.onEvent(FavoriteAppsEvent.LoadFavorites)
        val apps = listOf(AppInfo("App", "pkg", "url"))
        shared.emit(DataState.Loading())
        shared.emit(DataState.Success(apps))
        delay(10)

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
        setup(fetchFlow = fetchFlow, favoritesFlow = failingFavs)

        viewModel.uiState.test {
            val first = awaitItem()
            assertTrue(first.screenState is ScreenState.IsLoading)
            delay(10)
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
        setup(fetchFlow = shared, favoritesFlow = favorites)

        delay(10)

        // initial list contains the app
        shared.emit(DataState.Success(listOf(AppInfo("App", "pkg", "url"))))
        delay(10)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)

        // list updates without the app
        shared.emit(DataState.Success(emptyList()))
        delay(10)
        assertTrue(viewModel.uiState.value.screenState is ScreenState.NoData)

        // toggle favorite on removed app
        viewModel.toggleFavorite("pkg")
        delay(10)
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
        setup(fetchFlow = flow, initialFavorites = setOf("pkg"))
        viewModel.uiState.testSuccess(expectedSize = 2)
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
            toggleError = RuntimeException("fail")
        )

        delay(10)
        viewModel.toggleFavorite("pkg")
        delay(10)
        assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite throws after load")
    }
}
