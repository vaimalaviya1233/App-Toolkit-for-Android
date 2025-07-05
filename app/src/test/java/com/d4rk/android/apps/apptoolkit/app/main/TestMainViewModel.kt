package com.d4rk.android.apps.apptoolkit.app.main

import com.d4rk.android.apps.apptoolkit.app.core.MainDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.TestDispatchers
import com.d4rk.android.apps.apptoolkit.app.main.domain.action.MainEvent
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.main.domain.usecases.PerformInAppUpdateUseCase
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import com.google.common.truth.Truth.assertThat

class TestMainViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    private lateinit var dispatcherProvider: TestDispatchers
    private lateinit var viewModel: MainViewModel
    private lateinit var updateUseCase: PerformInAppUpdateUseCase

    private fun setup(flow: Flow<DataState<Int, Errors>>, dispatcher: TestDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcher)
        updateUseCase = mockk()
        coEvery { updateUseCase.invoke(Unit) } returns flow
        viewModel = MainViewModel(updateUseCase, dispatcherProvider)
    }

    @Test
    fun `navigation items loaded on init`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow<DataState<Int, Errors>> { }
        setup(flow, dispatcherExtension.testDispatcher)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val items = viewModel.uiState.value.data?.navigationDrawerItems
        assertThat(items?.size).isEqualTo(4)
    }

    @Test
    fun `check update error shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Error<Int, Errors>(error = Errors.UseCase.FAILED_TO_UPDATE_APP))
        }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val snackbar = viewModel.uiState.value.snackbar
        assertThat(snackbar).isNotNull()
        val msg = snackbar!!.message as UiTextHelper.StringResource
        assertThat(msg.resourceId).isEqualTo(R.string.snack_update_failed)
    }

    @Test
    fun `check update success`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Success<Int, Errors>(0))
        }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val snackbar = viewModel.uiState.value.snackbar
        assertThat(snackbar).isNull()
    }

    @Test
    fun `load navigation via event`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow<DataState<Int, Errors>> { }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.LoadNavigation)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val items = viewModel.uiState.value.data?.navigationDrawerItems
        assertThat(items?.size).isEqualTo(4)
    }
}
