package com.d4rk.android.libs.apptoolkit.notifications.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.d4rk.android.libs.apptoolkit.R
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.core.net.toUri

class AppUpdateNotificationsManager(private val context : Context , private val channelId : String) {
    private val updateNotificationId : Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAndSendUpdateNotification() {
        val notificationManager : NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val appUpdateInfoTask : Task<AppUpdateInfo> = AppUpdateManagerFactory.create(context).appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.FLEXIBLE
                )
            ) {
                val updateChannel = NotificationChannel(
                    channelId , context.getString(R.string.update_notifications) , NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(updateChannel)
                val updateBuilder : NotificationCompat.Builder =
                        NotificationCompat.Builder(context , channelId).setSmallIcon(R.drawable.ic_notification_update).setContentTitle(context.getString(R.string.notification_update_title)).setContentText(context.getString(R.string.summary_notification_update)).setAutoCancel(true).setContentIntent(
                                    PendingIntent.getActivity(
                                        context , 0 , Intent(
                                            Intent.ACTION_VIEW , "market://details?id=${context.packageName}".toUri()
                                        ) , PendingIntent.FLAG_IMMUTABLE
                                    )
                                )
                notificationManager.notify(updateNotificationId , updateBuilder.build())
            }
        }
    }
}