package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.cash.turbine.test
import com.d4rk.android.libs.apptoolkit.app.main.domain.repository.MainRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private class FakeMainRepository : MainRepository {
        val itemsFlow = MutableSharedFlow<List<NavigationDrawerItem>>(replay = 1)
        private val errorFlow = MutableSharedFlow<Throwable>(replay = 1)

        override fun getNavigationDrawerItems(): Flow<List<NavigationDrawerItem>> =
            merge(
                itemsFlow,
                errorFlow.flatMapConcat { throwable -> flow { throw throwable } }
            )

        suspend fun emitItems(items: List<NavigationDrawerItem>) {
            itemsFlow.emit(items)
        }

        suspend fun emitError(throwable: Throwable) {
            errorFlow.emit(throwable)
        }
    }

    @Test
    fun `emitting items updates navigation drawer items`() = runTest {
        val repository = FakeMainRepository()
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        val icon = ImageVector.Builder(
            name = "test",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).build()
        val items = listOf(NavigationDrawerItem(title = 1, selectedIcon = icon, route = "route"))

        viewModel.uiState.test {
            awaitItem() // initial state
            repository.emitItems(items)
            val updated = awaitItem()
            assertEquals(items, updated.data?.navigationDrawerItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emitting error shows snackbar`() = runTest {
        val repository = FakeMainRepository()
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        val error = RuntimeException("boom")

        viewModel.uiState.test {
            awaitItem() // initial state
            repository.emitError(error)
            val updated = awaitItem()
            assertEquals(true, updated.data?.showSnackbar)
            assertEquals("boom", updated.data?.snackbarMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

