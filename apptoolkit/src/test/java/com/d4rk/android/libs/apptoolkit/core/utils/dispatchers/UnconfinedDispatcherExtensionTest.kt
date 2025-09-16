package com.d4rk.android.libs.apptoolkit.core.utils.dispatchers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class UnconfinedDispatcherExtensionTest {

    private val extension = UnconfinedDispatcherExtension()

    @Test
    fun `beforeEach pins Main dispatcher to the shared UnconfinedTestDispatcher`() {
        val sharedDispatcher = extension.testDispatcher

        extension.beforeEach(null)

        try {
            assertInstanceOf(UnconfinedTestDispatcher::class.java, sharedDispatcher)
            assertSame(sharedDispatcher, extension.testDispatcher)
            assertSame(sharedDispatcher, Dispatchers.Main)
        } finally {
            extension.afterEach(null)
        }
    }
}
