package com.d4rk.android.libs.apptoolkit.notifications.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.d4rk.android.libs.apptoolkit.notifications.workers.AppUsageNotificationWorker

/**
 * AppUsageNotificationReceiver is a BroadcastReceiver responsible for triggering the
 * [AppUsageNotificationWorker] when a specific broadcast intent is received.
 *
 * This receiver is designed to listen for a specific broadcast (e.g., a custom intent
 * indicating a need to check app usage and potentially display a notification) and,
 * upon receiving it, enqueue a one-time work request to the WorkManager. This
 * worker will handle the actual app usage logic and notification handling.
 *
 * Key Responsibilities:
 *  1. Listen for a specific broadcast intent.
 *  2. Upon receiving the intent, create a [OneTimeWorkRequest] for [AppUsageNotificationWorker].
 *  3. Enqueue the work request with [WorkManager] to ensure the worker executes in the background.
 *
 *  Note:
 *  The specific intent that triggers this receiver needs to be registered in the Android Manifest or dynamically in the code
 *  using IntentFilter. Also the [AppUsageNotificationWorker] must be defined for it to work properly
 */
class AppUsageNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val notificationSummary = intent?.getIntExtra("notification_summary", -1) ?: return

        val workRequest = OneTimeWorkRequestBuilder<AppUsageNotificationWorker>()
                .setInputData(
                    workDataOf("notification_summary" to notificationSummary)
                )
                .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
