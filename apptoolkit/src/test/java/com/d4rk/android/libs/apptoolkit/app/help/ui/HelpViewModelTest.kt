package com.d4rk.android.libs.apptoolkit.app.help.ui

import com.d4rk.android.libs.apptoolkit.app.help.domain.actions.HelpEvent
import com.d4rk.android.libs.apptoolkit.app.help.domain.data.model.UiHelpQuestion
import com.d4rk.android.libs.apptoolkit.app.help.domain.repository.HelpRepository
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class HelpViewModelTest {

    @Test
    fun `loadFaq sets success state when repository returns data`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = object : HelpRepository {
                override fun fetchFaq() = flowOf(listOf(UiHelpQuestion("Q", "A")))
            }
            val viewModel = HelpViewModel(repository)
            viewModel.onEvent(HelpEvent.LoadFaq)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.screenState is ScreenState.Success)
            assertEquals(1, viewModel.uiState.value.data?.questions?.size)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `loadFaq sets error state when repository throws`() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = object : HelpRepository {
                override fun fetchFaq() = flow<List<UiHelpQuestion>> { throw IOException("error") }
            }
            val viewModel = HelpViewModel(repository)
            viewModel.onEvent(HelpEvent.LoadFaq)
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.screenState is ScreenState.Error)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

