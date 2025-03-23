package com.d4rk.android.libs.apptoolkit.app.settings.utils.providers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.d4rk.android.libs.apptoolkit.app.about.ui.AboutSettingsList
import com.d4rk.android.libs.apptoolkit.app.advanced.ui.AdvancedSettingsList
import com.d4rk.android.libs.apptoolkit.app.diagnostics.ui.UsageAndDiagnosticsList
import com.d4rk.android.libs.apptoolkit.app.display.DisplaySettingsList
import com.d4rk.android.libs.apptoolkit.app.display.theme.ThemeSettingsList
import com.d4rk.android.libs.apptoolkit.app.privacy.ui.PrivacySettingsList
import com.d4rk.android.libs.apptoolkit.app.settings.utils.constants.SettingsContent

class GeneralSettingsContentProvider(
    private val deviceProvider : AboutSettingsProvider ,
    private val advancedProvider : AdvancedSettingsProvider ,
    private val displayProvider : DisplaySettingsProvider ,
    private val privacyProvider : PrivacySettingsProvider ,
    private val configProvider : BuildInfoProvider ,
    private val customScreens : Map<String , @Composable (PaddingValues) -> Unit> = emptyMap()
) {
    @Composable
    fun ProvideContent(contentKey : String? , paddingValues : PaddingValues) {
        when (contentKey) {
            SettingsContent.ABOUT -> AboutSettingsList(paddingValues = paddingValues , deviceProvider = deviceProvider , configProvider = configProvider)
            SettingsContent.ADVANCED -> AdvancedSettingsList(paddingValues = paddingValues , provider = advancedProvider)
            SettingsContent.DISPLAY -> DisplaySettingsList(paddingValues = paddingValues , provider = displayProvider)
            SettingsContent.SECURITY_AND_PRIVACY -> PrivacySettingsList(paddingValues = paddingValues , provider = privacyProvider)
            SettingsContent.THEME -> ThemeSettingsList(paddingValues = paddingValues)
            SettingsContent.USAGE_AND_DIAGNOSTICS -> UsageAndDiagnosticsList(paddingValues = paddingValues , configProvider = configProvider)
            else -> customScreens[contentKey]?.invoke(paddingValues)
        }
    }
}