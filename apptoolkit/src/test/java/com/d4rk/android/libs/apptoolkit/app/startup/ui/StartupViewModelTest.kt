package com.d4rk.android.libs.apptoolkit.app.startup.ui

import app.cash.turbine.test
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupAction
import com.d4rk.android.libs.apptoolkit.app.startup.domain.actions.StartupEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `initial state is loading and consent not loaded`() = runTest(dispatcherExtension.testDispatcher) {
        val state = StartupViewModel().uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
        assertThat(state.data?.consentFormLoaded).isFalse()
    }

    @Test
    fun `consent event updates state`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = StartupViewModel()
        viewModel.onEvent(StartupEvent.ConsentFormLoaded)
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.consentFormLoaded).isTrue()
    }

    @Test
    fun `repeat consent events keep state successful`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = StartupViewModel()
        viewModel.onEvent(StartupEvent.ConsentFormLoaded)
        viewModel.onEvent(StartupEvent.ConsentFormLoaded)
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.consentFormLoaded).isTrue()
    }

    @Test
    fun `continue event emits navigation action`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = StartupViewModel()
        viewModel.actionEvent.test {
            viewModel.onEvent(StartupEvent.Continue)
            assertThat(awaitItem()).isEqualTo(StartupAction.NavigateNext)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
