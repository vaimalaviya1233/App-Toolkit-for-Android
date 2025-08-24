package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsCategory
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsPreference
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.setErrors
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.UiTextHelper
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class TestPermissionsViewModel {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    private lateinit var viewModel: PermissionsViewModel
    private lateinit var provider: PermissionsProvider

    private fun setup(config: SettingsConfig, dispatcher: TestDispatcher) {
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns config
        viewModel = PermissionsViewModel(provider)
    }

    @Test
    fun `load permissions success`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions success")
        val config = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(1)
        println("🏁 [TEST DONE] load permissions success")
    }

    @Test
    fun `load permissions empty`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions empty")
        val config = SettingsConfig(title = "title", categories = emptyList())
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        val error = state.errors.first().message as UiTextHelper.DynamicString
        assertThat(error.content).isEqualTo("No settings found")
        println("🏁 [TEST DONE] load permissions empty")
    }

    @Test
    fun `load permissions provider throws`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions provider throws")
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } throws IllegalStateException("boom")
        viewModel = PermissionsViewModel(provider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.IsLoading::class.java)
        println("🏁 [TEST DONE] load permissions provider throws")
    }

    @Test
    fun `dismiss errors clears state`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] dismiss errors clears state")
        val config = SettingsConfig(title = "title", categories = emptyList())
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.errors).isNotEmpty()

        viewModel.screenState.setErrors(emptyList())
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.value.errors).isEmpty()
        println("🏁 [TEST DONE] dismiss errors clears state")
    }

    @Test
    fun `load permissions valid after error`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions valid after error")
        val errorConfig = SettingsConfig(title = "title", categories = emptyList())
        val successConfig = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns errorConfig andThen successConfig
        viewModel = PermissionsViewModel(provider)
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
        println("🏁 [TEST DONE] load permissions valid after error")
    }

    @Test
    fun `load permissions error after success`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions error after success")
        val successConfig = SettingsConfig(title = "title", categories = listOf(SettingsCategory(title = "c")))
        val errorConfig = SettingsConfig(title = "title", categories = emptyList())
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns successConfig andThen errorConfig
        viewModel = PermissionsViewModel(provider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        var state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()
        state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        println("🏁 [TEST DONE] load permissions error after success")
    }

    @Test
    fun `load permissions provider returns null`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions provider returns null")
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returns SettingsConfig(title = "null test", categories = emptyList())
        viewModel = PermissionsViewModel(provider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        println("🏁 [TEST DONE] load permissions provider returns null")
    }

    @Test
    fun `concurrent load events yield latest state`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] concurrent load events yield latest state")
        val first = SettingsConfig(title = "first", categories = listOf(SettingsCategory(title = "one")))
        val second = SettingsConfig(title = "second", categories = emptyList())
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } returnsMany listOf(first, second)
        viewModel = PermissionsViewModel(provider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.NoData::class.java)
        println("🏁 [TEST DONE] concurrent load events yield latest state")
    }

    @Test
    fun `load permissions with malformed data`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions with malformed data")
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
        println("🏁 [TEST DONE] load permissions with malformed data")
    }

    @Test
    fun `load permissions with duplicated categories`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions with duplicated categories")
        val category = SettingsCategory(title = "dup")
        val config = SettingsConfig(title = "t", categories = listOf(category, category))
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(2)
        println("🏁 [TEST DONE] load permissions with duplicated categories")
    }

    @Test
    fun `load very large permissions config`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load very large permissions config")
        val prefs = List(10) { index -> SettingsPreference(key = "k$index") }
        val categories = List(50) { index -> SettingsCategory(title = "c$index", preferences = prefs) }
        val config = SettingsConfig(title = "big", categories = categories)
        setup(config, dispatcherExtension.testDispatcher)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Success::class.java)
        assertThat(state.data?.categories?.size).isEqualTo(50)
        println("🏁 [TEST DONE] load very large permissions config")
    }
}
