package com.d4rk.android.libs.apptoolkit.ui.screens.settings.advanced

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.data.interfaces.providers.AdvancedSettingsProvider

@Composable
fun AdvancedSettingsList(
    paddingValues : PaddingValues = PaddingValues() ,
    provider : AdvancedSettingsProvider ,
) {
    val context : Context = LocalContext.current

    LazyColumn(
        contentPadding = paddingValues,
        modifier = Modifier
                .fillMaxHeight()
    ) {
        item {
            PreferenceCategoryItem(title = stringResource(id = R.string.error_reporting))
            PreferenceItem(title = stringResource(id = R.string.bug_report) , summary = stringResource(id = R.string.summary_preference_settings_bug_report) , onClick = { IntentsHelper.openUrl(context = context , url = provider.bugReportUrl) })
        }
    }
}