package com.d4rk.android.apps.apptoolkit.app.apps.list

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Error
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestAppsListViewModel : TestAppsListViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
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
}
