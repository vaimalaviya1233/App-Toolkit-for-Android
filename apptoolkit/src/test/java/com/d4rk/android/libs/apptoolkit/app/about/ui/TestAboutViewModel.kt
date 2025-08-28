package com.d4rk.android.libs.apptoolkit.app.about.ui

import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.about.domain.actions.AboutEvent
import com.d4rk.android.libs.apptoolkit.app.about.data.DefaultAboutRepository
import com.d4rk.android.libs.apptoolkit.app.about.domain.model.ui.UiAboutScreen
import com.d4rk.android.libs.apptoolkit.app.about.domain.repository.AboutRepository
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestAboutViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    private val deviceProvider = object : AboutSettingsProvider {
        override val deviceInfo: String = "device-info"
    }

    private val buildInfoProvider = object : BuildInfoProvider {
        override val appVersion: String = "1.0"
        override val appVersionCode: Int = 1
        override val packageName: String = "pkg"
        override val isDebugBuild: Boolean = false
    }

    private fun createViewModel(): AboutViewModel =
        AboutViewModel(
            repository = DefaultAboutRepository(
                deviceProvider = deviceProvider,
                configProvider = buildInfoProvider,
                ioDispatcher = dispatcherExtension.testDispatcher,
            ),
        )

    private fun createFailingViewModel(): AboutViewModel =
        AboutViewModel(
            repository = object : AboutRepository {
                override fun getAboutInfoStream(): Flow<UiAboutScreen> = flow {
                    throw Exception("fail")
                }
            },
        )

    @Test
    fun `copy device info shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.data?.deviceInfo).isEqualTo(deviceProvider.deviceInfo)
        val snackbar = state.snackbar!!
        val msg = snackbar.message as UiTextHelper.StringResource
        assertThat(msg.resourceId).isEqualTo(R.string.snack_device_info_copied)
    }

    @Test
    fun `dismiss snackbar resets state`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        viewModel.onEvent(AboutEvent.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()
    }

    @Test
    fun `snackbar can be shown again after dismissal`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(AboutEvent.DismissSnackbar)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNull()

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()
    }

    @Test
    fun `repeated copy events show snackbar each time`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val first = viewModel.uiState.value.snackbar!!.timeStamp

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val second = viewModel.uiState.value.snackbar!!.timeStamp

        assertThat(second).isGreaterThan(first)
    }

    @Test
    fun `rapid successive copy events keep snackbar visible`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        repeat(5) { viewModel.onEvent(AboutEvent.CopyDeviceInfo) }
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.value.snackbar).isNotNull()
    }

    @Test
    fun `repository error shows snackbar`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createFailingViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val snackbar = viewModel.uiState.value.snackbar!!
        val msg = snackbar.message as UiTextHelper.StringResource
        assertThat(msg.resourceId).isEqualTo(R.string.snack_device_info_failed)
    }

    @Test
    fun `new viewmodel has default state`() = runTest(dispatcherExtension.testDispatcher) {
        val viewModel = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(AboutEvent.CopyDeviceInfo)
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.snackbar).isNotNull()

        val recreated = createViewModel()
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = recreated.uiState.value
        assertThat(state.snackbar).isNull()
        assertThat(state.data?.deviceInfo).isEqualTo(deviceProvider.deviceInfo)
    }
}
