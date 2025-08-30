package com.d4rk.android.apps.apptoolkit.app.startup.utils.interfaces.providers

import android.Manifest
import android.content.Context
import android.os.Build
import com.d4rk.android.libs.apptoolkit.app.onboarding.ui.OnboardingActivity
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field

class AppStartupProviderTest {

    private var originalSdk: Int = Build.VERSION.SDK_INT

    @BeforeEach
    fun setup() {
        originalSdk = Build.VERSION.SDK_INT
    }

    @AfterEach
    fun tearDown() {
        setSdk(originalSdk)
    }

    @Test
    fun `required permissions contains post notifications on api 33 plus`() {
        setSdk(Build.VERSION_CODES.TIRAMISU)
        val provider = AppStartupProvider()
        assertThat(provider.requiredPermissions.asList())
            .containsExactly(Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `required permissions empty below api 33`() {
        setSdk(Build.VERSION_CODES.S_V2)
        val provider = AppStartupProvider()
        assertThat(provider.requiredPermissions.toList()).isEmpty()
    }

    @Test
    fun `get next intent returns onboarding activity`() {
        val context = mockk<Context>(relaxed = true)
        val provider = AppStartupProvider()
        val intent = provider.getNextIntent(context)
        assertThat(intent.component?.className).isEqualTo(OnboardingActivity::class.java.name)
    }

    @Test
    fun `consent request parameters not null`() {
        val provider = AppStartupProvider()
        assertThat(provider.consentRequestParameters).isNotNull()
    }

    private fun setSdk(value: Int) {
        val field: Field = Build.VERSION::class.java.getDeclaredField("SDK_INT")
        field.isAccessible = true
        field.setInt(null, value)
    }
}

