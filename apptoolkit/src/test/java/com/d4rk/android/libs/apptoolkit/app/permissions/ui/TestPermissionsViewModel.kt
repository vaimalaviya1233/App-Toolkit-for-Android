package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsPreference
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertFailsWith

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

    @Test
    fun `load permissions valid after error`() = runTest(dispatcherExtension.testDispatcher) {
        val errorConfig = SettingsConfig(title = "title", categories = emptyList())
        val successConfig = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns errorConfig andThen successConfig
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(1)
    }

    @Test
    fun `load permissions error after success`() = runTest(dispatcherExtension.testDispatcher) {
        val successConfig = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        val errorConfig = SettingsConfig(title = "title", categories = emptyList())
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns successConfig andThen errorConfig
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `load permissions provider returns null`() = runTest(dispatcherExtension.testDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns mockk<SettingsConfig>(relaxed = true).copy(
            categories = emptyList()
        )
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        assertFailsWith<NullPointerException> {
            viewModel.onEvent(PermissionsEvent.Load(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `concurrent load events yield latest state`() = runTest(dispatcherExtension.testDispatcher) {
        val first = SettingsConfig(title = "first", categories = listOf(SettingsCategory(title = "one")))
        val second = SettingsConfig(title = "second", categories = emptyList())
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returnsMany listOf(first, second)
        viewModel = PermissionsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `load permissions with malformed data`() = runTest(dispatcherExtension.testDispatcher) {
        val malformed = SettingsCategory(
            title = "",
            preferences = listOf(SettingsPreference(key = null, title = null))
        )
        val config = SettingsConfig(title = "bad", categories = listOf(malformed))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.first()?.preferences?.size).isEqualTo(1)
    }
}
