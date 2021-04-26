package com.jackz314.keepfit.controllers;

import com.jackz314.keepfit.models.ScheduledExercise;

import java.util.Date;

public class SchedulingController {

    public static void deleteScheduledExercise(String uid) {
        UserControllerKt.getCurrentUserDoc().collection("scheduled_exercises").document(uid).delete();
    }

    public static boolean scheduleExercise(ScheduledExercise scheduledExercise) {
        if (!scheduledExercise.getTime().after(new Date())) return false;
        UserControllerKt.getCurrentUserDoc().collection("scheduled_exercises").add(scheduledExercise);
        return true;
    }
}
