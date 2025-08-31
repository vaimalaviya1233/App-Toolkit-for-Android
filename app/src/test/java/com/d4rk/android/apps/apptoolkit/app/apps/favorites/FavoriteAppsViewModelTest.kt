package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoriteAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteAppsViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `favorites flow starts empty and updates`() = runTest(dispatcherExtension.testDispatcher) {
        val observeFavorites = ObserveFavoritesUseCase()
        val observeFavoriteApps = ObserveFavoriteAppsUseCase()
        val toggleFavorite = ToggleFavoriteUseCase()
        val viewModel = FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = observeFavoriteApps,
            observeFavoritesUseCase = observeFavorites,
            toggleFavoriteUseCase = toggleFavorite,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        viewModel.favorites.test {
            assertThat(awaitItem()).isEmpty()
            observeFavorites.flow.emit(setOf("pkg"))
            assertThat(awaitItem()).containsExactly("pkg")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `screen state reflects DataState emissions`() = runTest(dispatcherExtension.testDispatcher) {
        val observeFavorites = ObserveFavoritesUseCase()
        val observeFavoriteApps = ObserveFavoriteAppsUseCase()
        val toggleFavorite = ToggleFavoriteUseCase()
        val viewModel = FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = observeFavoriteApps,
            observeFavoritesUseCase = observeFavorites,
            toggleFavoriteUseCase = toggleFavorite,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        viewModel.uiState.test {
            assertThat(awaitItem().screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            observeFavoriteApps.flow.emit(DataState.Loading())
            assertThat(awaitItem().screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            observeFavoriteApps.flow.emit(DataState.Error(error = Error("boom")))
            assertThat(awaitItem().screenState).isInstanceOf(ScreenState.Error::class.java)

            observeFavoriteApps.flow.emit(DataState.Success(emptyList()))
            assertThat(awaitItem().screenState).isInstanceOf(ScreenState.NoData::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite failure updates screen state with error`() = runTest(dispatcherExtension.testDispatcher) {
        val observeFavorites = ObserveFavoritesUseCase()
        val observeFavoriteApps = ObserveFavoriteAppsUseCase()
        val toggleFavorite = ToggleFavoriteUseCase().apply { shouldFail = RuntimeException("fail") }
        val viewModel = FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = observeFavoriteApps,
            observeFavoritesUseCase = observeFavorites,
            toggleFavoriteUseCase = toggleFavorite,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        viewModel.uiState.test {
            awaitItem() // initial loading
            viewModel.toggleFavorite("pkg")
            assertThat(awaitItem().screenState).isInstanceOf(ScreenState.Error::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `continuous favorites collection via background launch`() = runTest(dispatcherExtension.testDispatcher) {
        val observeFavorites = ObserveFavoritesUseCase()
        val observeFavoriteApps = ObserveFavoriteAppsUseCase()
        val toggleFavorite = ToggleFavoriteUseCase()
        val viewModel = FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = observeFavoriteApps,
            observeFavoritesUseCase = observeFavorites,
            toggleFavoriteUseCase = toggleFavorite,
            ioDispatcher = dispatcherExtension.testDispatcher,
        )

        val collected = mutableListOf<Set<String>>()
        val job = backgroundScope.launch { viewModel.favorites.toList(collected) }

        observeFavorites.flow.emit(setOf("a"))
        observeFavorites.flow.emit(setOf("a", "b"))

        assertThat(collected[0]).isEmpty()
        assertThat(collected[1]).containsExactly("a")
        assertThat(collected[2]).containsExactly("a", "b")

        job.cancel()
    }
}
