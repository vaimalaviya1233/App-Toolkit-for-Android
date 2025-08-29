package com.d4rk.android.apps.apptoolkit.app.onboarding.utils.interfaces.providers

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.d4rk.android.apps.apptoolkit.app.onboarding.utils.constants.OnboardingKeys
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TestAppOnboardingProvider {

    @Test
    fun `getOnboardingPages returns expected pages`() {
        println("🚀 [TEST] getOnboardingPages returns expected pages")
        val context = mockk<Context> {
            every { getString(any()) } returns ""
        }
        val provider = AppOnboardingProvider()

        val pages = provider.getOnboardingPages(context)

        assertThat(pages).hasSize(6)
        assertThat(pages.map { it.key }).containsExactly(
            OnboardingKeys.WELCOME,
            OnboardingKeys.PERSONALIZATION_OPTIONS,
            OnboardingKeys.THEME_OPTIONS,
            OnboardingKeys.FEATURE_HIGHLIGHT_1,
            OnboardingKeys.CRASHLYTICS_OPTIONS,
            OnboardingKeys.ONBOARDING_COMPLETE,
        ).inOrder()
        println("🏁 [TEST DONE] getOnboardingPages returns expected pages")
    }

    @Test
    fun `onOnboardingFinished starts activity when resolvable`() {
        println("🚀 [TEST] onOnboardingFinished starts activity when resolvable")
        val packageManager = mockk<PackageManager>()
        every { packageManager.resolveActivity(any(), any()) } returns ComponentName("pkg", "cls")

        val activity = mockk<Activity>(relaxed = true)
        every { activity.packageManager } returns packageManager

        val provider = AppOnboardingProvider()
        provider.onOnboardingFinished(activity)

        verify { activity.startActivity(any()) }
        verify { activity.finish() }
        println("🏁 [TEST DONE] onOnboardingFinished starts activity when resolvable")
    }

    @Test
    fun `onOnboardingFinished does nothing when intent not resolved`() {
        println("🚀 [TEST] onOnboardingFinished does nothing when intent not resolved")
        val packageManager = mockk<PackageManager>()
        every { packageManager.resolveActivity(any(), any()) } returns null

        val activity = mockk<Activity>(relaxed = true)
        every { activity.packageManager } returns packageManager

        val provider = AppOnboardingProvider()
        provider.onOnboardingFinished(activity)

        verify(exactly = 0) { activity.startActivity(any()) }
        verify(exactly = 0) { activity.finish() }
        println("🏁 [TEST DONE] onOnboardingFinished does nothing when intent not resolved")
    }
}

