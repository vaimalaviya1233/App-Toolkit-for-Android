package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
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
            toggleError = RuntimeException("fail"),
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
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
            initialFavorites = setOf("pkg1"),
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
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
        setup(fetchApps = apps, dispatchers = TestDispatchers(dispatcherExtension.testDispatcher))

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
        setup(fetchApps = apps, initialFavorites = emptySet(), dispatchers = TestDispatchers(dispatcherExtension.testDispatcher))

        viewModel.uiState.test {
            awaitItem() // Initial loading
            val state = awaitItem()
            assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `datastore updates move screen from no data to success`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        val favoritesFlow = MutableStateFlow(emptySet<String>())
        setup(
            fetchApps = apps,
            favoritesFlow = favoritesFlow,
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
        )

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            val noData = awaitItem()
            assertThat(noData.screenState).isInstanceOf(ScreenState.NoData::class.java)

            favoritesFlow.value = setOf("pkg")
            runCurrent()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps?.map { it.packageName }).containsExactly("pkg")
            assertThat(viewModel.favorites.value).containsExactly("pkg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `fetch apps error updates state and shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(
            fetchApps = apps,
            fetchError = Errors.UseCase.FAILED_TO_LOAD_APPS,
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
        )

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            assertThat(errorState.data).isNull()

            val snackbarState = awaitItem()
            assertThat(snackbarState.screenState).isInstanceOf(ScreenState.Error::class.java)
            val snackbar = snackbarState.snackbar
            assertThat(snackbar).isNotNull()
            assertThat(snackbar?.isError).isTrue()
            assertThat(snackbar?.message).isEqualTo(UiTextHelper.StringResource(R.string.error_an_error_occurred))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite failure updates state and snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "url"))
        setup(
            fetchApps = apps,
            initialFavorites = setOf("pkg"),
            toggleError = RuntimeException("fail"),
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
        )

        viewModel.uiState.test {
            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps?.map { it.packageName }).containsExactly("pkg")

            viewModel.toggleFavorite("pkg")
            runCurrent()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            assertThat(viewModel.favorites.value).containsExactly("pkg")

            val snackbarState = awaitItem()
            val snackbar = snackbarState.snackbar
            assertThat(snackbar).isNotNull()
            assertThat(snackbar?.isError).isTrue()
            assertThat(snackbar?.message).isEqualTo(UiTextHelper.StringResource(R.string.error_failed_to_update_favorite))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
