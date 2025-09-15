package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import app.cash.turbine.test
import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.FakeFavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.apps.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AppsListViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    private val fetchDeveloperAppsUseCase = mockk<FetchDeveloperAppsUseCase>()
    private lateinit var viewModel: AppsListViewModel

    @AfterEach
    fun tearDown() {
        if (::viewModel.isInitialized) {
            viewModel.viewModelScope.cancel()
        }
        clearAllMocks()
    }

    @Test
    fun `when fetch emits empty success state shows no data`() = runTest(dispatcherExtension.testDispatcher) {
        every { fetchDeveloperAppsUseCase() } returns flow<DataState<List<AppInfo>, Errors>> {
            emit(DataState.Success(emptyList()))
        }

        viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.runCurrent()

            val noData = awaitItem()
            assertThat(noData.screenState).isInstanceOf(ScreenState.NoData::class.java)
            assertThat(noData.data?.apps).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when fetch emits success apps are exposed`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "icon"))
        every { fetchDeveloperAppsUseCase() } returns flow<DataState<List<AppInfo>, Errors>> {
            emit(DataState.Success(apps))
        }

        viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.runCurrent()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps).containsExactlyElementsIn(apps).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when fetch emits error screen shows error and snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        every { fetchDeveloperAppsUseCase() } returns flow<DataState<List<AppInfo>, Errors>> {
            emit(DataState.Error(error = Errors.Network.NO_INTERNET))
        }

        viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.runCurrent()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)
            assertThat(errorState.snackbar).isNotNull()
            assertThat(errorState.snackbar?.isError).isTrue()
            assertThat(errorState.snackbar?.message)
                .isEqualTo(UiTextHelper.StringResource(R.string.error_failed_to_load_apps))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when refetch emits loading screen state is loading`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("App", "pkg", "icon"))
        val successFlow = flow<DataState<List<AppInfo>, Errors>> { emit(DataState.Success(apps)) }
        val loadingFlow = flow<DataState<List<AppInfo>, Errors>> { emit(DataState.Loading()) }
        every { fetchDeveloperAppsUseCase() } returnsMany listOf(successFlow, loadingFlow)

        viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.runCurrent()
            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)

            viewModel.onEvent(HomeEvent.FetchApps)
            this@runTest.runCurrent()

            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(): AppsListViewModel {
        val favoritesRepository = FakeFavoritesRepository()
        val observeFavoritesUseCase = ObserveFavoritesUseCase(favoritesRepository)
        val toggleFavoriteUseCase = ToggleFavoriteUseCase(favoritesRepository)
        return AppsListViewModel(
            fetchDeveloperAppsUseCase,
            observeFavoritesUseCase,
            toggleFavoriteUseCase,
            TestDispatchers(dispatcherExtension.testDispatcher),
        )
    }
}
