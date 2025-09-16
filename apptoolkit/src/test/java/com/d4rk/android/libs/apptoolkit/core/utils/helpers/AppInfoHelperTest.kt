package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.core.di.TestDispatchers
import io.mockk.any
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val PACKAGE_NAME = "com.example.app"

@OptIn(ExperimentalCoroutinesApi::class)
class AppInfoHelperTest {

    @Test
    fun `isAppInstalled returns true when package exists`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val applicationInfo = mockk<ApplicationInfo>()
        every { context.packageManager } returns packageManager
        every { packageManager.getApplicationInfo(PACKAGE_NAME, 0) } returns applicationInfo

        val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

        assertTrue(helper.isAppInstalled(context, PACKAGE_NAME))
        verify { packageManager.getApplicationInfo(PACKAGE_NAME, 0) }
    }

    @Test
    fun `isAppInstalled returns false when package is missing`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.getApplicationInfo(PACKAGE_NAME, 0) } throws PackageManager.NameNotFoundException()

        val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

        assertFalse(helper.isAppInstalled(context, PACKAGE_NAME))
        verify { packageManager.getApplicationInfo(PACKAGE_NAME, 0) }
    }

    @Test
    fun `openApp returns true when launch succeeds`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val launchIntent = mockk<Intent>(relaxed = true)
        val component = mockk<ComponentName>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns launchIntent
        every { launchIntent.resolveActivity(packageManager) } returns component
        justRun { context.startActivity(launchIntent) }

        val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

        assertTrue(helper.openApp(context, PACKAGE_NAME))
        verify { launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        verify { context.startActivity(launchIntent) }
    }

    @Test
    fun `openApp returns false when launch intent is missing`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns null
        every { context.getString(R.string.app_not_installed) } returns "App not installed"

        mockkStatic(Toast::class)
        try {
            val toast = mockk<Toast>(relaxed = true)
            every { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) } returns toast

            val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

            assertFalse(helper.openApp(context, PACKAGE_NAME))
            verify { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) }
            verify { toast.show() }
            verify(exactly = 0) { context.startActivity(any()) }
        } finally {
            unmockkStatic(Toast::class)
        }
    }

    @Test
    fun `openApp returns false when startActivity throws`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val launchIntent = mockk<Intent>(relaxed = true)
        val component = mockk<ComponentName>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns launchIntent
        every { launchIntent.resolveActivity(packageManager) } returns component
        every { context.getString(R.string.app_not_installed) } returns "App not installed"

        mockkStatic(Toast::class)
        try {
            val toast = mockk<Toast>(relaxed = true)
            every { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) } returns toast
            every { context.startActivity(launchIntent) } throws RuntimeException("boom")

            val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

            assertFalse(helper.openApp(context, PACKAGE_NAME))
            verify { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) }
            verify { toast.show() }
        } finally {
            unmockkStatic(Toast::class)
        }
    }

    @Test
    fun `openAppResult returns success when launch succeeds`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val launchIntent = mockk<Intent>(relaxed = true)
        val component = mockk<ComponentName>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns launchIntent
        every { launchIntent.resolveActivity(packageManager) } returns component
        justRun { context.startActivity(launchIntent) }

        val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

        val result = helper.openAppResult(context, PACKAGE_NAME)

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull())
        verify { context.startActivity(launchIntent) }
    }

    @Test
    fun `openAppResult returns failure when launch intent is missing`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns null
        every { context.getString(R.string.app_not_installed) } returns "App not installed"

        mockkStatic(Toast::class)
        try {
            val toast = mockk<Toast>(relaxed = true)
            every { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) } returns toast

            val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

            val result = helper.openAppResult(context, PACKAGE_NAME)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            verify { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) }
            verify { toast.show() }
            verify(exactly = 0) { context.startActivity(any()) }
        } finally {
            unmockkStatic(Toast::class)
        }
    }

    @Test
    fun `openAppResult returns failure when startActivity throws`() = runTest {
        val context = mockk<Context>()
        val packageManager = mockk<PackageManager>()
        val launchIntent = mockk<Intent>(relaxed = true)
        val component = mockk<ComponentName>()
        every { context.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(PACKAGE_NAME) } returns launchIntent
        every { launchIntent.resolveActivity(packageManager) } returns component
        every { context.getString(R.string.app_not_installed) } returns "App not installed"

        mockkStatic(Toast::class)
        try {
            val toast = mockk<Toast>(relaxed = true)
            every { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) } returns toast
            every { context.startActivity(launchIntent) } throws RuntimeException("boom")

            val helper = AppInfoHelper(TestDispatchers(UnconfinedTestDispatcher(testScheduler)))

            val result = helper.openAppResult(context, PACKAGE_NAME)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is RuntimeException)
            verify { Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT) }
            verify { toast.show() }
        } finally {
            unmockkStatic(Toast::class)
        }
    }
}
