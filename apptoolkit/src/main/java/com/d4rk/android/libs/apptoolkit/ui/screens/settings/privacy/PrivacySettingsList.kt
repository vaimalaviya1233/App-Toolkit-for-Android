package com.d4rk.android.libs.apptoolkit.ui.screens.settings.privacy

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.utils.interfaces.providers.PrivacySettingsProvider

@Composable
fun PrivacySettingsList(
    paddingValues : PaddingValues = PaddingValues() , provider : PrivacySettingsProvider
) {
    val context : Context = LocalContext.current

    LazyColumn(
        modifier = Modifier
                .fillMaxHeight()
                .padding(paddingValues = paddingValues) ,
    ) {
        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.privacy))
            PreferenceItem(title = stringResource(id = R.string.privacy_policy) , summary = stringResource(id = R.string.summary_preference_settings_privacy_policy) , onClick = { IntentsHelper.openUrl(context , provider.privacyPolicyUrl) })
            PreferenceItem(title = stringResource(id = R.string.terms_of_service) , summary = stringResource(id = R.string.summary_preference_settings_terms_of_service) , onClick = { IntentsHelper.openUrl(context , provider.termsOfServiceUrl) })
            PreferenceItem(title = stringResource(id = R.string.code_of_conduct) , summary = stringResource(id = R.string.summary_preference_settings_code_of_conduct) , onClick = { IntentsHelper.openUrl(context , provider.codeOfConductUrl) })
            PreferenceItem(title = stringResource(id = R.string.permissions) , summary = stringResource(id = R.string.summary_preference_settings_permissions) , onClick = { provider.openPermissionsScreen() })
            PreferenceItem(title = stringResource(id = R.string.ads) , summary = stringResource(id = R.string.summary_preference_settings_ads) , onClick = { provider.openAdsScreen() })
            PreferenceItem(title = stringResource(id = R.string.usage_and_diagnostics) , summary = stringResource(id = R.string.summary_preference_settings_usage_and_diagnostics) , onClick = { provider.openUsageAndDiagnosticsScreen() })
        }
        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.legal))
            PreferenceItem(title = stringResource(id = R.string.legal_notices) , summary = stringResource(id = R.string.summary_preference_settings_legal_notices) , onClick = { IntentsHelper.openUrl(context , provider.legalNoticesUrl) })
            PreferenceItem(title = stringResource(id = R.string.license) , summary = stringResource(id = R.string.summary_preference_settings_license) , onClick = { IntentsHelper.openUrl(context , provider.licenseUrl) })
        }
    }
}