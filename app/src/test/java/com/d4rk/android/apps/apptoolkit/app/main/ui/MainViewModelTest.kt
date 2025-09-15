package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.d4rk.android.apps.apptoolkit.app.main.domain.model.ui.UiMainScreen
import com.d4rk.android.libs.apptoolkit.app.main.domain.repository.NavigationRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.UiStateScreen
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var navigationRepository: NavigationRepository

    @BeforeEach
    fun setUp() {
        navigationRepository = mockk()
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNavigationItems updates navigation drawer items`() = runTest(dispatcher.scheduler) {
        val expectedItems = listOf(
            NavigationDrawerItem(title = 1, selectedIcon = createIcon(), route = "route-1"),
            NavigationDrawerItem(title = 2, selectedIcon = createIcon(), route = "route-2"),
        )

        every { navigationRepository.getNavigationDrawerItems() } returns flowOf(expectedItems)

        val viewModel = TestMainViewModel(navigationRepository)
        val collectedStates = mutableListOf<UiMainScreen?>()
        val job = launch { viewModel.exposedScreenState().collect { collectedStates.add(it.data) } }

        advanceUntilIdle()

        assertThat(collectedStates).isNotEmpty()
        val latestState = collectedStates.last()
        assertThat(latestState).isNotNull()
        assertThat(latestState!!.navigationDrawerItems).isEqualTo(expectedItems)
        assertThat(latestState.showSnackbar).isFalse()

        job.cancelAndJoin()
    }

    @Test
    fun `loadNavigationItems failure shows snackbar`() = runTest(dispatcher.scheduler) {
        val errorMessage = "Failed to fetch navigation"
        every { navigationRepository.getNavigationDrawerItems() } returns flow {
            throw IllegalStateException(errorMessage)
        }

        val viewModel = TestMainViewModel(navigationRepository)
        val collectedStates = mutableListOf<UiMainScreen?>()
        val job = launch { viewModel.exposedScreenState().collect { collectedStates.add(it.data) } }

        advanceUntilIdle()

        assertThat(collectedStates).isNotEmpty()
        val latestState = collectedStates.last()
        assertThat(latestState).isNotNull()
        assertThat(latestState!!.showSnackbar).isTrue()
        assertThat(latestState.snackbarMessage).isEqualTo(errorMessage)

        job.cancelAndJoin()
    }

    private fun createIcon(): ImageVector = ImageVector.Builder(
        name = "test",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).build()

    private class TestMainViewModel(
        navigationRepository: NavigationRepository,
    ) : MainViewModel(navigationRepository) {
        fun exposedScreenState(): MutableStateFlow<UiStateScreen<UiMainScreen>> = screenState
    }
}
