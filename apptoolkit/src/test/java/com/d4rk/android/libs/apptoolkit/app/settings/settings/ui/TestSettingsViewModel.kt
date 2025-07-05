package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.actions.SettingsEvent
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.ui.SettingsViewModel
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
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

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
        every { provider.provideSettingsConfig(any()) } returns null as SettingsConfig
        viewModel = SettingsViewModel(provider, dispatcherProvider)
        val context = mockk<Context>(relaxed = true)

        assertFailsWith<NullPointerException> {
            viewModel.onEvent(SettingsEvent.Load(context))
            dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        }
    }
}
