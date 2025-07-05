package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestClipboardHelper {
    @Test
    fun `copyTextToClipboard sets primary clip and executes callback when appropriate`() {
        val manager = mockk<ClipboardManager>()
        val context = mockk<Context>()
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns manager
        val clipSlot = slot<ClipData>()
        justRun { manager.setPrimaryClip(capture(clipSlot)) }

        var callbackExecuted = false
        ClipboardHelper.copyTextToClipboard(context, "label", "text") { callbackExecuted = true }

        verify { manager.setPrimaryClip(any()) }
        assertEquals("text", clipSlot.captured.getItemAt(0).text)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            assertTrue(callbackExecuted)
        } else {
            assertFalse(callbackExecuted)
        }
    }

    @Test
    fun `copyTextToClipboard throws when manager missing`() {
        val context = mockk<Context>()
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns null

        assertFailsWith<NullPointerException> {
            ClipboardHelper.copyTextToClipboard(context, "l", "t")
        }
    }

    @Test
    fun `copyTextToClipboard handles manager exception`() {
        val manager = mockk<ClipboardManager>()
        val context = mockk<Context>()
        every { context.getSystemService(Context.CLIPBOARD_SERVICE) } returns manager
        every { manager.setPrimaryClip(any()) } throws RuntimeException("boom")

        assertFailsWith<RuntimeException> {
            ClipboardHelper.copyTextToClipboard(context, "l", "t")
        }
    }
}
