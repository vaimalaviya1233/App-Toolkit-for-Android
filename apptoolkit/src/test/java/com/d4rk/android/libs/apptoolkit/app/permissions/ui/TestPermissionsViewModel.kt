package com.d4rk.android.libs.apptoolkit.app.permissions.ui

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.permissions.domain.actions.PermissionsEvent
import com.d4rk.android.libs.apptoolkit.app.permissions.utils.interfaces.PermissionsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.core.domain.model.ui.ScreenState
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
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
    fun `load permissions provider throws`() = runTest(dispatcherExtension.testDispatcher) {
        println("🚀 [TEST] load permissions provider throws")
        provider = mockk()
        every { provider.providePermissionsConfig(any()) } throws IllegalStateException("boom")
        viewModel = PermissionsViewModel(provider)
        val context = mockk<Context>(relaxed = true)

        viewModel.onEvent(PermissionsEvent.Load(context))
        dispatcherExtension.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.screenState).isInstanceOf(ScreenState.Error::class.java)
        assertThat(state.errors).isNotEmpty()
        println("🏁 [TEST DONE] load permissions provider throws")
    }
}
