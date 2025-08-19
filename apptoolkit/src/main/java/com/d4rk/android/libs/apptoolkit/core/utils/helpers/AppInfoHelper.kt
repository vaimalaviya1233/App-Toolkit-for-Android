package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class AppInfoHelper {

    /**
     * Checks if a specific app is installed on the device.
     * @return True if the app is installed, false otherwise.
     */
    suspend fun isAppInstalled(context : Context , packageName : String) : Boolean = withContext(Dispatchers.IO) {
        runCatching { context.packageManager.getApplicationInfo(packageName , 0) }.isSuccess
    }

    /**
     * Opens a specific app if installed.
     * @return True if the app was opened, false otherwise.
     */
    suspend fun openApp(context : Context , packageName : String) : Boolean {
        val launchIntent = withContext(Dispatchers.IO) {
            runCatching { context.packageManager.getLaunchIntentForPackage(packageName) }.getOrNull()
        }

        return if (launchIntent != null) {
            context.startActivity(launchIntent)
            true
        } else {
            Toast.makeText(context , context.getString(R.string.app_not_installed) , Toast.LENGTH_SHORT).show()
            false
        }
    }
}