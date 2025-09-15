package com.d4rk.android.apps.apptoolkit.core.di

import android.content.Context
import com.d4rk.android.apps.apptoolkit.core.di.modules.adsModule
import com.d4rk.android.apps.apptoolkit.core.di.modules.appModule
import com.d4rk.android.apps.apptoolkit.core.di.modules.appToolkitModule
import com.d4rk.android.apps.apptoolkit.core.di.modules.dispatchersModule
import com.d4rk.android.apps.apptoolkit.core.di.modules.settingsModule
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.KoinAppDeclaration
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import kotlin.test.assertEquals
import kotlin.test.assertSame

class InitializeKoinTest {

    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `initializeKoin can be invoked multiple times and registers expected modules`() {
        mockkStatic("org.koin.core.context.GlobalContextKt")
        mockkStatic("org.koin.android.ext.koin.KoinExtKt")

        val context = mockk<Context>(relaxed = true)
        val koinApplication = mockk<KoinApplication>(relaxed = true)
        val koin = mockk<Koin>(relaxed = true)
        val registeredModules = mutableListOf<List<Module>>()

        every { koinApplication.androidContext(any()) } returns koinApplication
        every { koinApplication.modules(any<List<Module>>()) } answers {
            registeredModules += firstArg<List<Module>>()
            koinApplication
        }
        every { startKoin(any()) } answers {
            val declaration = firstArg<KoinAppDeclaration>()
            declaration.invoke(koinApplication)
            koin
        }

        assertDoesNotThrow {
            initializeKoin(context)
            initializeKoin(context)
        }

        val expectedModules = listOf(appModule, settingsModule, adsModule, appToolkitModule, dispatchersModule)
        assertEquals(2, registeredModules.size)
        registeredModules.forEach { modules ->
            assertEquals(expectedModules.size, modules.size)
            expectedModules.forEachIndexed { index, module ->
                assertSame(module, modules[index])
            }
        }

        verify(exactly = 2) { startKoin(any()) }
        verify(exactly = 2) { koinApplication.androidContext(context) }
        verify(exactly = 2) { koinApplication.modules(any<List<Module>>()) }
    }
}
