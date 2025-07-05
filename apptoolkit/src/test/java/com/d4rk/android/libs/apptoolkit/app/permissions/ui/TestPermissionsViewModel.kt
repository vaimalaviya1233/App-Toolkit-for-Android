package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestPermissionsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    private lateinit var dispatcherProvider: TestDispatchers
    private lateinit var viewModel: PermissionsViewModel
    private lateinit var provider: PermissionsProvider

    private fun setup(config: SettingsConfig, dispatcher: TestDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns config
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
    }

    @Test
    fun `load permissions success`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(1)
    }

    @Test
    fun `load permissions empty`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "title", categories = emptyList())
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        val error = state.errors.first().message as UiTextHelper.DynamicString
        assertThat(error.content).isEqualTo("No settings found")
    }

    @Test
    fun `load permissions provider throws`() = runTest(dispatcherExtension.testDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } throws IllegalStateException("boom")
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        assertFailsWith<IllegalStateException> {
            viewModel.onEvent(PermissionsEvent.Load(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `dismiss errors clears state`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "title", categories = emptyList())
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.errors).isNotEmpty()

        viewModel.screenState.setErrors(emptyList())
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.errors).isEmpty()
    }
}
