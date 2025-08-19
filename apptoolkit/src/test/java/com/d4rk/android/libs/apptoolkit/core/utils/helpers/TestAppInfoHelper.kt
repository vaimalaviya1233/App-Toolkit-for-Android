package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestAppInfoHelper {

    @Test
    fun `openApp adds new task flag when context not Activity`() = runBlocking {
        println("🚀 [TEST] openApp adds new task flag when context not Activity")
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        val intent = mockk<Intent>(relaxed = true)
        every { context.packageManager } returns pm
        every { pm.getLaunchIntentForPackage("pkg") } returns intent
        justRun { context.startActivity(intent) }

        AppInfoHelper().openApp(context, "pkg")

        verify { intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        println("🏁 [TEST DONE] openApp adds new task flag when context not Activity")
    }

    @Test
    fun `openApp returns false on start failure`() = runBlocking {
        println("🚀 [TEST] openApp returns false on start failure")
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        val intent = mockk<Intent>()
        every { context.packageManager } returns pm
        every { pm.getLaunchIntentForPackage("pkg") } returns intent
        every { context.getString(any()) } returns "not installed"
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "not installed", Toast.LENGTH_SHORT) } returns toast
        every { context.startActivity(intent) } throws RuntimeException("fail")

        val result = AppInfoHelper().openApp(context, "pkg")
        assertEquals(false, result)
        verify { Toast.makeText(context, "not installed", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] openApp returns false on start failure")
    }

    @Test
    fun `openAppResult exposes failure`() = runBlocking {
        println("🚀 [TEST] openAppResult exposes failure")
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        val intent = mockk<Intent>()
        every { context.packageManager } returns pm
        every { pm.getLaunchIntentForPackage("pkg") } returns intent
        every { context.getString(any()) } returns "not installed"
        mockkStatic(Toast::class)
        val toast = mockk<Toast>(relaxed = true)
        every { Toast.makeText(context, "not installed", Toast.LENGTH_SHORT) } returns toast
        every { context.startActivity(intent) } throws RuntimeException("fail")

        val result = AppInfoHelper().openAppResult(context, "pkg")
        assertTrue(result.isFailure)
        verify { Toast.makeText(context, "not installed", Toast.LENGTH_SHORT) }
        println("🏁 [TEST DONE] openAppResult exposes failure")
    }
}
