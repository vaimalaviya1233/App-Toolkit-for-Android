package com.d4rk.android.libs.apptoolkit.app.about.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.AboutSettingsProvider
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.BuildInfoProvider
import com.d4rk.android.libs.apptoolkit.core.ui.components.network.rememberHtmlData
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.PreferenceCategoryItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.preferences.SettingsPreferenceItem
import com.d4rk.android.libs.apptoolkit.core.ui.components.snackbar.Snackbar
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.ExtraTinyVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.ui.components.spacers.SmallVerticalSpacer
import com.d4rk.android.libs.apptoolkit.core.utils.constants.ui.SizeConstants
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.ClipboardHelper
import com.d4rk.android.libs.apptoolkit.core.utils.helpers.IntentsHelper

@Composable
fun AboutSettingsList(paddingValues : PaddingValues = PaddingValues() , deviceProvider : AboutSettingsProvider , configProvider : BuildInfoProvider) {
    val context : Context = LocalContext.current

    val htmlData : State<Pair<String? , String?>> = rememberHtmlData(packageName = configProvider.packageName , currentVersionName = configProvider.appVersion , context = context)
    val changelogHtmlString : String? = htmlData.value.first
    val eulaHtmlString : String? = htmlData.value.second

    var showSnackbar : Boolean by remember { mutableStateOf(value = false) }

    val deviceInfo : String = stringResource(id = R.string.device_info)

    Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
        LazyColumn(
            modifier = Modifier.fillMaxHeight()
        ) {
            item {
                PreferenceCategoryItem(title = stringResource(id = R.string.app_info))
                SmallVerticalSpacer()
                Column(modifier = Modifier
                        .padding(horizontal = SizeConstants.LargeSize)
                        .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))) {
                    SettingsPreferenceItem(title = stringResource(id = R.string.app_full_name) , summary = stringResource(id = R.string.copyright))
                    ExtraTinyVerticalSpacer()
                    SettingsPreferenceItem(title = stringResource(id = R.string.app_build_version) , summary = configProvider.appVersion + " (${configProvider.appVersionCode})")
                    ExtraTinyVerticalSpacer()
                    SettingsPreferenceItem(title = stringResource(id = R.string.oss_license_title) , summary = stringResource(id = R.string.summary_preference_settings_oss) , onClick = {
                        IntentsHelper.openLicensesScreen(
                            context = context ,
                            eulaHtmlString = eulaHtmlString ,
                            changelogHtmlString = changelogHtmlString ,
                            appName = context.getString(R.string.app_name) ,
                            appVersion = configProvider.appVersion ,
                            appVersionCode = configProvider.appVersionCode ,
                            appShortDescription = R.string.app_short_description
                        )
                    })
                }

            }
            item {
                PreferenceCategoryItem(title = deviceInfo)
                SmallVerticalSpacer()
                Column(modifier = Modifier
                        .padding(horizontal = SizeConstants.LargeSize)
                        .clip(shape = RoundedCornerShape(size = SizeConstants.LargeSize))) {
                    SettingsPreferenceItem(title = deviceInfo , summary = deviceProvider.deviceInfo , onClick = {
                        ClipboardHelper.copyTextToClipboard(context = context , label = deviceInfo , text = deviceProvider.deviceInfo , onShowSnackbar = {
                            showSnackbar = true
                        })
                    })
                }
            }
        }

        Snackbar(message = stringResource(id = R.string.snack_device_info_copied) , showSnackbar = showSnackbar , onDismiss = { showSnackbar = false })
    }
}