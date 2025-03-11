package com.d4rk.android.libs.apptoolkit.ui.screens.settings.about

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.ui.components.preferences.PreferenceItem
import com.d4rk.android.libs.apptoolkit.ui.components.snackbar.Snackbar
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper
import com.d4rk.android.libs.apptoolkit.data.interfaces.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.ui.components.network.rememberHtmlData

@Composable
fun AboutSettingsList(
    paddingValues : PaddingValues = PaddingValues() ,
    provider : AboutSettingsProvider ,
) {
    val context : Context = LocalContext.current

    val htmlData : State<Pair<String? , String?>> = rememberHtmlData(packageName = provider.packageName , currentVersionName = provider.appVersion , context = context)
    val changelogHtmlString : String? = htmlData.value.first
    val eulaHtmlString : String? = htmlData.value.second

    var showSnackbar : Boolean by remember { mutableStateOf(value = false) }

    Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight()
        ) {
            item {
                PreferenceCategoryItem(title = stringResource(id = R.string.app_info))
                PreferenceItem(
                    title = provider.appName ,
                    summary = provider.copyrightText ,
                )
                PreferenceItem(
                    title = stringResource(id = R.string.app_build_version) , summary = provider.appVersion + " (${provider.appVersionCode})"
                )
                PreferenceItem(title = stringResource(id = R.string.oss_license_title) , summary = stringResource(id = R.string.summary_preference_settings_oss) , onClick = {
                    IntentsHelper.openLicensesScreen(
                        context = context , eulaHtmlString = eulaHtmlString , changelogHtmlString = changelogHtmlString , appName = provider.appName , appVersion = provider.appVersion , appVersionCode = provider.appVersionCode , appShortDescription = R.string.app_short_description
                    )
                })
            }
            item {
                PreferenceCategoryItem(title = stringResource(id = R.string.device_info))
            }
            item {
                PreferenceItem(title = stringResource(id = R.string.device_info) , summary = provider.deviceInfo , onClick = {
                    ClipboardHelper.copyTextToClipboard(context = context , label = "Device Info" , text = provider.deviceInfo , onShowSnackbar = {
                        showSnackbar = true
                    })
                })
            }
        }

        Snackbar(message = stringResource(id = R.string.snack_device_info_copied) , showSnackbar = showSnackbar , onDismiss = { showSnackbar = false })
    }
}