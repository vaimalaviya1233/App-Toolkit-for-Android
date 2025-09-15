package com.d4rk.android.apps.apptoolkit.app.apps.favorites.ui

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoriteAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.di.DispatcherProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.ScreenMessageType
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteAppsViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    private lateinit var observeFavoriteAppsUseCase: ObserveFavoriteAppsUseCase
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var dispatcherProvider: DispatcherProvider

    @BeforeEach
    fun setUp() {
        observeFavoriteAppsUseCase = mockk()
        observeFavoritesUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        dispatcherProvider = mockk()

        every { dispatcherProvider.io } returns dispatcherExtension.testDispatcher
        every { dispatcherProvider.main } returns dispatcherExtension.testDispatcher
        every { dispatcherProvider.default } returns dispatcherExtension.testDispatcher
        every { dispatcherProvider.unconfined } returns dispatcherExtension.testDispatcher

        coEvery { toggleFavoriteUseCase.invoke(any()) } returns Unit
        coEvery { observeFavoritesUseCase() } returns flowOf(emptySet())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initial LoadFavorites trigger emits IsLoading`() = runTest(dispatcherExtension.testDispatcher) {
        val scope = this
        val favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>> = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
        }

        val viewModel = buildViewModel(favoriteAppsFlow)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.screenState is ScreenState.IsLoading)
            scope.advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        advanceUntilIdle()
        coVerify(exactly = 1) { observeFavoriteAppsUseCase.invoke() }
    }

    @Test
    fun `success with empty data sets NoData state`() = runTest(dispatcherExtension.testDispatcher) {
        val scope = this
        val favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>> = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success(emptyList<AppInfo>()))
        }

        val viewModel = buildViewModel(favoriteAppsFlow)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.screenState is ScreenState.IsLoading)

            scope.advanceUntilIdle()

            val noData = awaitItem()
            assertTrue(noData.screenState is ScreenState.NoData)
            assertNotNull(noData.data)
            assertTrue(noData.data?.apps?.isEmpty() == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `success with apps populates screen data`() = runTest(dispatcherExtension.testDispatcher) {
        val scope = this
        val apps = listOf(AppInfo(name = "App", packageName = "pkg", iconUrl = "icon"))
        val favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>> = flow {
            emit(DataState.Loading<List<AppInfo>, Error>())
            emit(DataState.Success(apps))
        }

        val viewModel = buildViewModel(favoriteAppsFlow)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.screenState is ScreenState.IsLoading)

            scope.advanceUntilIdle()

            val success = awaitItem()
            assertTrue(success.screenState is ScreenState.Success)
            assertEquals(apps, success.data?.apps)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error result shows snackbar and sets Error state`() = runTest(dispatcherExtension.testDispatcher) {
        val favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>> = flow {
            emit(DataState.Error<List<AppInfo>, Error>(error = Error("boom")))
        }

        val viewModel = buildViewModel(favoriteAppsFlow)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is ScreenState.Error)
        assertNull(state.data)
        val snackbar = state.snackbar
        assertNotNull(snackbar)
        assertEquals(UiTextHelper.StringResource(R.string.error_an_error_occurred), snackbar.message)
        assertTrue(snackbar.isError)
        assertEquals(ScreenMessageType.SNACKBAR, snackbar.type)
    }

    @Test
    fun `toggleFavorite failure updates state and shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo(name = "App", packageName = "pkg", iconUrl = "icon"))
        val favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>> = flow {
            emit(DataState.Success(apps))
        }
        val failure = IllegalStateException("fail")
        coEvery { toggleFavoriteUseCase.invoke("pkg") } throws failure

        val viewModel = buildViewModel(favoriteAppsFlow)

        advanceUntilIdle()

        viewModel.toggleFavorite("pkg")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.screenState is ScreenState.Error)
        val snackbar = state.snackbar
        assertNotNull(snackbar)
        assertEquals(UiTextHelper.StringResource(R.string.error_failed_to_update_favorite), snackbar.message)
        assertTrue(snackbar.isError)
        assertEquals(ScreenMessageType.SNACKBAR, snackbar.type)

        coVerify { toggleFavoriteUseCase.invoke("pkg") }
    }

    private fun buildViewModel(
        favoriteAppsFlow: Flow<DataState<List<AppInfo>, Error>>,
        favoritesFlow: Flow<Set<String>> = flowOf(emptySet())
    ): FavoriteAppsViewModel {
        coEvery { observeFavoriteAppsUseCase() } returns favoriteAppsFlow
        coEvery { observeFavoritesUseCase() } returns favoritesFlow
        return FavoriteAppsViewModel(
            observeFavoriteAppsUseCase = observeFavoriteAppsUseCase,
            observeFavoritesUseCase = observeFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            dispatchers = dispatcherProvider,
        )
    }
}
