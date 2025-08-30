package com.d4rk.android.apps.apptoolkit.app.apps.list

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import app.cash.turbine.test

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
    fun `fetch apps - empty list shows no data`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList())

        viewModel.uiState.test {
            val first = awaitItem()
            val state = if (first.screenState is ScreenState.IsLoading) awaitItem() else first
            assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetch apps - repository error shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList(), fetchThrows = RuntimeException("fail"))

        viewModel.uiState.test {
            val first = awaitItem()
            val errorState = if (first.screenState is ScreenState.IsLoading) awaitItem() else first
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            val message = errorState.snackbar?.message as? UiTextHelper.DynamicString
            assertThat(message?.content).isEqualTo("Failed to load apps")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorites flow emits repository updates`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val favoritesFlow = MutableSharedFlow<Set<String>>()
        setup(fetchApps = apps, favoritesFlow = favoritesFlow)

        viewModel.favorites.test {
            assertThat(awaitItem()).isEmpty()
            favoritesFlow.emit(setOf("pkg"))
            assertThat(awaitItem()).containsExactly("pkg")
            favoritesFlow.emit(emptySet())
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite error updates ui state`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps, toggleError = RuntimeException("fail"))

        viewModel.uiState.test {
            awaitItem() // initial loading
            awaitItem() // success
            viewModel.toggleFavorite("pkg")
            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}