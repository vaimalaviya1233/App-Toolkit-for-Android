package com.d4rk.android.apps.apptoolkit.app.apps.list.ui

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.actions.HomeEvent
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AppsListViewModelTest {

    @JvmField
    @RegisterExtension
    val dispatcherExtension = StandardDispatcherExtension()

    private lateinit var fetchDeveloperAppsUseCase: FetchDeveloperAppsUseCase
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var favoritesFlow: MutableStateFlow<Set<String>>

    @BeforeEach
    fun setUp() {
        fetchDeveloperAppsUseCase = mockk()
        observeFavoritesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        favoritesFlow = MutableStateFlow(emptySet())

        coEvery { observeFavoritesUseCase.invoke() } returns favoritesFlow
        coJustRun { toggleFavoriteUseCase.invoke(any()) }
    }

    private fun createViewModel(): AppsListViewModel {
        return AppsListViewModel(
            fetchDeveloperAppsUseCase,
            observeFavoritesUseCase,
            toggleFavoriteUseCase,
            TestDispatchers(dispatcherExtension.testDispatcher)
        )
    }

    @Test
    fun `initial trigger emits loading then success state`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(
            AppInfo(name = "First", packageName = "first.pkg", iconUrl = "first-url"),
            AppInfo(name = "Second", packageName = "second.pkg", iconUrl = "second-url")
        )

        every { fetchDeveloperAppsUseCase.invoke() } returns flow {
            emit(DataState.Loading())
            emit(DataState.Success(apps))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.advanceUntilIdle()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps).containsExactlyElementsIn(apps).inOrder()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial trigger emits loading then error state`() = runTest(dispatcherExtension.testDispatcher) {
        every { fetchDeveloperAppsUseCase.invoke() } returns flow {
            emit(DataState.Loading())
            emit(DataState.Error(error = Error("failed")))
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `home event fetch apps refreshes data`() = runTest(dispatcherExtension.testDispatcher) {
        val initialApps = listOf(AppInfo("Initial", "initial.pkg", "initial-url"))
        val refreshedApps = listOf(AppInfo("Refreshed", "refreshed.pkg", "refreshed-url"))

        every { fetchDeveloperAppsUseCase.invoke() } returnsMany listOf(
            flow {
                emit(DataState.Loading())
                emit(DataState.Success(initialApps))
            },
            flow {
                emit(DataState.Loading())
                emit(DataState.Success(refreshedApps))
            }
        )

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.advanceUntilIdle()

            val firstSuccess = awaitItem()
            assertThat(firstSuccess.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(firstSuccess.data?.apps).containsExactlyElementsIn(initialApps).inOrder()

            viewModel.onEvent(HomeEvent.FetchApps)

            this@runTest.advanceUntilIdle()

            val loading = awaitItem()
            assertThat(loading.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            val refreshed = awaitItem()
            assertThat(refreshed.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(refreshed.data?.apps).containsExactlyElementsIn(refreshedApps).inOrder()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite failure updates error state`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo("Any", "any.pkg", "any-url"))

        every { fetchDeveloperAppsUseCase.invoke() } returns flow {
            emit(DataState.Loading())
            emit(DataState.Success(apps))
        }
        coEvery { toggleFavoriteUseCase.invoke(any()) } throws IllegalStateException("toggle failed")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            this@runTest.advanceUntilIdle()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)

            viewModel.toggleFavorite("any.pkg")

            this@runTest.advanceUntilIdle()

            val errorState = awaitItem()
            assertThat(errorState.screenState).isInstanceOf(ScreenState.Error::class.java)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
