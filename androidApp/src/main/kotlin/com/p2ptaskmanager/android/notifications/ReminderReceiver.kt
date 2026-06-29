package com.p2ptaskmanager.android.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.p2ptaskmanager.android.R

const val CHANNEL_REMINDER = "p2ptask_reminders"
const val CHANNEL_STREAK = "p2ptask_streak"
const val EXTRA_TASK_TITLE = "task_title"
const val EXTRA_TASK_ID = "task_id"
const val EXTRA_TYPE = "notification_type"
const val TYPE_REMINDER = "reminder"
const val TYPE_STREAK = "streak"
const val TYPE_MORNING = "morning"

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannels(nm)

        val type = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_REMINDER
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task"
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: ""

        val (channelId, title, body) = when (type) {
            TYPE_STREAK -> Triple(CHANNEL_STREAK, "Don't break your streak!", "Keep up your habit — complete a task today.")
            TYPE_MORNING -> Triple(CHANNEL_REMINDER, "Plan your day", "Open P2P Task Manager to see today's priorities.")
            else -> Triple(CHANNEL_REMINDER, "Reminder: $taskTitle", "Your task is due soon!")
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        nm.notify(taskId.hashCode().takeIf { it != 0 } ?: type.hashCode(), notification)
    }

    private fun createChannels(nm: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_REMINDER, "Task Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ))
            nm.createNotificationChannel(NotificationChannel(
                CHANNEL_STREAK, "Streak Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ))
        }
    }
}
