package com.d4rk.android.apps.apptoolkit.app.startup.utils.interfaces.providers

import android.content.Context
import com.d4rk.android.libs.apptoolkit.app.startup.utils.interfaces.providers.StartupProvider
import javax.inject.Inject
import android.content.Intent
import com.d4rk.android.apps.apptoolkit.app.main.ui.MainActivity
import com.google.android.ump.ConsentRequestParameters
import android.Manifest

class AppStartupProvider @Inject constructor() : StartupProvider {
    override val requiredPermissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    override val consentRequestParameters = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false).build()
    override fun getNextIntent(context : Context) = Intent(context , MainActivity::class.java)
}
