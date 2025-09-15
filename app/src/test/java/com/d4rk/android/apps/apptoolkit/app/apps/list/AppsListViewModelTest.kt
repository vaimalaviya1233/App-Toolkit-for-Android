package com.d4rk.android.apps.apptoolkit.app.apps.list

import androidx.lifecycle.viewModelScope
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.FakeFavoritesRepository
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ObserveFavoritesUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.favorites.domain.usecases.ToggleFavoriteUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.FakeDeveloperAppsRepository
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class AppsListViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @Test
    fun `toggle favorite failure emits screen error`() = runTest(dispatcherExtension.testDispatcher) {
        val apps = listOf(AppInfo(name = "App", packageName = "pkg", iconUrl = "url"))
        val fetchUseCase = FetchDeveloperAppsUseCase(FakeDeveloperAppsRepository(apps))
        val favoritesRepository = FakeFavoritesRepository()
        val observeFavoritesUseCase = ObserveFavoritesUseCase(favoritesRepository)
        val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>()
        coEvery { toggleFavoriteUseCase.invoke(any()) } throws IllegalStateException("Toggle failed")

        val viewModel = AppsListViewModel(
            fetchUseCase = fetchUseCase,
            observeFavoritesUseCase = observeFavoritesUseCase,
            toggleFavoriteUseCase = toggleFavoriteUseCase,
            dispatchers = TestDispatchers(dispatcherExtension.testDispatcher),
        )

        advanceUntilIdle()

        assertDoesNotThrow {
            viewModel.toggleFavorite("pkg")
        }

        advanceUntilIdle()

        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.Error::class.java)
        coVerify(exactly = 1) { toggleFavoriteUseCase.invoke("pkg") }

        viewModel.viewModelScope.cancel()
    }
}
