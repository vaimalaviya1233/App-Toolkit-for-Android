package com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers

import android.content.Context
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers.PermissionsSettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsSettingsRepositoryTest {

    @Test
    fun getPermissionsConfig_emitsExpectedStructure() = runTest {
        val context = mockk<Context>()
        every { context.getString(any()) } returns ""

        val repository = PermissionsSettingsRepository(context, UnconfinedTestDispatcher(testScheduler))
        val config = repository.getPermissionsConfig().first()

        assertEquals(3, config.categories.size)
        assertEquals(7, config.categories[0].preferences.size)
        assertEquals(1, config.categories[1].preferences.size)
        assertEquals(1, config.categories[2].preferences.size)
    }
}

