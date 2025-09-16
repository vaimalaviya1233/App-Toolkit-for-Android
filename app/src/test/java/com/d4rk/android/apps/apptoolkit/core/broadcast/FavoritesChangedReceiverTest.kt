package com.d4rk.android.apps.apptoolkit.core.broadcast

import android.content.Context
import android.content.Intent
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

class FavoritesChangedReceiverTest {

    private val context = mockk<Context>(relaxed = true)
    private val receiver = FavoritesChangedReceiver()

    @Test
    fun `onReceive reads package name extra when present`() {
        val packageName = "com.example.app"
        val intent = mockk<Intent>()

        every { intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME) } returns packageName

        mockkStatic(Log::class)
        try {
            every { Log.d(any(), any()) } returns 0

            receiver.onReceive(context, intent)

            verify(exactly = 1) { intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME) }
            verify { Log.d(any(), match { packageName in it }) }
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `onReceive completes without exception when package name extra missing`() {
        val intent = mockk<Intent>()

        every { intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME) } returns null

        mockkStatic(Log::class)
        try {
            every { Log.d(any(), any()) } returns 0
            every { Log.w(any(), any()) } returns 0

            assertDoesNotThrow {
                receiver.onReceive(context, intent)
            }

            verify(exactly = 1) { intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME) }
            verify { Log.w(any(), match { "missing package name" in it }) }
        } finally {
            unmockkStatic(Log::class)
        }
    }

    @Test
    fun `createIntentWithPackage includes package name extra`() {
        val context = mockk<Context> {
            every { packageName } returns "com.example"
        }

        val packageName = "com.example.app"

        val intent = FavoritesChangedReceiver.createIntentWithPackage(context, packageName)

        assertEquals(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED, intent.action)
        assertEquals(packageName, intent.getStringExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME))
    }

    @Test
    fun `createIntentWithoutPackage omits package name extra`() {
        val context = mockk<Context> {
            every { packageName } returns "com.example"
        }

        val intent = FavoritesChangedReceiver.createIntentWithoutPackage(context)

        assertEquals(FavoritesChangedReceiver.ACTION_FAVORITES_CHANGED, intent.action)
        assertFalse(intent.hasExtra(FavoritesChangedReceiver.EXTRA_PACKAGE_NAME))
    }
}
