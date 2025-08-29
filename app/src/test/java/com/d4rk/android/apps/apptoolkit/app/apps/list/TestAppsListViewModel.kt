package com.d4rk.android.apps.apptoolkit.app.apps.list

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestAppsListViewModel : TestAppsListViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `fetch apps - large list`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = (1..10_000).map { AppInfo("App$it", "pkg$it", "url$it") }
        setup(fetchApps = apps)
        viewModel.uiState.testSuccess(expectedSize = apps.size)
    }

    @Test
    fun `toggle favorite updates state`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps)
        toggleAndAssert(packageName = "pkg", expected = true)
        toggleAndAssert(packageName = "pkg", expected = false)
    }

    @Test
    fun `fetch apps - empty list results in NoData`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList())
        viewModel.uiState.testNoData()
    }

    @Test
    fun `fetch apps - repository error emits Error state`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList(), fetchThrows = RuntimeException("boom"))
        viewModel.uiState.testError()
    }

    @Test
    fun `favorites flow emits initial favorites and updates`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = MutableSharedFlow<Set<String>>(replay = 1)
        flow.tryEmit(setOf("pkg"))
        setup(fetchApps = listOf(AppInfo("App", "pkg", "url")), favoritesFlow = flow)

        viewModel.favorites.test {
            assertThat(awaitItem()).containsExactly("pkg")
            flow.emit(setOf("pkg2"))
            assertThat(awaitItem()).containsExactly("pkg2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite error updates uiState to Error`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps, toggleError = RuntimeException("fail"))

        viewModel.uiState.test {
            awaitItem() // initial emission
            viewModel.toggleFavorite("pkg")
            val errorState = awaitItem()
            assertTrue(errorState.screenState is ScreenState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}