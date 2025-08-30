package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
    fun `fetch apps - empty list shows no data`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList())
        viewModel.uiState.testNoData()
    }

    @Test
    fun `fetch apps error emits error state`() = runTest(dispatcherExtension.testDispatcher) {
        setup(fetchApps = emptyList(), fetchThrows = RuntimeException("fail"))
        viewModel.uiState.testError()
    }

    @Test
    fun `favorites flow updates when repository emits new values`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(
            AppInfo("App1", "pkg1", "url1"),
            AppInfo("App2", "pkg2", "url2")
        )
        val favoritesFlow = MutableStateFlow(setOf("pkg1"))
        setup(fetchApps = apps, favoritesFlow = favoritesFlow)

        viewModel.favorites.test {
            assertThat(awaitItem()).containsExactly("pkg1")
            favoritesFlow.value = setOf("pkg1", "pkg2")
            assertThat(awaitItem()).containsExactly("pkg1", "pkg2")
            favoritesFlow.value = emptySet()
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite failure sets error state`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps, toggleError = RuntimeException("fail"))

        viewModel.uiState.test {
            awaitItem() // initial loading state
            viewModel.toggleFavorite("pkg")
            runCurrent()
            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}