package com.d4rk.android.apps.apptoolkit.app.startup.utils.interfaces.providers

import android.Manifest
import android.content.Context
import android.os.Build
import com.d4rk.android.libs.apptoolkit.app.onboarding.ui.OnboardingActivity
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class AppStartupProviderTest {
    private val provider = AppStartupProvider()

    @Test
    fun `required permissions reflect platform`() {
        val expected = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
        assertThat(provider.requiredPermissions).isEqualTo(expected)
    }

    @Test
    fun `getNextIntent targets onboarding activity`() {
        val context = mockk<Context>(relaxed = true)
        every { context.packageName } returns "com.test"
        val intent = provider.getNextIntent(context)
        assertThat(intent.component?.className).isEqualTo(OnboardingActivity::class.java.name)
    }
}
