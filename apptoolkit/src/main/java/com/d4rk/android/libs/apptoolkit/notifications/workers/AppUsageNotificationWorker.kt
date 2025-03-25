package com.d4rk.android.libs.apptoolkit.notifications.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.d4rk.android.libs.apptoolkit.R
import com.d4rk.android.libs.apptoolkit.data.datastore.CommonDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Worker class responsible for app usage notifications.
 *
 * This worker class extends the WorkManager's Worker class to perform background tasks for
 * app usage notifications. It checks the last app usage timestamp stored in preferences
 * and triggers a notification if the threshold for notification has been exceeded.
 *
 * @property context The application context used for accessing system services and resources.
 * @property workerParams The parameters for this worker instance.
 */
class AppUsageNotificationWorker(
    context : Context , workerParams : WorkerParameters
) : Worker(context , workerParams) {
    private val dataStore : CommonDataStore = CommonDataStore.getInstance(context)
    private val appUsageChannelId : String = "app_usage_channel"
    private val appUsageNotificationId : Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun doWork() : Result {
        val currentTimestamp = System.currentTimeMillis()
        val notificationThreshold = 3 * 24 * 60 * 60 * 1000
        val lastUsedTimestamp = runBlocking { dataStore.lastUsed.first() }

        if (currentTimestamp - lastUsedTimestamp > notificationThreshold) {
            val notificationSummary = inputData.getInt("notification_summary" , R.string.default_notification_summary)

            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            val appUsageChannel = NotificationChannel(
                appUsageChannelId , applicationContext.getString(R.string.app_usage_notifications) , NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(appUsageChannel)

            val notificationBuilder =
                    NotificationCompat.Builder(applicationContext , appUsageChannelId).setSmallIcon(R.drawable.ic_notification_important).setContentTitle(applicationContext.getString(R.string.notification_last_time_used_title)).setContentText(applicationContext.getString(notificationSummary))
                            .setAutoCancel(true)

            notificationManager.notify(appUsageNotificationId , notificationBuilder.build())
        }

        runBlocking { dataStore.saveLastUsed(timestamp = currentTimestamp) }
        return Result.success()
    }
}