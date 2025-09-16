package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.firstArg
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.match
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.unmockkStatic
import io.mockk.verify
import java.net.URLEncoder
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IntentsHelperTest {

    @AfterTest
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `openUrl returns true when handler exists`() {
        val (context, packageManager) = contextWithPackageManager()
        val intentSlot = slot<Intent>()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        justRun { context.startActivity(capture(intentSlot)) }

        val result = IntentsHelper.openUrl(context, "https://example.com")

        assertTrue(result)
        val intent = intentSlot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https://example.com", intent.dataString)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openUrl returns false when no handler exists`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns null

        val result = IntentsHelper.openUrl(context, "https://example.com")

        assertFalse(result)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `openUrl returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.openUrl(context, "https://example.com")

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openActivity returns true when activity is launched`() {
        val (context, packageManager) = contextWithPackageManager()
        val intentSlot = slot<Intent>()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        justRun { context.startActivity(capture(intentSlot)) }

        val result = IntentsHelper.openActivity(context, DummyActivity::class.java)

        assertTrue(result)
        val intent = intentSlot.captured
        assertEquals(DummyActivity::class.java.name, intent.component?.className)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openActivity returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.openActivity(context, DummyActivity::class.java)

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openAppNotificationSettings uses modern intent on O and above`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.O

        try {
            val result = IntentsHelper.openAppNotificationSettings(context)

            assertTrue(result)
            val intent = intentSlot.captured
            assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
            assertEquals("com.example.app", intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        } finally {
            unmockkStatic(Build.VERSION::class)
        }
    }

    @Test
    fun `openAppNotificationSettings uses legacy intent below O`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.N

        try {
            val result = IntentsHelper.openAppNotificationSettings(context)

            assertTrue(result)
            val intent = intentSlot.captured
            assertEquals("android.settings.APPLICATION_DETAILS_SETTINGS", intent.action)
            assertEquals(Uri.fromParts("package", "com.example.app", null), intent.data)
            assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        } finally {
            unmockkStatic(Build.VERSION::class)
        }
    }

    @Test
    fun `openAppNotificationSettings returns false when no handler`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        every { packageManager.resolveActivity(any(), any()) } returns null

        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.O

        try {
            val result = IntentsHelper.openAppNotificationSettings(context)

            assertFalse(result)
            verify(exactly = 0) { context.startActivity(any()) }
        } finally {
            unmockkStatic(Build.VERSION::class)
        }
    }

    @Test
    fun `openAppNotificationSettings returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.O

        try {
            val result = IntentsHelper.openAppNotificationSettings(context)

            assertFalse(result)
            verify(exactly = 1) { context.startActivity(any()) }
        } finally {
            unmockkStatic(Build.VERSION::class)
        }
    }

    @Test
    fun `openDisplaySettings launches display settings when available`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        val result = IntentsHelper.openDisplaySettings(context)

        assertTrue(result)
        val intent = intentSlot.captured
        assertEquals(Settings.ACTION_DISPLAY_SETTINGS, intent.action)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `openDisplaySettings falls back to general settings when display missing`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(matchIntentAction(Settings.ACTION_DISPLAY_SETTINGS), any()) } returns null
        every { packageManager.resolveActivity(matchIntentAction(Settings.ACTION_SETTINGS), any()) } returns resolvableActivity()
        val intentSlot = slot<Intent>()
        justRun { context.startActivity(capture(intentSlot)) }

        val result = IntentsHelper.openDisplaySettings(context)

        assertTrue(result)
        val intent = intentSlot.captured
        assertEquals(Settings.ACTION_SETTINGS, intent.action)
    }

    @Test
    fun `openDisplaySettings returns false when no settings handlers`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns null

        val result = IntentsHelper.openDisplaySettings(context)

        assertFalse(result)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `openDisplaySettings returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(matchIntentAction(Settings.ACTION_DISPLAY_SETTINGS), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.openDisplaySettings(context)

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openPlayStoreForApp uses market intent when available`() {
        val (context, packageManager) = contextWithPackageManager()
        val slot = slot<Intent>()
        every { packageManager.resolveActivity(any(), any()) } answers {
            val intent = firstArg<Intent>()
            if (intent.dataString?.startsWith(AppLinks.MARKET_APP_PAGE) == true) {
                resolvableActivity()
            } else {
                null
            }
        }
        justRun { context.startActivity(capture(slot)) }

        val result = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertTrue(result)
        val intent = slot.captured
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("${AppLinks.MARKET_APP_PAGE}com.example.app", intent.dataString)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `openPlayStoreForApp falls back to web url`() {
        val (context, packageManager) = contextWithPackageManager()
        val slot = slot<Intent>()
        every { packageManager.resolveActivity(any(), any()) } answers {
            val intent = firstArg<Intent>()
            when {
                intent.dataString?.startsWith(AppLinks.MARKET_APP_PAGE) == true -> null
                intent.dataString?.startsWith(AppLinks.PLAY_STORE_APP) == true -> resolvableActivity()
                else -> null
            }
        }
        justRun { context.startActivity(capture(slot)) }

        val result = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertTrue(result)
        val intent = slot.captured
        assertEquals("${AppLinks.PLAY_STORE_APP}com.example.app", intent.dataString)
        assertEquals(Intent.ACTION_VIEW, intent.action)
    }

    @Test
    fun `openPlayStoreForApp returns false when market handler throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `openPlayStoreForApp returns false when neither handler exists`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns null

        val result = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertFalse(result)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `shareApp composes chooser with formatted message`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        val resources = mockk<android.content.res.Resources>()
        every { context.resources } returns resources
        every { resources.getText(R.string.send_email_using) } returns "Share via"
        every {
            context.getString(
                R.string.summary_share_message,
                "${AppLinks.PLAY_STORE_APP}com.example.app"
            )
        } returns "Share this app: ${AppLinks.PLAY_STORE_APP}com.example.app"
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        val result = IntentsHelper.shareApp(context, R.string.summary_share_message)

        assertTrue(result)
        val chooser = slot.captured
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Share via", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
        val inner = chooser.extractInnerIntent()
        assertNotNull(inner)
        assertEquals(Intent.ACTION_SEND, inner.action)
        assertEquals("text/plain", inner.type)
        assertEquals("Share this app: ${AppLinks.PLAY_STORE_APP}com.example.app", inner.getStringExtra(Intent.EXTRA_TEXT))
        assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `shareApp returns false when no chooser handler`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        val resources = mockk<android.content.res.Resources>()
        every { context.resources } returns resources
        every { resources.getText(R.string.send_email_using) } returns "Share via"
        every {
            context.getString(
                R.string.summary_share_message,
                "${AppLinks.PLAY_STORE_APP}com.example.app"
            )
        } returns "Share this app"
        every { packageManager.resolveActivity(any(), any()) } returns null

        val result = IntentsHelper.shareApp(context, R.string.summary_share_message)

        assertFalse(result)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `shareApp returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { context.packageName } returns "com.example.app"
        val resources = mockk<android.content.res.Resources>()
        every { context.resources } returns resources
        every { resources.getText(R.string.send_email_using) } returns "Share via"
        every {
            context.getString(
                R.string.summary_share_message,
                "${AppLinks.PLAY_STORE_APP}com.example.app"
            )
        } returns "Share this app"
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.shareApp(context, R.string.summary_share_message)

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    @Test
    fun `sendEmailToDeveloper composes encoded mailto`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.getString(R.string.app_name) } returns "App Toolkit"
        every { context.getString(R.string.feedback_for, "App Toolkit") } returns "Feedback for App Toolkit"
        every { context.getString(R.string.dear_developer) } returns "Hello developer"
        every { context.getString(R.string.send_email_using) } returns "Send email"
        val slot = slot<Intent>()
        justRun { context.startActivity(capture(slot)) }

        val result = IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)

        assertTrue(result)
        val chooser = slot.captured
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals("Send email", chooser.getCharSequenceExtra(Intent.EXTRA_TITLE))
        val inner = chooser.extractInnerIntent()
        assertNotNull(inner)
        assertEquals(Intent.ACTION_SENDTO, inner.action)
        val expectedSubject = URLEncoder.encode("Feedback for App Toolkit", "UTF-8").replace("+", "%20")
        val expectedBody = URLEncoder.encode("Hello developer\n\n", "UTF-8").replace("+", "%20")
        val expectedUri = "mailto:${AppLinks.CONTACT_EMAIL}?subject=$expectedSubject&body=$expectedBody"
        assertEquals(expectedUri, inner.data.toString())
        assertTrue(chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
    }

    @Test
    fun `sendEmailToDeveloper returns false when no handler`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns null
        every { context.getString(R.string.app_name) } returns "App Toolkit"
        every { context.getString(R.string.feedback_for, "App Toolkit") } returns "Feedback for App Toolkit"
        every { context.getString(R.string.dear_developer) } returns "Hello developer"
        every { context.getString(R.string.send_email_using) } returns "Send email"

        val result = IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)

        assertFalse(result)
        verify(exactly = 0) { context.startActivity(any()) }
    }

    @Test
    fun `sendEmailToDeveloper returns false when startActivity throws`() {
        val (context, packageManager) = contextWithPackageManager()
        every { packageManager.resolveActivity(any(), any()) } returns resolvableActivity()
        every { context.getString(R.string.app_name) } returns "App Toolkit"
        every { context.getString(R.string.feedback_for, "App Toolkit") } returns "Feedback for App Toolkit"
        every { context.getString(R.string.dear_developer) } returns "Hello developer"
        every { context.getString(R.string.send_email_using) } returns "Send email"
        every { context.startActivity(any()) } throws IllegalStateException("boom")

        val result = IntentsHelper.sendEmailToDeveloper(context, R.string.app_name)

        assertFalse(result)
        verify(exactly = 1) { context.startActivity(any()) }
    }

    private fun contextWithPackageManager(): Pair<Context, PackageManager> {
        val context = mockk<Context>(relaxed = true)
        val packageManager = mockk<PackageManager>(relaxed = true)
        every { context.packageManager } returns packageManager
        return context to packageManager
    }

    private fun resolvableActivity(): ResolveInfo {
        return ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                applicationInfo = ApplicationInfo().apply {
                    packageName = "resolved.package"
                }
                packageName = "resolved.package"
                name = "ResolvedActivity"
            }
        }
    }

    private fun matchIntentAction(action: String) = match<Intent> { it.action == action }

    private fun Intent.extractInnerIntent(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_INTENT)
        }
    }

    private class DummyActivity : Activity()
}
