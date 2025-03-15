package com.d4rk.android.libs.apptoolkit.core.utils.helpers

import android.content.Context
import android.content.res.Resources
import android.content.pm.PackageManager
import android.widget.Toast
import com.d4rk.android.libs.apptoolkit.R

object AppInfoHelper {

    /**
     * Checks if a specific app is installed on the device.
     * @param context The application context.
     * @param packageName The package name of the app to check.
     * @return True if the app is installed, false otherwise.
     */
     fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Opens a specific app if installed.
     * @param context The application context.
     * @param packageName The package name of the app to open.
     * @return True if the app was opened, false if not installed.
     */
     fun openApp(context: Context , packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        val res: Resources = context.resources
        return if (intent != null) {
            context.startActivity(intent)
            true
        } else {
            Toast.makeText(context , res.getString(R.string.app_not_installed) , Toast.LENGTH_SHORT).show()
            false
        }
    }
}