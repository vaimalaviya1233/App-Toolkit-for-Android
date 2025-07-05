package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.actions.FavoriteAppsEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestFavoriteAppsViewModel : TestFavoriteAppsViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    @Test
    fun `load favorites - success`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App1", "pkg1", "url1"), AppInfo("App2", "pkg2", "url2"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1"), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 1, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `load favorites - empty`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App1", "pkg1", "url1"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testEmpty(testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `load favorites - error`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Error<List<AppInfo>, Error>(error = object : Error {}))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testError(testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `toggle favorite`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg"), testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = false, testDispatcher = dispatcherExtension.testDispatcher)
        toggleAndAssert(packageName = "pkg", expected = true, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `mismatched favorites filtered`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App1", "pkg1", "url1"), AppInfo("App2", "pkg2", "url2"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = setOf("pkg1", "pkg2", "pkg3"), testDispatcher = dispatcherExtension.testDispatcher)
        viewModel.uiState.testSuccess(expectedSize = 2, testDispatcher = dispatcherExtension.testDispatcher)
    }

    @Test
    fun `favorites change during loading`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val flow = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            delay(100)
            emit(DataState.Success<List<AppInfo>, Error>(apps))
        }
        setup(fetchFlow = flow, initialFavorites = emptySet(), testDispatcher = dispatcherExtension.testDispatcher)
        dispatcherExtension.testDispatcher.scheduler.advanceTimeBy(50)
        viewModel.toggleFavorite("pkg")
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
        assertThat(viewModel.uiState.value.data?.apps?.size).isEqualTo(1)
    }

    @Test
    fun `state recovers after reload`() = runTest(dispatcherExtension.testDispatcher) {
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
    }
}
