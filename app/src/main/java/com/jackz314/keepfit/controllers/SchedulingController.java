package com.jackz314.keepfit.controllers;

import com.jackz314.keepfit.models.ScheduledExercise;

public class SchedulingController {

    public static void deleteScheduledExercise(String uid) {
        UserControllerKt.getCurrentUserDoc().collection("scheduled_exercises").document(uid).delete();
    }

    public static void scheduleExercise(ScheduledExercise scheduledExercise) {
        UserControllerKt.getCurrentUserDoc().collection("scheduled_exercises").add(scheduledExercise);
    }
}
