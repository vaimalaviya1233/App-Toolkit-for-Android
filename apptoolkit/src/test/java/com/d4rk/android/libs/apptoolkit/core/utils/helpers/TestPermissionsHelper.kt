package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.d4rk.android.libs.apptoolkit.core.utils.constants.permissions.PermissionsConstants
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class TestPermissionsHelper {

    @Test
    fun `hasNotificationPermission parses permission state`() {
        println("🚀 [TEST] hasNotificationPermission parses permission state")
        val context = mockk<Context>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ContextCompat::class)
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_GRANTED
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returns PackageManager.PERMISSION_DENIED
            assertFalse(PermissionsHelper.hasNotificationPermission(context))
        } else {
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
        }
        println("🏁 [TEST DONE] hasNotificationPermission parses permission state")
    }

    @Test
    fun `requestNotificationPermission delegates to ActivityCompat when needed`() {
        println("🚀 [TEST] requestNotificationPermission delegates to ActivityCompat when needed")
        val activity = mockk<Activity>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ActivityCompat::class)
            justRun { ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PermissionsConstants.REQUEST_CODE_NOTIFICATION_PERMISSION) }
            PermissionsHelper.requestNotificationPermission(activity)
            verify { ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PermissionsConstants.REQUEST_CODE_NOTIFICATION_PERMISSION) }
        } else {
            mockkStatic(ActivityCompat::class)
            PermissionsHelper.requestNotificationPermission(activity)
            verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
        }
        println("🏁 [TEST DONE] requestNotificationPermission delegates to ActivityCompat when needed")
    }

    @Test
    fun `hasNotificationPermission handles unexpected value`() {
        println("🚀 [TEST] hasNotificationPermission handles unexpected value")
        val context = mockk<Context>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ContextCompat::class)
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returns 123
            assertFalse(PermissionsHelper.hasNotificationPermission(context))
        } else {
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
        }
        println("🏁 [TEST DONE] hasNotificationPermission handles unexpected value")
    }

    @Test
    fun `hasNotificationPermission propagates exception`() {
        println("🚀 [TEST] hasNotificationPermission propagates exception")
        val context = mockk<Context>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ContextCompat::class)
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } throws RuntimeException("fail")
            assertFailsWith<RuntimeException> { PermissionsHelper.hasNotificationPermission(context) }
        } else {
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
        }
        println("🏁 [TEST DONE] hasNotificationPermission propagates exception")
    }

    @Test
    fun `requestNotificationPermission propagates exception`() {
        println("🚀 [TEST] requestNotificationPermission propagates exception")
        val activity = mockk<Activity>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ActivityCompat::class)
            every { ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PermissionsConstants.REQUEST_CODE_NOTIFICATION_PERMISSION) } throws RuntimeException("boom")
            assertFailsWith<RuntimeException> { PermissionsHelper.requestNotificationPermission(activity) }
        } else {
            mockkStatic(ActivityCompat::class)
            PermissionsHelper.requestNotificationPermission(activity)
            verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
        }
        println("🏁 [TEST DONE] requestNotificationPermission propagates exception")
    }

    @Test
    fun `hasNotificationPermission handles other unknown values`() {
        println("🚀 [TEST] hasNotificationPermission handles other unknown values")
        val context = mockk<Context>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ContextCompat::class)
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returnsMany listOf(1, -2)
            assertFalse(PermissionsHelper.hasNotificationPermission(context))
            assertFalse(PermissionsHelper.hasNotificationPermission(context))
        } else {
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
        }
        println("🏁 [TEST DONE] hasNotificationPermission handles other unknown values")
    }

    @Test
    fun `hasNotificationPermission reflects runtime revocation`() {
        println("🚀 [TEST] hasNotificationPermission reflects runtime revocation")
        val context = mockk<Context>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mockkStatic(ContextCompat::class)
            every { ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) } returnsMany listOf(
                PackageManager.PERMISSION_GRANTED,
                PackageManager.PERMISSION_DENIED
            )
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
            assertFalse(PermissionsHelper.hasNotificationPermission(context))
        } else {
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
            assertTrue(PermissionsHelper.hasNotificationPermission(context))
        }
        println("🏁 [TEST DONE] hasNotificationPermission reflects runtime revocation")
    }
}
