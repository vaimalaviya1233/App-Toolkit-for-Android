package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class TestFavoriteAppsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `loading state emitted when observe favorite apps emits loading`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val viewModel = createViewModel(dataFlow = dataFlow)
        runCurrent()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            dataFlow.tryEmit(DataState.Loading<List<AppInfo>, Errors>())
            runCurrent()

            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `success state updates with returned apps`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val viewModel = createViewModel(dataFlow = dataFlow)
        runCurrent()

        val apps = listOf(AppInfo(name = "App", packageName = "pkg", iconUrl = "icon"))

        viewModel.uiState.test {
            awaitItem() // Initial loading

            dataFlow.tryEmit(DataState.Success<List<AppInfo>, Errors>(apps))
            runCurrent()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps).containsExactlyElementsIn(apps)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `no data state emitted when success contains empty list`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val viewModel = createViewModel(dataFlow = dataFlow)
        runCurrent()

        viewModel.uiState.test {
            awaitItem()

            dataFlow.tryEmit(DataState.Success<List<AppInfo>, Errors>(emptyList<AppInfo>()))
            runCurrent()

            val noData = awaitItem()
            assertThat(noData.screenState).isInstanceOf(ScreenState.NoData::class.java)
            assertThat(noData.data?.apps).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state emits snackbar message`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val viewModel = createViewModel(dataFlow = dataFlow)
        runCurrent()

        viewModel.uiState.test {
            awaitItem()

            dataFlow.tryEmit(
                DataState.Error<List<AppInfo>, Errors>(error = Errors.UseCase.FAILED_TO_LOAD_APPS)
            )
            runCurrent()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            assertThat(errorState.data).isNull()

            val snackbarState = awaitItem()
            assertThat(snackbarState.snackbar?.message)
                .isEqualTo(UiTextHelper.StringResource(R.string.error_an_error_occurred))
            assertThat(snackbarState.snackbar?.isError).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite delegates to use case`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val toggleUseCase = FakeToggleFavoriteUseCase()
        val viewModel = createViewModel(dataFlow = dataFlow, toggleUseCase = toggleUseCase)
        runCurrent()

        viewModel.toggleFavorite("pkg")
        runCurrent()

        assertThat(toggleUseCase.invocationCount).isEqualTo(1)
        assertThat(toggleUseCase.lastPackageName).isEqualTo("pkg")
    }

    @Test
    fun `toggle favorite failure updates state and snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val toggleUseCase = FakeToggleFavoriteUseCase().apply { error = RuntimeException("fail") }
        val viewModel = createViewModel(dataFlow = dataFlow, toggleUseCase = toggleUseCase)
        runCurrent()

        viewModel.uiState.test {
            awaitItem()

            viewModel.toggleFavorite("pkg")
            runCurrent()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)

            val snackbarState = awaitItem()
            assertThat(snackbarState.snackbar?.message)
                .isEqualTo(UiTextHelper.StringResource(R.string.error_failed_to_update_favorite))
            assertThat(snackbarState.snackbar?.isError).isTrue()
            assertThat(toggleUseCase.lastPackageName).isEqualTo("pkg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite updates favorites flow`() = runTest(dispatcherExtension.testDispatcher) {
        val favoritesFlow = MutableStateFlow(emptySet<String>())
        val dataFlow = MutableSharedFlow<DataState<List<AppInfo>, Errors>>(replay = 1)
        val toggleUseCase = FakeToggleFavoriteUseCase { packageName ->
            val current = favoritesFlow.value.toMutableSet()
            if (!current.add(packageName)) {
                current.remove(packageName)
            }
            favoritesFlow.value = current
        }
        val viewModel = createViewModel(
            dataFlow = dataFlow,
            favoritesFlow = favoritesFlow,
            toggleUseCase = toggleUseCase,
        )
        runCurrent()

        viewModel.favorites.test {
            assertThat(awaitItem()).isEmpty()

            viewModel.toggleFavorite("pkg")
            runCurrent()
            assertThat(awaitItem()).containsExactly("pkg")

            viewModel.toggleFavorite("pkg")
            runCurrent()
            assertThat(awaitItem()).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(
        dataFlow: MutableSharedFlow<DataState<List<AppInfo>, Errors>> = MutableSharedFlow(replay = 1),
        favoritesFlow: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
        toggleUseCase: FakeToggleFavoriteUseCase = FakeToggleFavoriteUseCase(),
    ): FavoriteAppsViewModel {
        val dispatchers = TestDispatchers(dispatcherExtension.testDispatcher)
        return FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = FakeObserveFavoriteAppsUseCase(dataFlow, dispatchers),
            observeFavoritesUseCase = FakeObserveFavoritesUseCase(favoritesFlow),
            toggleFavoriteUseCase = toggleUseCase,
            dispatchers = dispatchers,
        )
    }
}
