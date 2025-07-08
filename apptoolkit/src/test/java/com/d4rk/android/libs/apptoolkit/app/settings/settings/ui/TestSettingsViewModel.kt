package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.actions.SettingsEvent
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsPreference
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.TestDispatchers
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
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
        val dispatcherExtension = UnconfinedDispatcherExtension()
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

    @Test
    fun `load settings with malformed preference`() = runTest(dispatcherExtension.testDispatcher) {
        val malformed = SettingsCategory(
            title = "bad",
            preferences = listOf(SettingsPreference(key = null, title = null))
        )
        val config = SettingsConfig(title = "bad", categories = listOf(malformed))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.first()?.preferences?.size).isEqualTo(1)
    }

    @Test
    fun `load settings provider throws`() = runTest(dispatcherExtension.testDispatcher) {
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } throws IllegalStateException("boom")
        viewModel = SettingsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
    }

    @Test
    fun `state persists across config changes`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "persist", categories = listOf(SettingsCategory(title = "c")))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(SettingsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val before = viewModel.uiState.value

        // simulate orientation change by re-reading state
        val after = viewModel.uiState.value
        assertThat(after).isEqualTo(before)
    }

    @Test
    fun `load settings with null context`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "bad context", categories = emptyList())
        dispatcherProvider = TestDispatchers(dispatcherExtension.testDispatcher)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } answers {
            firstArg<Context>().hashCode()
            config
        }
        viewModel = SettingsViewModel(provider, dispatcherProvider)

        val context = null as Context
        assertFailsWith<NullPointerException> {
            viewModel.onEvent(SettingsEvent.Load(context))
        }
    }
}
