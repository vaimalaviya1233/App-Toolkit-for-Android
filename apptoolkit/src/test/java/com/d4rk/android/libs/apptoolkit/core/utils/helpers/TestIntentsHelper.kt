package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import com.d4rk.android.libs.apptoolkit.test.R
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
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

    @Test
    fun `openAppNotificationSettings builds correct intent`() {
        val context = mockk<Context>()
        every { context.packageName } returns "pkg"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.openAppNotificationSettings(context)

        val intent = slot.captured
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
            assertEquals("pkg", intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        } else {
            assertEquals("android.settings.APPLICATION_DETAILS_SETTINGS", intent.action)
            assertEquals(Uri.fromParts("package", "pkg", null), intent.data)
        }
    }

    @Test
    fun `openPlayStoreForApp uses market when resolvable`() {
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().resolveActivity(pm) } returns mockk()

        IntentsHelper.openPlayStoreForApp(context, "com.test")

        val intent = slot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("${AppLinks.MARKET_APP_PAGE}com.test", intent.data.toString())
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `openPlayStoreForApp falls back to web when market missing`() {
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().resolveActivity(pm) } returns null

        IntentsHelper.openPlayStoreForApp(context, "com.test")

        val intent = slot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("${AppLinks.PLAY_STORE_APP}com.test", intent.data.toString())
    }

    @Test
    fun `shareApp builds chooser intent`() {
        val context = mockk<Context>()
        val res = mockk<Resources>()
        every { context.resources } returns res
        every { res.getText(R.string.send_email_using) } returns "send"
        every { context.getString(R.string.summary_share_message, any()) } returns "msg"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.shareApp(context, R.string.summary_share_message)

        val chooser = slot.captured
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val sendIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            chooser.getParcelableExtra(Intent.EXTRA_INTENT)
        }
        assertEquals(Intent.ACTION_SEND, sendIntent?.action)
        assertEquals("msg", sendIntent?.getStringExtra(Intent.EXTRA_TEXT))
        assertEquals("text/plain", sendIntent?.type)
    }

    @Test
    fun `sendEmailToDeveloper builds mailto chooser`() {
        val context = mockk<Context>()
        every { context.getString(R.string.feedback_for, "App") } returns "subject"
        every { context.getString(R.string.dear_developer) } returns "body"
        every { context.getString(R.string.send_email_using) } returns "send"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)

        val chooser = slot.captured
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val inner = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            chooser.getParcelableExtra(Intent.EXTRA_INTENT)
        }
        assertEquals(Intent.ACTION_SENDTO, inner?.action)
        assertTrue(inner?.data.toString().startsWith("mailto:"))
    }

    @Test
    fun `openAppNotificationSettings propagates exception`() {
        val context = mockk<Context>()
        every { context.packageName } returns "pkg"
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.openAppNotificationSettings(context)
        }
    }

    @Test
    fun `openPlayStoreForApp propagates exception`() {
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().resolveActivity(pm) } returns mockk()
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.openPlayStoreForApp(context, "com.test")
        }
    }

    @Test
    fun `shareApp propagates exception`() {
        val context = mockk<Context>()
        val res = mockk<Resources>()
        every { context.resources } returns res
        every { res.getText(R.string.send_email_using) } returns "send"
        every { context.getString(R.string.summary_share_message, any()) } returns "msg"
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.shareApp(context, R.string.summary_share_message)
        }
    }

    @Test
    fun `sendEmailToDeveloper propagates exception`() {
        val context = mockk<Context>()
        every { context.getString(R.string.feedback_for, "App") } returns "subject"
        every { context.getString(R.string.dear_developer) } returns "body"
        every { context.getString(R.string.send_email_using) } returns "send"
        every { context.startActivity(any()) } throws RuntimeException("fail")

        assertFailsWith<RuntimeException> {
            IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)
        }
    }

    @Test
    fun `openPlayStoreForApp with empty package name`() {
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().resolveActivity(pm) } returns mockk()

        IntentsHelper.openPlayStoreForApp(context, "")

        val intent = slot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(AppLinks.MARKET_APP_PAGE, intent.data.toString())
    }

    @Test
    fun `openPlayStoreForApp null package throws`() {
        val context = mockk<Context>()
        val method = IntentsHelper::class.java.getDeclaredMethod(
            "openPlayStoreForApp",
            Context::class.java,
            String::class.java
        )

        assertFailsWith<NullPointerException> {
            method.invoke(IntentsHelper, context, null)
        }
    }

    @Test
    fun `openUrl handles malformed url`() {
        val context = mockk<Context>()
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.openUrl(context, "htp::://bad url")

        val intent = slot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("htp::://bad url", intent.data.toString())
    }

    @Test
    fun `openPlayStoreForApp handles unusual package name`() {
        val context = mockk<Context>()
        val pm = mockk<PackageManager>()
        every { context.packageManager } returns pm
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().resolveActivity(pm) } returns mockk()

        val pkg = "com.example.app-1_2"
        IntentsHelper.openPlayStoreForApp(context, pkg)

        val intent = slot.captured
        assertEquals("${AppLinks.MARKET_APP_PAGE}$pkg", intent.data.toString())
    }

    @Test
    fun `shareApp uses provided chooser title`() {
        val context = mockk<Context>()
        val res = mockk<Resources>()
        every { context.resources } returns res
        every { res.getText(R.string.send_email_using) } returns "Share via \u2728"
        every { context.getString(R.string.summary_share_message, any()) } returns "msg"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.shareApp(context, R.string.summary_share_message)

        val chooser = slot.captured
        assertEquals("Share via \u2728", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
    }

    @Test
    fun `sendEmailToDeveloper uses provided chooser title`() {
        val context = mockk<Context>()
        every { context.getString(R.string.feedback_for, "App") } returns "subject"
        every { context.getString(R.string.dear_developer) } returns "body"
        every { context.getString(R.string.send_email_using) } returns "Email via \uD83D\uDE80"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)

        val chooser = slot.captured
        assertEquals("Email via \uD83D\uDE80", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
    }

    @Test
    fun `openAppNotificationSettings uses legacy intent pre O`() {
        val context = mockk<Context>()
        every { context.packageName } returns "pkg"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.N

        IntentsHelper.openAppNotificationSettings(context)

        val intent = slot.captured
        assertEquals("android.settings.APPLICATION_DETAILS_SETTINGS", intent.action)
        assertEquals(Uri.fromParts("package", "pkg", null), intent.data)
    }
}
