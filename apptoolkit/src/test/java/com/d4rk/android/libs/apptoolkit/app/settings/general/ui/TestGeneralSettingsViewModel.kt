package com.d4rk.android.libs.apptoolkit.app.settings.general.ui

import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.d4rk.android.libs.apptoolkit.app.settings.general.domain.actions.GeneralSettingsEvent
import com.d4rk.android.libs.apptoolkit.app.settings.general.ui.GeneralSettingsViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestGeneralSettingsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    @Test
    fun `load content success`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()
        viewModel.onEvent(GeneralSettingsEvent.Load("key"))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.contentKey).isEqualTo("key")
    }

    @Test
    fun `load content invalid`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()
        viewModel.onEvent(GeneralSettingsEvent.Load(null))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        val error = state.errors.first().message as UiTextHelper.StringResource
        assertThat(error.resourceId).isEqualTo(R.string.error_invalid_content_key)
    }

    @Test
    fun `load content blank`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()
        viewModel.onEvent(GeneralSettingsEvent.Load(""))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        val error = state.errors.first().message as UiTextHelper.StringResource
        assertThat(error.resourceId).isEqualTo(R.string.error_invalid_content_key)
    }

    @Test
    fun `state transitions loading success error`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()

        viewModel.onEvent(GeneralSettingsEvent.Load("key"))
        // Immediately after triggering, state should be loading
        var state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)

        viewModel.onEvent(GeneralSettingsEvent.Load(""))
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `multiple load calls update key`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()
        viewModel.onEvent(GeneralSettingsEvent.Load("one"))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.data?.contentKey).isEqualTo("one")

        viewModel.onEvent(GeneralSettingsEvent.Load("two"))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.data?.contentKey).isEqualTo("two")
    }

    @Test
    fun `errors cleared after successful load`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = GeneralSettingsViewModel()
        viewModel.onEvent(GeneralSettingsEvent.Load(""))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.errors).isNotEmpty()

        viewModel.onEvent(GeneralSettingsEvent.Load("valid"))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.errors).isEmpty()
    }
}
