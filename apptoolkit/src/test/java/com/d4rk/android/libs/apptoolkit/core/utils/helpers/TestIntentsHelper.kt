package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.test.mock.MockContext
import android.test.mock.MockPackageManager
import com.d4rk.android.libs.apptoolkit.core.utils.constants.links.AppLinks
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class TestIntentsHelper {

    @Test
    fun `openUrl launches view intent when a handler exists`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent ->
                intent.action == Intent.ACTION_VIEW && intent.data == Uri.parse("https://example.com")
            }
        }
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openUrl(context, "https://example.com")

        assertTrue(launched)
        val started = context.startedIntents.single()
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals("https://example.com", started.dataString)
        assertTrue(started.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openUrl returns false when startActivity throws`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent ->
                intent.action == Intent.ACTION_VIEW && intent.data == Uri.parse("https://example.com")
            }
        }
        val context = RecordingContext(pm).apply { throwOnStart = true }

        val launched = IntentsHelper.openUrl(context, "https://example.com")

        assertFalse(launched)
        assertEquals(1, context.startedIntents.size)
        assertTrue(context.startedIntents.single().hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openUrl returns false when no matching handler exists`() {
        val pm = RecordingPackageManager()
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openUrl(context, "https://example.com")

        assertFalse(launched)
        assertTrue(context.startedIntents.isEmpty())
    }

    @Test
    fun `openActivity uses explicit component and adds new task flag`() {
        val pm = RecordingPackageManager()
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openActivity(context, SampleActivity::class.java)

        assertTrue(launched)
        val intent = context.startedIntents.single()
        assertEquals(ComponentName(context.packageName, SampleActivity::class.java.name), intent.component)
        assertTrue(intent.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openActivity returns false when activity launch fails`() {
        val pm = RecordingPackageManager()
        val context = RecordingContext(pm).apply { throwOnStart = true }

        val launched = IntentsHelper.openActivity(context, SampleActivity::class.java)

        assertFalse(launched)
        assertEquals(1, context.startedIntents.size)
        assertTrue(context.startedIntents.single().hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openPlayStoreForApp prefers market intent when available`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent ->
                intent.action == Intent.ACTION_VIEW && intent.dataString == "${AppLinks.MARKET_APP_PAGE}com.example.app"
            }
        }
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertTrue(launched)
        val intent = context.startedIntents.single()
        assertEquals("${AppLinks.MARKET_APP_PAGE}com.example.app", intent.dataString)
        assertTrue(intent.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openPlayStoreForApp falls back to web url when market unavailable`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent ->
                intent.action == Intent.ACTION_VIEW && intent.dataString == "${AppLinks.PLAY_STORE_APP}com.example.app"
            }
        }
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertTrue(launched)
        val intent = context.startedIntents.single()
        assertEquals("${AppLinks.PLAY_STORE_APP}com.example.app", intent.dataString)
        assertTrue(intent.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    fun `openPlayStoreForApp returns false when no handler exists`() {
        val pm = RecordingPackageManager()
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openPlayStoreForApp(context, "com.example.app")

        assertFalse(launched)
        assertTrue(context.startedIntents.isEmpty())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `openAppNotificationSettings uses app notification settings intent on O+`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent -> intent.action == Settings.ACTION_APP_NOTIFICATION_SETTINGS }
        }
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openAppNotificationSettings(context)

        assertTrue(launched)
        val intent = context.startedIntents.single()
        assertEquals(Settings.ACTION_APP_NOTIFICATION_SETTINGS, intent.action)
        assertEquals(context.packageName, intent.getStringExtra(Settings.EXTRA_APP_PACKAGE))
        assertTrue(intent.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun `openAppNotificationSettings falls back to details settings before O`() {
        val pm = RecordingPackageManager().apply {
            addResolver { intent -> intent.action == "android.settings.APPLICATION_DETAILS_SETTINGS" }
        }
        val context = RecordingContext(pm)

        val launched = IntentsHelper.openAppNotificationSettings(context)

        assertTrue(launched)
        val intent = context.startedIntents.single()
        assertEquals("android.settings.APPLICATION_DETAILS_SETTINGS", intent.action)
        assertEquals(Uri.fromParts("package", context.packageName, null), intent.data)
        assertTrue(intent.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private class RecordingContext(
        private val packageManager: PackageManager,
        private val packageNameValue: String = "com.example.test"
    ) : MockContext() {

        val startedIntents = mutableListOf<Intent>()
        var throwOnStart: Boolean = false

        override fun getPackageManager(): PackageManager = packageManager

        override fun getPackageName(): String = packageNameValue

        override fun getApplicationContext(): Context = this

        override fun startActivity(intent: Intent?) {
            if (intent != null) {
                startedIntents += Intent(intent)
                if (throwOnStart) {
                    throw RuntimeException("startActivity failed")
                }
            }
        }

        override fun startActivity(intent: Intent?, options: Bundle?) {
            startActivity(intent)
        }
    }

    private class RecordingPackageManager : MockPackageManager() {

        private val resolvers = mutableListOf<(Intent) -> Boolean>()

        fun addResolver(predicate: (Intent) -> Boolean) {
            resolvers += predicate
        }

        override fun resolveActivity(intent: Intent?, flags: Int): ResolveInfo? {
            if (intent == null) return null
            val matches = resolvers.any { it(intent) }
            return if (matches) createResolveInfo() else null
        }

        override fun queryIntentActivities(intent: Intent?, flags: Int): MutableList<ResolveInfo> {
            val info = resolveActivity(intent, flags) ?: return mutableListOf()
            return mutableListOf(info)
        }

        private fun createResolveInfo(): ResolveInfo = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                applicationInfo = ApplicationInfo().apply {
                    packageName = "handler.package"
                }
                packageName = "handler.package"
                name = "HandlerActivity"
            }
        }
    }

    private fun Intent.hasFlag(flag: Int): Boolean = flags and flag != 0

    private class SampleActivity : Activity()
}
