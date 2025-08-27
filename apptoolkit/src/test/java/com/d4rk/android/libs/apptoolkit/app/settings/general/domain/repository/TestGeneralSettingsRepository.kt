package com.d4rk.android.libs.apptoolkit.app.settings.general.domain.repository

import com.d4rk.android.libs.apptoolkit.core.utils.dispatchers.UnconfinedDispatcherExtension
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension

class TestGeneralSettingsRepository {

    companion object {
        @JvmField
        @RegisterExtension
        val dispatcherExtension = UnconfinedDispatcherExtension()
    }

    @Test
    fun `getContentKey returns provided key`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = DefaultGeneralSettingsRepository(dispatcherExtension.testDispatcher)
        val result = repository.getContentKey("valid")
        assertThat(result).isEqualTo("valid")
    }

    @Test
    fun `getContentKey throws on null key`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = DefaultGeneralSettingsRepository(dispatcherExtension.testDispatcher)
        assertThrows<IllegalArgumentException> {
            repository.getContentKey(null)
        }
    }

    @Test
    fun `getContentKey throws on blank key`() = runTest(dispatcherExtension.testDispatcher) {
        val repository = DefaultGeneralSettingsRepository(dispatcherExtension.testDispatcher)
        assertThrows<IllegalArgumentException> {
            repository.getContentKey("")
        }
    }
}
