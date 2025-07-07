package com.d4rk.android.apps.apptoolkit.app.main

import com.d4rk.android.apps.apptoolkit.app.core.MainDispatcherExtension
import com.d4rk.android.apps.apptoolkit.app.core.TestDispatchers
import com.d4rk.android.apps.apptoolkit.app.main.domain.action.MainEvent
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainViewModel
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.main.domain.usecases.PerformInAppUpdateUseCase
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.DataState
import com.d4rk.android.libs.apptoolkit.core.domain.model.network.Errors
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.dismissSnackbar
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.updateData
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertFailsWith

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

    @Test
    fun `dismiss snackbar clears state`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Error<Int, Errors>(error = Errors.UseCase.FAILED_TO_UPDATE_APP))
        }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        viewModel.screenState.dismissSnackbar()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `multiple update attempts error then success`() = runTest(dispatcherExtension.testDispatcher) {
        val errorFlow = flow {
            emit(DataState.Loading())
            emit(DataState.Error<Int, Errors>(error = Errors.Network.REQUEST_TIMEOUT))
        }
        val successFlow = flow {
            emit(DataState.Loading())
            emit(DataState.Success<Int, Errors>(0))
        }

        setup(errorFlow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        coEvery { updateUseCase.invoke(Unit) } returns successFlow
        viewModel.screenState.dismissSnackbar()
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `handle different update errors`() = runTest(dispatcherExtension.testDispatcher) {
        val timeoutFlow = flow {
            emit(DataState.Error<Int, Errors>(error = Errors.Network.REQUEST_TIMEOUT))
        }
        setup(timeoutFlow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        viewModel.screenState.dismissSnackbar()
        val authFlow = flow {
            emit(DataState.Error<Int, Errors>(error = Errors.Network.NO_INTERNET))
        }
        coEvery { updateUseCase.invoke(Unit) } returns authFlow
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()
    }

    @Test
    fun `repeated navigation event does not duplicate items`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow<DataState<Int, Errors>> { }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.LoadNavigation)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val firstItems = viewModel.uiState.value.data?.navigationDrawerItems

        viewModel.onEvent(MainEvent.LoadNavigation)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val secondItems = viewModel.uiState.value.data?.navigationDrawerItems

        assertThat(firstItems?.size).isEqualTo(4)
        assertThat(secondItems?.size).isEqualTo(4)
    }

    @Test
    fun `loading update emits nothing else`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Loading<Int, Errors>())
        }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `unexpected update result is ignored`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow {
            emit(DataState.Success<Int, Errors>(5))
        }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.onEvent(MainEvent.CheckForUpdates)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `use case throws propagates exception`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow<DataState<Int, Errors>> { }
        setup(flow, dispatcherExtension.testDispatcher)
        coEvery { updateUseCase.invoke(Unit) } throws RuntimeException("boom")

        assertFailsWith<RuntimeException> {
            viewModel.onEvent(MainEvent.CheckForUpdates)
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `load navigation replaces invalid config`() = runTest(dispatcherExtension.testDispatcher) {
        val flow = flow<DataState<Int, Errors>> { }
        setup(flow, dispatcherExtension.testDispatcher)
        viewModel.screenState.updateData(ScreenState.Success()) { it.copy(navigationDrawerItems = emptyList()) }

        viewModel.onEvent(MainEvent.LoadNavigation)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val items = viewModel.uiState.value.data?.navigationDrawerItems
        assertThat(items?.size).isEqualTo(4)
    }
}
