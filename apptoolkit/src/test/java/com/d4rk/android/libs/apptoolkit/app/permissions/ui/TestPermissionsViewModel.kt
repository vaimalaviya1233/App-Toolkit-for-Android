package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import android.content.Context
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.Test
import kotlin.OptIn

@OptIn(ExperimentalCoroutinesApi::class)
class TestPermissionsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    private lateinit var viewModel: PermissionsViewModel
    private lateinit var provider: PermissionsProvider

    private fun setup(config: SettingsConfig? = null, error: Throwable? = null, dispatcher: TestDispatcher) {
        provider = mockk()
        if (error != null) {
            every { provider.providePermissionsConfig(any()) } returns flow { throw error }
        } else {
            every { provider.providePermissionsConfig(any()) } returns flowOf(config!!)
        }
        viewModel = PermissionsViewModel(provider, dispatcher)
    }

    @Test
    fun `load permissions success`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "P", categories = listOf(SettingsCategory(title = "c", preferences = emptyList())))
        val context = mockk<Context>(relaxed = true)
        setup(config = config, dispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(PermissionsEvent.Load(context))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.data?.title).isEqualTo("P")
        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.Success::class.java)
    }

    @Test
    fun `load permissions error`() = runTest(dispatcherExtension.testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        setup(error = RuntimeException("fail"), dispatcher = dispatcherExtension.testDispatcher)

        viewModel.onEvent(PermissionsEvent.Load(context))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.Error::class.java)
    }
}
