package com.d4rk.android.apps.apptoolkit.app.onboarding.utils.interfaces.providers

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Star
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainActivity
import com.d4rk.android.apps.apptoolkit.app.onboarding.ui.tabs.CustomFunOnboardingPageTab
import com.d4rk.android.libs.apptoolkit.app.oboarding.domain.data.model.ui.OnboardingPage
import com.d4rk.android.libs.apptoolkit.app.oboarding.utils.interfaces.providers.OnboardingProvider

class AppOnboardingProvider : OnboardingProvider {

    override fun getOnboardingPages(context: Context): List<OnboardingPage> {
        return listOf(
            OnboardingPage.DefaultPage(
                key = "welcome",
                title = "Welcome to App Toolkit!",
                description = "Discover a powerful suite of tools to enhance your Android experience.",
                imageVector = Icons.Outlined.Star
            ),
            OnboardingPage.DefaultPage(
                key = "feature_highlight_1",
                title = "Core Functionality",
                description = "Learn about the essential tools that App Toolkit offers to simplify your tasks.",
                imageVector = Icons.Outlined.Build
            ),
            OnboardingPage.DefaultPage(
                key = "personalization_options",
                title = "Customize Your Experience",
                description = "Tailor App Toolkit to your needs with various personalization settings.",
                imageVector = Icons.Outlined.AccountCircle
            ),
            OnboardingPage.DefaultPage(
                key = "pro_features_teaser",
                title = "Unlock More Power",
                description = "Consider upgrading to access exclusive features and advanced capabilities.",
                imageVector = Icons.Outlined.FavoriteBorder
            ),
            OnboardingPage.CustomPage(
                key = "custom-fun",
                content = {
                    CustomFunOnboardingPageTab()
                }
            )
        ).filter {
            when (it) {
                is OnboardingPage.DefaultPage -> it.isEnabled
                is OnboardingPage.CustomPage -> it.isEnabled
            }
        }
    }

    override fun onOnboardingFinished(context: Context) {
        context.startActivity(Intent(context, MainActivity::class.java))
    }
}