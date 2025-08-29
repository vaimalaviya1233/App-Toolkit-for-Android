package com.d4rk.android.apps.apptoolkit.app.startup.utils.interfaces.providers

import android.Manifest
import android.content.Context
import android.os.Build
import com.d4rk.android.libs.apptoolkit.app.onboarding.ui.OnboardingActivity
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class AppStartupProviderTest {

    @AfterEach
    fun tearDown() {
        unmockkStatic(Build.VERSION::class)
    }

    @Test
    fun `required permissions include notifications on tiramisu or above`() {
        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.TIRAMISU

        val provider = AppStartupProvider()
        assertThat(provider.requiredPermissions.toList())
            .containsExactly(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `required permissions empty below tiramisu`() {
        mockkStatic(Build.VERSION::class)
        every { Build.VERSION.SDK_INT } returns Build.VERSION_CODES.S

        val provider = AppStartupProvider()
        assertThat(provider.requiredPermissions).isEmpty()
    }

    @Test
    fun `next intent points to onboarding activity`() {
        val provider = AppStartupProvider()
        val context = mockk<Context>(relaxed = true)

        val intent = provider.getNextIntent(context)
        assertThat(intent.component?.className)
            .isEqualTo(OnboardingActivity::class.qualifiedName)
        assertThat(provider.consentRequestParameters).isNotNull()
    }
}

