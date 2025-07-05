package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TestIntentsHelper {

    @Test
    fun `openUrl starts ACTION_VIEW intent`() {
        val context = mockk<Context>()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        IntentsHelper.openUrl(context, "https://example.com")

        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://example.com", intent.data.toString())
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `openActivity starts activity with new task flag`() {
        val context = mockk<Context>()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        IntentsHelper.openActivity(context, String::class.java)

        val intent = intentSlot.captured
        assertEquals(String::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `openUrl propagates exception`() {
        val context = mockk<Context>()
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.openUrl(context, "https://example.com")
        }
    }

    @Test
    fun `openActivity propagates exception`() {
        val context = mockk<Context>()
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.openActivity(context, String::class.java)
        }
    }
}
