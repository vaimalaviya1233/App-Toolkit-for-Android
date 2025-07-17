package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertFailsWith

class TestAppsListViewModel : TestAppsListViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `fetch apps - success list`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] fetch apps - success list")
        val apps = listOf(
            AppInfo("App1", "pkg1", "url1"),
            AppInfo("App2", "pkg2", "url2")
        )
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = apps.size, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] fetch apps - success list")
    }

    @Test
    fun `fetch apps - empty list`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] fetch apps - empty list")
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(emptyList()))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testEmpty(testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] fetch apps - empty list")
    }

    @Test
    fun `fetch apps - error`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] fetch apps - error")
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Error<List<AppInfo>, Error>(error = object : Error {}))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testError(testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] fetch apps - error")
    }

    @Test
    fun `toggle favorite`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = true, testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = false, testDispatcher = dispatcherExtension.testDispatcher)
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite")
    }

    @Test
    fun `toggle favorite for package not in list`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "missing.pkg", expected = true, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggle favorite during fetch`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            delay(100)
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        dispatcherExtension.testDispatcher.scheduler.advanceTimeBy(50)
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.favorites.value.contains("pkg")).isTrue()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `error state clears after reload`() = runTest(dispatcherExtension.testDispatcher) {
        val shared = MutableSharedFlow<DataState<List<AppInfo>, Error>>()
        setup(fetchFlow = shared, testDispatcher = dispatcherExtension.testDispatcher)

        shared.emit(DataState.Loading())
        shared.emit(DataState.Error(error = object : Error {}))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.IsLoading)

        val apps = listOf(AppInfo("App", "pkg", "url"))
        viewModel.onEvent(HomeEvent.FetchApps)
        shared.emit(DataState.Loading())
        shared.emit(DataState.Success(apps))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `datastore failure does not update favorites`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher, toggleError = RuntimeException("ds fail"))
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
    }

    @Test
    fun `duplicate apps returned are preserved`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(
            AppInfo("App", "pkg", "url"),
            AppInfo("App", "pkg", "url")
        )
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 2, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `favorite apps flow throws`() = runTest(dispatcherExtension.testDispatcher) {
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
    }

    @Test
    fun `fetch apps - large list`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = (1..10_000).map { AppInfo("App$it", "pkg$it", "url$it") }
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = apps.size, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `toggle favorite concurrently`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, testDispatcher = dispatcherExtension.testDispatcher)

        coroutineScope {
            repeat(5) { launch { viewModel.toggleFavorite("pkg") } }
        }
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.favorites.value.contains("pkg")).isTrue()
    }

    @Test
    fun `fetch apps - use case throws`() {
        assertFailsWith<RuntimeException> {
            runTest(dispatcherExtension.testDispatcher) {
                val flow = flow<DataState<List<AppInfo>, Error>> { }
                setup(
                    fetchFlow = flow,
                    testDispatcher = dispatcherExtension.testDispatcher,
                    fetchThrows = RuntimeException("boom")
                )
                dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
            }
        }
    }

    @Test
    fun `favorites update during fetch`() = runTest(dispatcherExtension.testDispatcher) {
        val favorites = MutableSharedFlow<Set<String>>(replay = 1).apply { tryEmit(emptySet()) }
        val fetchFlow = MutableSharedFlow<DataState<List<AppInfo>, Error>>()
        setup(fetchFlow = fetchFlow, testDispatcher = dispatcherExtension.testDispatcher, favoritesFlow = favorites)

        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val apps = listOf(AppInfo("App", "pkg", "url"))
        fetchFlow.emit(DataState.Loading())
        favorites.emit(setOf("pkg"))
        fetchFlow.emit(DataState.Success(apps))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.favorites.value.contains("pkg")).isTrue()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
    }
}
