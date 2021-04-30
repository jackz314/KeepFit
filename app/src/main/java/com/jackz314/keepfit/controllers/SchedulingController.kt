package com.jackz314.keepfit.controllers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Task
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.controllers.UserControllerKt.currentUserDoc
import com.jackz314.keepfit.models.ScheduledExercise
import com.jackz314.keepfit.views.ExerciseActivity
import com.jackz314.keepfit.views.PromptActivity
import java.util.*

object SchedulingController {

    @JvmStatic
    fun cancelScheduledExercise(context: Context, scheduledExercise: ScheduledExercise): Task<Void> {
        cancelReminderAlarm(context, scheduledExercise)
        cancelReminderNotification(context, scheduledExercise)
        return currentUserDoc.collection("scheduled_exercises").document(scheduledExercise.hashCode().toString()).delete()
    }

    @JvmStatic
    fun scheduleExercise(context: Context, scheduledExercise: ScheduledExercise): Boolean {
        if (!scheduledExercise.time.after(Date())) return false
        currentUserDoc.collection("scheduled_exercises").document(scheduledExercise.hashCode().toString()).set(scheduledExercise)
        scheduleReminderAlarm(context, scheduledExercise)
        return true
    }

    @JvmStatic
    fun pushReminderNotification(context: Context, scheduledExercise: ScheduledExercise) {
        val promptExerciseIntent = Intent(context, PromptActivity::class.java).apply {
            type = scheduledExercise.hashCode().toString()
            action = GlobalConstants.ACTION_EXERCISE
            putExtra(GlobalConstants.SCHEDULED_EXERCISE, scheduledExercise)
        }
        val startExerciseIntent = Intent(context, ExerciseActivity::class.java).apply {
            type = scheduledExercise.hashCode().toString()
            putExtra(GlobalConstants.SCHEDULED_EXERCISE, scheduledExercise)
            putExtra(GlobalConstants.EXERCISE_TYPE, scheduledExercise.category)
            putExtra(GlobalConstants.EXERCISE_INTENSITY, scheduledExercise.intensity)
        }
        val builder = NotificationCompat.Builder(context, GlobalConstants.REMINDER_NOTIF_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Time to Exercise")
                .setContentText("It's time to start your scheduled " + scheduledExercise.category.toLowerCase(Locale.getDefault()) + " exercise!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(PendingIntent.getActivity(context, 0, promptExerciseIntent, 0))
                .addAction(R.drawable.ic_baseline_directions_run_24, "Start",
                        PendingIntent.getActivity(context, 0, startExerciseIntent, 0))
                .addAction(R.drawable.ic_baseline_cancel_24, "Cancel",
                        PendingIntent.getBroadcast(context, GlobalConstants.RC_CANCEL_REMINDER,
                                generateIntentForScheduledExercise(context, scheduledExercise, GlobalConstants.ACTION_CANCEL_REMINDER), 0))
                .setAutoCancel(false)
        with(NotificationManagerCompat.from(context)) {
            notify(scheduledExercise.hashCode(), builder.build())
        }
    }

    @JvmStatic
    fun cancelReminderNotification(context: Context, scheduledExercise: ScheduledExercise) {
        with(NotificationManagerCompat.from(context)) {
            cancel(scheduledExercise.hashCode())
        }
    }

    @JvmStatic
    fun createNotificationChannels(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Exercise Reminders"
            val descriptionText = "Notifications for exercise reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(GlobalConstants.REMINDER_NOTIF_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @JvmStatic
    fun scheduleReminderAlarm(context: Context, scheduledExercise: ScheduledExercise) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = generateIntentForScheduledExercise(context, scheduledExercise)
        val pendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.RC_TRIGGER_REMINDER, intent, 0)
        val alarmTriggerTime = scheduledExercise.time.time
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTriggerTime, pendingIntent)
    }

    @JvmStatic
    fun cancelReminderAlarm(context: Context, scheduledExercise: ScheduledExercise) {
        val ogPendingIntent = PendingIntent.getBroadcast(context, GlobalConstants.RC_TRIGGER_REMINDER, generateIntentForScheduledExercise(context, scheduledExercise), 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(ogPendingIntent)
    }

    @JvmStatic
    fun generateIntentForScheduledExercise(context: Context, scheduledExercise: ScheduledExercise, action: String = GlobalConstants.ACTION_TRIGGER_REMINDER): Intent {
        return Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = action
            type = scheduledExercise.hashCode().toString() // to uniquely identify the intent
            putExtra(GlobalConstants.SCHEDULED_EXERCISE, Bundle().apply { putSerializable(GlobalConstants.SCHEDULED_EXERCISE, scheduledExercise) })
        }
    }
}