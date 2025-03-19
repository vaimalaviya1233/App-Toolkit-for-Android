package com.d4rk.android.apps.apptoolkit.app.settings.settings.utils.providers

import android.content.Context
import android.content.Intent
import com.d4rk.android.libs.apptoolkit.app.settings.utils.providers.PrivacySettingsProvider

class AppPrivacySettingsProvider(val context : Context) : PrivacySettingsProvider {

    override fun openPermissionsScreen() {
      //  IntentsHelper.openActivity(context = context , activityClass = PermissionsSettingsActivity::class.java)
    }

    override fun openAdsScreen() {
     //   IntentsHelper.openActivity(context = context , activityClass = AdsSettingsActivity::class.java)
    }

    override fun openUsageAndDiagnosticsScreen() {

/*        val intent : Intent = Intent(context , GeneralSettingsActivity::class.java).apply {
            putExtra("extra_title" , context.getString(com.d4rk.android.libs.apptoolkit.R.string.usage_and_diagnostics))
            putExtra("extra_content" , SettingsContent.USAGE_AND_DIAGNOSTICS.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)*/
    }
}
