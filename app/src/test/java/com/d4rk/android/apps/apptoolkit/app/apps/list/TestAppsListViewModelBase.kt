package com.d4rk.android.apps.apptoolkit.app.apps.list

import app.cash.turbine.test
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.AppInfo
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.model.ui.UiHomeScreen
import com.d4rk.android.apps.apptoolkit.app.apps.list.domain.usecases.FetchDeveloperAppsUseCase
import com.d4rk.android.apps.apptoolkit.app.apps.list.ui.AppsListViewModel
import com.d4rk.android.apps.apptoolkit.core.data.datastore.DataStore
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.RootError
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestDispatcher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
open class TestAppsListViewModelBase {

    protected lateinit var dispatcherProvider: TestDispatchers
    protected lateinit var viewModel: AppsListViewModel
    private lateinit var fetchUseCase: FetchDeveloperAppsUseCase
    private lateinit var dataStore: DataStore

    protected fun setup(
        fetchFlow: Flow<DataState<List<AppInfo>, RootError>>,
        initialFavorites: Set<String> = emptySet(),
        testDispatcher: TestDispatcher
    ) {
        dispatcherProvider = TestDispatchers(testDispatcher)
        fetchUseCase = mockk()
        dataStore = mockk(relaxed = true)
        every { dataStore.favoriteApps } returns MutableStateFlow(initialFavorites)
        coEvery { fetchUseCase.invoke() } returns fetchFlow

        viewModel = AppsListViewModel(fetchUseCase, dispatcherProvider, dataStore)
    }

    protected fun Flow<UiStateScreen<UiHomeScreen>>.testSuccess(
        expectedSize: Int,
        testDispatcher: TestDispatcher
    ) = runTest(testDispatcher) {
        this@testSuccess.test {
            // First emission from init loading might be Loading or IsLoading - the initial state
            val first = awaitItem()
            // Initial state is IsLoading from ViewModel's init
            assertTrue(first.screenState is ScreenState.IsLoading)

            val second = awaitItem()
            assertTrue(second.screenState is ScreenState.Success)
            assertThat(second.data?.apps?.size).isEqualTo(expectedSize)
        }
    }

    protected fun Flow<UiStateScreen<UiHomeScreen>>.testEmpty(testDispatcher: TestDispatcher) = runTest(testDispatcher) {
        this@testEmpty.test {
            val first = awaitItem()
            assertTrue(first.screenState is ScreenState.IsLoading)

            val second = awaitItem()
            assertTrue(second.screenState is ScreenState.NoData)
        }
    }

    protected fun toggleAndAssert(packageName: String, expected: Boolean, testDispatcher: TestDispatcher) =
        runTest(testDispatcher) {
            viewModel.toggleFavorite(packageName)
            testDispatcher.scheduler.advanceUntilIdle()
            val favorites = viewModel.favorites.value
            assertThat(favorites.contains(packageName)).isEqualTo(expected)
        }
}
