package com.d4rk.android.libs.apptoolkit.app.settings.settings.ui

import com.d4rk.android.libs.apptoolkit.app.settings.settings.domain.model.SettingsConfig
import com.d4rk.android.libs.apptoolkit.app.settings.utils.interfaces.SettingsProvider
import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.extension.RegisterExtension

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
        viewModel = SettingsViewModel(provider)
    }
}