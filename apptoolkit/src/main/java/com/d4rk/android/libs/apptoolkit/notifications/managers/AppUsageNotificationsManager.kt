package com.d4rk.android.libs.apptoolkit.notifications.managers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.d4rk.android.libs.apptoolkit.notifications.receivers.AppUsageNotificationReceiver
import java.util.concurrent.TimeUnit

/**
 * Utility class for managing app usage notifications.
 *
 * This class provides functionality to schedule periodic checks for app usage notifications
 * using WorkManager and a custom worker class.
 *
 * @property context The application context used for scheduling app usage checks.
 */
class AppUsageNotificationsManager(private val context: Context) {
    private val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAppUsageCheck(@StringRes notificationSummary: Int) {
        val intent = Intent(context, AppUsageNotificationReceiver::class.java).apply {
            putExtra("notification_summary", notificationSummary)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(3)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            TimeUnit.DAYS.toMillis(3),
            pendingIntent
        )
    }
}