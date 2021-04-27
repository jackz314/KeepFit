package com.jackz314.keepfit.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.Query
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.models.ScheduledExercise

private const val TAG = "ReminderBroadcastReceiv"

class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Device boot complete, set the alarms here.
            if (FirebaseApp.getApps(context).isEmpty()) { // just in case
                FirebaseApp.initializeApp(context)
            }
            try {
                UserControllerKt.currentUserDoc.collection("scheduled_exercises")
                        .orderBy("time", Query.Direction.ASCENDING).get()
                        .addOnSuccessListener { snapshot -> snapshot.toObjects(ScheduledExercise::class.java).forEach { SchedulingController.scheduleReminderAlarm(context, it) } }
            } catch (e: Exception) {
                Log.e(TAG, "onReceive: error getting scheduled exercises", e)
            }
        } else if (intent.action == GlobalConstants.ACTION_TRIGGER_REMINDER) {
            val scheduledExercise = intent.getBundleExtra(GlobalConstants.SCHEDULED_EXERCISE)!!.getSerializable(GlobalConstants.SCHEDULED_EXERCISE) as ScheduledExercise
            Log.d(TAG, "onReceive: triggering reminder for $scheduledExercise")
            SchedulingController.pushReminderNotification(context, scheduledExercise)
        } else if (intent.action == GlobalConstants.ACTION_CANCEL_REMINDER) {
            val scheduledExercise = intent.getBundleExtra(GlobalConstants.SCHEDULED_EXERCISE)!!.getSerializable(GlobalConstants.SCHEDULED_EXERCISE) as ScheduledExercise
            if (FirebaseApp.getApps(context).isEmpty()) { // just in case
                FirebaseApp.initializeApp(context)
            }
            SchedulingController.cancelScheduledExercise(context, scheduledExercise)
            Log.d(TAG, "onReceive: cancelled reminder for scheduled exercise: $scheduledExercise")
            Toast.makeText(context, "Scheduled exercise cancelled!", Toast.LENGTH_SHORT).show()
        }
    }
}