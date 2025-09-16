package com.d4rk.android.apps.apptoolkit.core.broadcast

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class FavoritesChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val pkg = intent?.getStringExtra(EXTRA_PACKAGE_NAME)
        if (pkg.isNullOrEmpty()) {
            Log.w(TAG, "Favorites changed intent missing package name extra")
        } else {
            Log.d(TAG, "Favorites changed: $pkg")
        }
    }

    companion object {
        const val ACTION_FAVORITES_CHANGED = "com.d4rk.android.apps.apptoolkit.action.FAVORITES_CHANGED"
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        private const val TAG = "FavoritesChangedRcvr"

        fun createIntentWithPackage(context: Context, packageName: String): Intent =
            baseIntent(context).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }

        fun createIntentWithoutPackage(context: Context): Intent = baseIntent(context)

        private fun baseIntent(context: Context): Intent =
            Intent(ACTION_FAVORITES_CHANGED).apply {
                component = ComponentName(context, FavoritesChangedReceiver::class.java)
            }
    }
}
