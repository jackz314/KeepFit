package com.jackz314.keepfit.controllers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.UtilsKt.formatDurationTextString
import com.jackz314.keepfit.controllers.UserControllerKt.currentUserDoc
import com.jackz314.keepfit.models.Exercise
import com.jackz314.keepfit.models.ScheduledExercise
import com.jackz314.keepfit.views.ExerciseActivity
import com.jackz314.keepfit.views.MainActivity
import com.jackz314.keepfit.views.PromptActivity
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

private const val TAG = "SchedulingController"

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
            val name2 = "Daily Summary"
            val desc2 = "Notifications for daily summaries"
            val channel2 = NotificationChannel(GlobalConstants.DAILY_NOTIF_CHANNEL_ID, name2, importance).apply {
                description = desc2
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channel2)
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

    @JvmOverloads
    @JvmStatic
    fun getIntentForDailyNotification(context: Context, flag: Int = 0): PendingIntent? {
        return Intent(context, ReminderBroadcastReceiver::class.java).apply {
            this.action = GlobalConstants.ACTION_DAILY_NOTIF
        }.let {
            PendingIntent.getBroadcast(context, GlobalConstants.RC_TRIGGER_DAILY, it, flag)
        }
    }

    @JvmStatic
    fun scheduleDailyNotifications(context: Context, timeMillis: Long) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putLong(GlobalConstants.DAILY_NOTIF_SCHEDULED_TIME, timeMillis).apply()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentForDailyNotification = getIntentForDailyNotification(context)
        alarmManager.cancel(intentForDailyNotification)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeMillis, AlarmManager.INTERVAL_DAY, intentForDailyNotification)
    }

    @JvmStatic
    fun cancelDailyNotifications(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(getIntentForDailyNotification(context))
    }

    @JvmStatic
    fun pushDailyNotification(context: Context): Task<Unit> {
        val todayStartTime = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())
        return currentUserDoc.collection("exercises").whereGreaterThanOrEqualTo("starting_time", todayStartTime).get().continueWith {
            if (!it.isSuccessful || it.exception != null || it.result == null) {
                Log.w(TAG, "Listen failed.", it.exception)
            } else {
                // today exercise summary stuff
                val todayExercises = it.result!!.toObjects(Exercise::class.java)
                val todayCal = ExerciseController.getTotalCalories(todayExercises)
                val todayExTime = ExerciseController.getTotalExerciseTime(todayExercises)

                val builder = NotificationCompat.Builder(context, GlobalConstants.REMINDER_NOTIF_CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Daily Summary ${DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)}")
                        .setContentText("You exercised ${formatDurationTextString(todayExTime / DateUtils.SECOND_IN_MILLIS)} today and burned ${"%.2f".format(todayCal)} Cal")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0))
                        .setAutoCancel(true)
                with(NotificationManagerCompat.from(context)) {
                    notify(GlobalConstants.DAILY_NOTIF_ID, builder.build()) // updates existing one if exists
                }
            }
        }
    }
}