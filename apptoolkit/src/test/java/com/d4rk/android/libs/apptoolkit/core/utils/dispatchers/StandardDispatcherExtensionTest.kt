package com.d4rk.android.libs.apptoolkit.core.utils.dispatchers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class StandardDispatcherExtensionTest {

    private val extension = StandardDispatcherExtension()

    @Test
    fun `beforeEach sets Main dispatcher to a StandardTestDispatcher`() {
        extension.beforeEach(null)

        try {
            assertInstanceOf(StandardTestDispatcher::class.java, extension.testDispatcher)
            assertSame(extension.testDispatcher, Dispatchers.Main)
        } finally {
            extension.afterEach(null)
        }
    }

    @Test
    fun `beforeEach provides a fresh dispatcher for every invocation`() {
        extension.beforeEach(null)
        val firstDispatcher = extension.testDispatcher
        extension.afterEach(null)

        extension.beforeEach(null)
        val secondDispatcher = extension.testDispatcher

        try {
            assertInstanceOf(StandardTestDispatcher::class.java, firstDispatcher)
            assertInstanceOf(StandardTestDispatcher::class.java, secondDispatcher)
            assertNotSame(firstDispatcher, secondDispatcher)
            assertSame(secondDispatcher, Dispatchers.Main)
        } finally {
            extension.afterEach(null)
        }
    }
}
