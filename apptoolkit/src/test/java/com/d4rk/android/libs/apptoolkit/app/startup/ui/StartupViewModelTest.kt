package com.d4rk.android.libs.apptoolkit.app.startup.ui

import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupAction
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {
    @Test
    fun `consent event updates state`() = runTest {
        val viewModel = StartupViewModel()
        viewModel.onEvent(StartupEvent.ConsentFormLoaded)
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.consentFormLoaded).isTrue()
    }

    @Test
    fun `continue event emits navigation action`() = runTest {
        val viewModel = StartupViewModel()
        val actions = mutableListOf<StartupAction>()
        val job = launch { viewModel.actionEvent.collect { actions.add(it) } }
        viewModel.onEvent(StartupEvent.Continue)
        advanceUntilIdle()
        assertThat(actions).containsExactly(StartupAction.NavigateNext)
        job.cancel()
    }
}
