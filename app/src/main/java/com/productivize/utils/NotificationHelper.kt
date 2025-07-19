package com.productivize.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.productivize.R
import com.productivize.data.model.Settings
import com.productivize.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID_REMINDERS = "productivity_reminders"
        private const val NOTIFICATION_ID_HOURLY = 1001
        private const val NOTIFICATION_ID_JOURNAL = 1002
        private const val NOTIFICATION_ID_BACKUP = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Productivity Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Hourly and journal reminders to track your productivity"
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showHourlyReminder(settings: Settings) {
        if (!settings.notificationsEnabled) return
        if (!areNotificationsEnabled()) return

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Rate This Hour")
                .setContentText("How productive was this hour? Tap to rate it!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_HOURLY, notification)
        } catch (e: SecurityException) {
            // Permission not granted, fail silently
        } catch (e: Exception) {
            // Any other exception, log and fail silently
            e.printStackTrace()
        }
    }

    fun showJournalReminder(settings: Settings) {
        if (!settings.notificationsEnabled || !settings.journalReminders) return
        if (!areNotificationsEnabled()) return

        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                // Navigate to journal screen
                putExtra("navigate_to", "journal")
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Daily Reflection Time")
                .setContentText("Take a moment to reflect on your day and plan tomorrow")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_JOURNAL, notification)
        } catch (e: SecurityException) {
            // Permission not granted, fail silently
        } catch (e: Exception) {
            // Any other exception, log and fail silently
            e.printStackTrace()
        }
    }

    fun cancelAllNotifications() {
        try {
            NotificationManagerCompat.from(context).cancelAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAutoBackupEnabled() {
        if (!areNotificationsEnabled()) return
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Auto-Backup Enabled")
            .setContentText("Your productivity data will be automatically backed up daily")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_BACKUP, notification)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
} 