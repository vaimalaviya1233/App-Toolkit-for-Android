package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import io.mockk.every
import kotlinx.coroutines.ExperimentalCoroutinesApi
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.actions.SettingsEvent
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.Test
@OptIn(ExperimentalCoroutinesApi::class)

class TestSettingsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    private lateinit var viewModel: SettingsViewModel
    private lateinit var provider: SettingsProvider

    private fun setup(config: SettingsConfig, dispatcher: TestDispatcher) {
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returns config
        viewModel = SettingsViewModel(provider, dispatcher)
    }

    @Test
    fun `load settings success`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "Title", categories = listOf(SettingsCategory(title = "c", preferences = emptyList())))
        val context = mockk<Context>(relaxed = true)
        setup(config, dispatcherExtension.testDispatcher)

        viewModel.onEvent(SettingsEvent.Load(context))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.data?.title).isEqualTo("Title")
        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.Success::class.java)
    }

    @Test
    fun `load settings no data`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "", categories = emptyList())
        val context = mockk<Context>(relaxed = true)
        setup(config, dispatcherExtension.testDispatcher)

        viewModel.onEvent(SettingsEvent.Load(context))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.NoData::class.java)
    }

    @Test
    fun `load settings clears previous errors on success`() = runTest(dispatcherExtension.testDispatcher) {
        val empty = SettingsConfig(title = "", categories = emptyList())
        val valid = SettingsConfig(title = "Title", categories = listOf(SettingsCategory(title = "c", preferences = emptyList())))
        val context = mockk<Context>(relaxed = true)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returnsMany listOf(empty, valid)
        viewModel = SettingsViewModel(provider, dispatcherExtension.testDispatcher)

        viewModel.onEvent(SettingsEvent.Load(context))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.NoData::class.java)
        assertThat(viewModel.uiState.value.errors).isNotEmpty()

        viewModel.onEvent(SettingsEvent.Load(context))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(viewModel.uiState.value.errors).isEmpty()
    }

    @Test
    fun `provider called once per load event`() = runTest(dispatcherExtension.testDispatcher) {
        val config = SettingsConfig(title = "Title", categories = listOf(SettingsCategory(title = "c", preferences = emptyList())))
        val context = mockk<Context>(relaxed = true)
        provider = mockk()
        every { provider.provideSettingsConfig(any()) } returns config
        viewModel = SettingsViewModel(provider, dispatcherExtension.testDispatcher)

        viewModel.onEvent(SettingsEvent.Load(context))
        advanceUntilIdle()

        verify(exactly = 1) { provider.provideSettingsConfig(context) }
    }
}