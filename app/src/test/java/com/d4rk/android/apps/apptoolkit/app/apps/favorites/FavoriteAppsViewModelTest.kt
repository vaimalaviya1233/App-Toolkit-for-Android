package com.d4rk.android.apps.apptoolkit.app.apps.favorites

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui.FavoriteAppsViewModel
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class FavoriteAppsViewModelTest {

    private val fetchUseCase = mockk<FetchDeveloperAppsUseCase>()
    private val observeFavoritesUseCase = mockk<ObserveFavoritesUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load favorites success`() = runTest(dispatcher) {
        val favoritesFlow = MutableStateFlow(setOf("pkg"))
        every { observeFavoritesUseCase.invoke() } returns favoritesFlow
        val app = AppInfo(name = "App", packageName = "pkg", iconUrl = "")
        coEvery { fetchUseCase.invoke() } returns flowOf(
            DataState.Success<List<AppInfo>, RootError>(listOf(app))
        )

        val viewModel = FavoriteAppsViewModel(fetchUseCase, observeFavoritesUseCase, toggleFavoriteUseCase)

        viewModel.screenState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            dispatcher.scheduler.advanceUntilIdle()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps).isEqualTo(listOf(app))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite delegates to use case`() = runTest(dispatcher) {
        every { observeFavoritesUseCase.invoke() } returns MutableStateFlow(emptySet())
        coEvery { fetchUseCase.invoke() } returns flowOf(
            DataState.Success<List<AppInfo>, RootError>(emptyList())
        )
        coEvery { toggleFavoriteUseCase.invoke("pkg") } returns Unit

        val viewModel = FavoriteAppsViewModel(fetchUseCase, observeFavoritesUseCase, toggleFavoriteUseCase)

        viewModel.toggleFavorite("pkg")
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleFavoriteUseCase.invoke("pkg") }
    }
}
