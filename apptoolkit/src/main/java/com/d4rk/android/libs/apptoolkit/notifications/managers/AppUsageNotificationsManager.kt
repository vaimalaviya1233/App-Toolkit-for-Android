package com.d4rk.android.libs.apptoolkit.notifications.managers

import android.content.Context
import androidx.annotation.StringRes
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.d4rk.android.libs.apptoolkit.notifications.workers.AppUsageNotificationWorker
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing app usage notifications.
 *
 * This class provides functionality to schedule periodic checks for app usage notifications
 * using WorkManager and a custom worker class.
 *
 * @property context The application context used for scheduling app usage checks.
 */
class AppUsageNotificationsManager(private val context : Context) {

    fun scheduleAppUsageCheck(@StringRes notificationSummary : Int) {
        val request = PeriodicWorkRequestBuilder<AppUsageNotificationWorker>(
            3 , TimeUnit.DAYS
        )
            .setInputData(
                workDataOf("notification_summary" to notificationSummary)
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "AppUsageNotification" ,
            ExistingPeriodicWorkPolicy.UPDATE ,
            request
        )
    }
}