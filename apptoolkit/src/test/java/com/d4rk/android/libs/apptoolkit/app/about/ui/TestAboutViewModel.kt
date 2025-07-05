package com.d4rk.android.libs.apptoolkit.app.about.ui

import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.actions.AboutEvents
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlinx.coroutines.test.runTest

class TestAboutViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    @Test
    fun `copy device info shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        val viewModel = AboutViewModel(dispatcherProvider)
        viewModel.onEvent(AboutEvents.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.data?.showDeviceInfoCopiedSnackbar).isTrue()
        val snackbar = state.snackbar!!
        val msg = snackbar.message as UiTextHelper.StringResource
        assertThat(msg.resourceId).isEqualTo(R.string.snack_device_info_copied)
    }

    @Test
    fun `dismiss snackbar resets state`() = runTest(dispatcherExtension.testDispatcher) {
        val dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        val viewModel = AboutViewModel(dispatcherProvider)

        viewModel.onEvent(AboutEvents.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        viewModel.onEvent(AboutEvents.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.snackbar).isNull()
        assertThat(state.data?.showDeviceInfoCopiedSnackbar).isFalse()
    }

    @Test
    fun `snackbar can be shown again after dismissal`() = runTest(dispatcherExtension.testDispatcher) {
        val dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        val viewModel = AboutViewModel(dispatcherProvider)

        viewModel.onEvent(AboutEvents.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AboutEvents.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.snackbar).isNull()

        viewModel.onEvent(AboutEvents.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterSecondCopy = viewModel.uiState.value
        assertThat(stateAfterSecondCopy.snackbar).isNotNull()
        assertThat(stateAfterSecondCopy.data?.showDeviceInfoCopiedSnackbar).isTrue()

        viewModel.onEvent(AboutEvents.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val finalState = viewModel.uiState.value
        assertThat(finalState.snackbar).isNull()
        assertThat(finalState.data?.showDeviceInfoCopiedSnackbar).isFalse()

        // dismissing again should keep snackbar hidden
        viewModel.onEvent(AboutEvents.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
        assertThat(viewModel.uiState.value.data?.showDeviceInfoCopiedSnackbar).isFalse()
    }
}
