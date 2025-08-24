package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class AppsListViewModelTest {

    private val fetchUseCase = mockk<FetchDeveloperAppsUseCase>()
    private val dataStore = mockk<DataStore>()
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
    fun `fetch apps success`() = runTest(dispatcher) {
        val app = AppInfo(name = "App", packageName = "pkg", iconUrl = "")
        coEvery { fetchUseCase.invoke() } returns flowOf(DataState.Success<List<AppInfo>, Error>(listOf(app)))
        every { dataStore.favoriteApps } returns MutableStateFlow(emptySet())

        val viewModel = AppsListViewModel(fetchUseCase, dataStore)

        viewModel.screenState.test {
            val initial = awaitItem()
            assertThat(initial.screenState).isInstanceOf(ScreenState.IsLoading::class.java)

            advanceUntilIdle()

            val success = awaitItem()
            assertThat(success.screenState).isInstanceOf(ScreenState.Success::class.java)
            assertThat(success.data?.apps).isEqualTo(listOf(app))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle favorite delegates to datastore`() = runTest(dispatcher) {
        val favFlow = MutableStateFlow(emptySet<String>())
        coEvery { fetchUseCase.invoke() } returns flowOf(DataState.Success<List<AppInfo>, Error>(emptyList()))
        every { dataStore.favoriteApps } returns favFlow
        coEvery { dataStore.toggleFavoriteApp("pkg") } coAnswers {
            favFlow.value = setOf("pkg")
        }

        val viewModel = AppsListViewModel(fetchUseCase, dataStore)

        viewModel.toggleFavorite("pkg")
        advanceUntilIdle()

        assertThat(viewModel.favorites.value).contains("pkg")
    }
}
