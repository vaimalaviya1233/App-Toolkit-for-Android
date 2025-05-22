package com.d4rk.android.apps.apptoolkit.app.onboarding.utils.interfaces.providers

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Star
import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.apps.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider


class AppOnboardingProvider : OnboardingProvider {

    override fun getOnboardingPages(context: Context): List<OnboardingPage> {
        return listOf(
            OnboardingPage(
                key = "welcome",
                title = context.getString(R.string.onboarding_welcome_title),
                description = context.getString(R.string.onboarding_welcome_description),
                imageVector = Icons.Outlined.Star,
                isEnabled = true
            ),
            OnboardingPage(
                key = "theme",
                title = context.getString(R.string.onboarding_theme_title),
                description = context.getString(R.string.onboarding_theme_description),
                imageVector = Icons.Outlined.Palette,
                isEnabled = true
            ),
            OnboardingPage(
                key = "currency",
                title = context.getString(R.string.onboarding_currency_title),
                description = context.getString(R.string.onboarding_currency_description),
                imageVector = Icons.Outlined.AttachMoney,
                isEnabled = true
            ),
            OnboardingPage(
                key = "analytics",
                title = context.getString(R.string.onboarding_analytics_title),
                description = context.getString(R.string.onboarding_analytics_description),
                imageVector = Icons.Outlined.BarChart,
                isEnabled = BuildConfig.DEBUG.not() // Disable for debug builds
            )
        ).filter { it.isEnabled }
    }

    override fun onOnboardingFinished(context: Context) {

    }
}