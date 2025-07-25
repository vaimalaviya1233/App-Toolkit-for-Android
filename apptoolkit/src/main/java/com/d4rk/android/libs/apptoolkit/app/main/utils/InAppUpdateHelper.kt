package com.d4rk.android.libs.apptoolkit.app.main.utils

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.tasks.await

/**
 * Helper object for performing in-app updates using the Play Core library.
 */
object InAppUpdateHelper {

    /**
     * Checks for available updates and attempts to start the update flow if an update is found.
     *
     * @param appUpdateManager The [AppUpdateManager] instance used to query and start updates.
     * @param updateResultLauncher Launcher used to start the update flow.
     */
    suspend fun performUpdate(
        appUpdateManager: AppUpdateManager,
        updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        runCatching {
            val appUpdateInfo: AppUpdateInfo = appUpdateManager.appUpdateInfo.await()
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val appUpdateOptions: AppUpdateOptions =
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateResultLauncher,
                    appUpdateOptions,
                )
            }
        }
    }
}
