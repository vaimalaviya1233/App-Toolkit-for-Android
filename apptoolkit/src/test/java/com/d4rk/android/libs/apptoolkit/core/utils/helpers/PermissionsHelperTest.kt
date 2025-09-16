package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.d4rk.android.libs.apptoolkit.core.utils.constants.permissions.PermissionsConstants
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.robolectric.util.ReflectionHelpers

class PermissionsHelperTest {

    private var originalSdkInt: Int = Build.VERSION.SDK_INT

    @BeforeEach
    fun setUp() {
        originalSdkInt = Build.VERSION.SDK_INT
    }

    @AfterEach
    fun tearDown() {
        setSdkInt(originalSdkInt)
        unmockkAll()
    }

    @Test
    fun `hasNotificationPermission returns true when granted on API 33`() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        val context = mockk<Context>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } returns PackageManager.PERMISSION_GRANTED

        val granted = PermissionsHelper.hasNotificationPermission(context)

        assertTrue(granted)
        verify(exactly = 1) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @Test
    fun `hasNotificationPermission returns false when denied on API 33`() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        val context = mockk<Context>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } returns PackageManager.PERMISSION_DENIED

        val granted = PermissionsHelper.hasNotificationPermission(context)

        assertFalse(granted)
        verify(exactly = 1) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @Test
    fun `hasNotificationPermission always true below API 33`() {
        setSdkInt(Build.VERSION_CODES.S_V2)
        val context = mockk<Context>()
        mockkStatic(ContextCompat::class)

        val granted = PermissionsHelper.hasNotificationPermission(context)

        assertTrue(granted)
        verify(exactly = 0) { ContextCompat.checkSelfPermission(any(), any()) }
    }

    @Test
    fun `requestNotificationPermission requests permission when missing on API 33`() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } returns PackageManager.PERMISSION_DENIED
        mockkStatic(ActivityCompat::class)
        every {
            ActivityCompat.requestPermissions(any(), any(), any())
        } just Runs

        PermissionsHelper.requestNotificationPermission(activity)

        verify(exactly = 1) {
            ActivityCompat.requestPermissions(
                activity,
                match { it.size == 1 && it[0] == Manifest.permission.POST_NOTIFICATIONS },
                PermissionsConstants.REQUEST_CODE_NOTIFICATION_PERMISSION
            )
        }
    }

    @Test
    fun `requestNotificationPermission does nothing when already granted on API 33`() {
        setSdkInt(Build.VERSION_CODES.TIRAMISU)
        val activity = mockk<Activity>()
        mockkStatic(ContextCompat::class)
        every {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } returns PackageManager.PERMISSION_GRANTED
        mockkStatic(ActivityCompat::class)

        PermissionsHelper.requestNotificationPermission(activity)

        verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
    }

    @Test
    fun `requestNotificationPermission does nothing below API 33`() {
        setSdkInt(Build.VERSION_CODES.S_V2)
        val activity = mockk<Activity>()
        mockkStatic(ActivityCompat::class)

        PermissionsHelper.requestNotificationPermission(activity)

        verify(exactly = 0) { ActivityCompat.requestPermissions(any(), any(), any()) }
    }

    private fun setSdkInt(value: Int) {
        ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", value)
    }
}
