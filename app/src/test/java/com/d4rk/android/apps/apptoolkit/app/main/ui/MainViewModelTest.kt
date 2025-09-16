package com.d4rk.android.apps.apptoolkit.app.main.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.d4rk.android.apps.apptoolkit.app.core.utils.dispatchers.StandardDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.main.domain.model.ui.UiMainScreen
import com.d4rk.android.libs.apptoolkit.app.main.domain.repository.NavigationRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.navigation.NavigationDrawerItem
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = StandardDispatcherExtension()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initialization triggers navigation load`() = runTest(dispatcherExtension.testDispatcher) {
        val navigationRepository = mockk<NavigationRepository>()
        every { navigationRepository.getNavigationDrawerItems() } returns emptyFlow()

        MainViewModel(navigationRepository)

        advanceUntilIdle()

        verify(exactly = 1) { navigationRepository.getNavigationDrawerItems() }
    }

    @Test
    fun `successful navigation load populates drawer items`() = runTest(dispatcherExtension.testDispatcher) {
        val navigationRepository = mockk<NavigationRepository>()
        val expectedItems = listOf(
            NavigationDrawerItem(
                title = 1,
                selectedIcon = createIcon(),
                route = "route"
            )
        )
        every { navigationRepository.getNavigationDrawerItems() } returns flowOf(expectedItems)

        val viewModel = MainViewModel(navigationRepository)

        advanceUntilIdle()

        assertEquals(
            UiMainScreen(navigationDrawerItems = expectedItems),
            viewModel.uiState.value.data
        )
    }

    @Test
    fun `navigation load error shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val navigationRepository = mockk<NavigationRepository>()
        val error = IllegalStateException("boom")
        every { navigationRepository.getNavigationDrawerItems() } returns flow { throw error }

        val viewModel = MainViewModel(navigationRepository)

        advanceUntilIdle()

        assertEquals(
            UiMainScreen(showSnackbar = true, snackbarMessage = "boom"),
            viewModel.uiState.value.data
        )
    }

    private fun createIcon(): ImageVector = ImageVector.Builder(
        name = "navigation_icon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).build()
}
