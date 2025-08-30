package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class TestFavoriteAppsViewModel : TestFavoriteAppsViewModelBase() {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `toggle favorite throws after load`() = runTest(dispatcherExtension.testDispatcher) {
        println("\uD83D\uDE80 [TEST] toggle favorite throws after load")
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(
            fetchApps = apps,
            initialFavorites = emptySet(),
            toggleError = RuntimeException("fail")
        )

        viewModel.favorites.test {
            awaitItem()
            viewModel.toggleFavorite("pkg")
            runCurrent()
            expectNoEvents()
            assertThat(viewModel.favorites.value.contains("pkg")).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
        println("\uD83C\uDFC1 [TEST DONE] toggle favorite throws after load")
    }

    @Test
    fun `load favorites emits saved apps`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(
            AppInfo("App1", "pkg1", "url1"),
            AppInfo("App2", "pkg2", "url2")
        )
        setup(
            fetchApps = apps,
            initialFavorites = setOf("pkg1")
        )

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps?.map { it.packageName }).containsExactly("pkg1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite updates favorites flow`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps)

        viewModel.favorites.test {
            assertThat(awaitItem()).isEmpty()
            viewModel.toggleFavorite("pkg")
            assertThat(awaitItem()).containsExactly("pkg")
            viewModel.toggleFavorite("pkg")
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `load favorites with no saved apps shows no data`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(fetchApps = apps, initialFavorites = emptySet())

        viewModel.uiState.test {
            awaitItem() // Initial loading
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
