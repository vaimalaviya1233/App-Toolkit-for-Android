package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.Intent
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
