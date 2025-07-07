package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.actions.SettingsEvent
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsPreference
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.core.MainDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.assertFailsWith

class TestSettingsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = MainDispatcherExtension()
    }

    private lateinit var dispatcherProvider: TestDispatchers
    private lateinit var viewModel: SettingsViewModel
    private lateinit var provider: SettingsProvider

    private fun setup(config: SettingsConfig, dispatcher: TestDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returns config
        viewModel = SettingsViewModel(provider, dispatcherProvider)
    }

    @Test
    fun `load settings success`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(1)
    }

    @Test
    fun `load settings empty`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "title", categories = emptyList())
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        val error = state.errors.first().message as UiTextHelper.StringResource
        assertThat(error.resourceId).isEqualTo(R.string.error_no_settings_found)
    }

    @Test
    fun `load settings provider throws`() = runTest(dispatcherExtension.testDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } throws IllegalStateException("boom")
        viewModel = SettingsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        assertFailsWith<IllegalStateException> {
            viewModel.onEvent(SettingsEvent.Load(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `load settings provider returns null`() = runTest(dispatcherExtension.testDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returns SettingsConfig(title = "null test", categories = emptyList())
        viewModel = SettingsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `sequential loads reflect latest config`() = runTest(dispatcherExtension.testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returnsMany listOf(
            SettingsConfig(title = "first", categories = listOf(SettingsCategory(title = "one"))),
            SettingsConfig(title = "second", categories = emptyList())
        )
        viewModel = SettingsViewModel(provider, dispatcherProvider)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.title).isEqualTo("first")

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `state clears errors after subsequent success`() = runTest(dispatcherExtension.testDispatcher) {
        val context = mockk<Context>(relaxed = true)
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returnsMany listOf(
            SettingsConfig(title = "bad", categories = emptyList()),
            SettingsConfig(title = "good", categories = listOf(SettingsCategory(title = "c")))
        )
        viewModel = SettingsViewModel(provider, dispatcherProvider)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.NoData::class.java)
        assertThat(viewModel.uiState.value.errors).isNotEmpty()

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.errors).isEmpty()
    }

    @Test
    fun `provider returns partial config`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "", categories = listOf(SettingsCategory()))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.title).isEmpty()
    }

    @Test
    fun `load settings with duplicated categories`() = runTest(dispatcherExtension.testDispatcher) {
        val category = SettingsCategory(title = "dup")
        val config = SettingsConfig(title = "t", categories = listOf(category, category))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(2)
    }

    @Test
    fun `load very large settings config`() = runTest(dispatcherExtension.testDispatcher) {
        val prefs = List(10) { index -> SettingsPreference(key = "k$index") }
        val categories = List(50) { index -> SettingsCategory(title = "c$index", preferences = prefs) }
        val config = SettingsConfig(title = "big", categories = categories)
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(50)
    }
}
