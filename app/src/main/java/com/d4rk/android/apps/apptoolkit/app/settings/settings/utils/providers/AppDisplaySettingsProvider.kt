package com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.constants.SettingsConstants
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.app.settings.general.ui.GeneralSettingsActivity
import com.d4rk.android.libs.apptoolkit.app.settings.utils.constants.SettingsContent
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.DisplaySettingsProvider

class AppDisplaySettingsProvider(val context : Context) : DisplaySettingsProvider {

    @Composable
    override fun LanguageSelectionDialog(onDismiss : () -> Unit , onLanguageSelected : (String) -> Unit) {
      /*  SelectLanguageAlertDialog(
            dataStore = AppCoreManager.dataStore , onDismiss = onDismiss , onLanguageSelected = onLanguageSelected
        )*/
    }

    @Composable
    override fun StartupPageDialog(onDismiss : () -> Unit , onStartupSelected : (String) -> Unit) {}

    override fun openThemeSettings() {
        GeneralSettingsActivity.start(
            context = context , title = context.getString(R.string.dark_theme) , contentKey = SettingsContent.THEME
        )
    }
}