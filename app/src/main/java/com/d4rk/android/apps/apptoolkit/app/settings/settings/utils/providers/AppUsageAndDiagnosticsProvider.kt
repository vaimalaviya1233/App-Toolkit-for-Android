package com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers

import com.d4rk.android.apps.apptoolkit.BuildConfig
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.UsageAndDiagnosticsSettingsProvider

class AppUsageAndDiagnosticsProvider : UsageAndDiagnosticsSettingsProvider {

    override val isDebugBuild : Boolean
        get() {
            return BuildConfig.DEBUG
        }
}