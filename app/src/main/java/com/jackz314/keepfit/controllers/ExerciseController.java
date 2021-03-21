package com.jackz314.keepfit.controllers;

import android.text.format.DateUtils;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.models.Exercise;
import com.jackz314.keepfit.models.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExerciseController {
    private static final String TAG = "ExerciseController";

    private User user;
    private double bmr;
    private float met = 10;
    private double calMultiplier;

    public ExerciseController(User user) {
        this.user = user;
        getUserBMR();
        getCalMultiplier();
    }

    public ExerciseController(User user, float met) {
        this.user = user;
        this.met = met;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setMet(float met) {
        this.met = met;
        getCalMultiplier();
    }

    // bmr / 24 * met
    public double getCalMultiplier(){
        if (calMultiplier != 0) return calMultiplier;
        calMultiplier = getUserBMR() / 24 * met;
        return calMultiplier;
    }

    public double getCalBurned(long elapsedTime) {
        return getCalMultiplier() * (elapsedTime / (double) DateUtils.HOUR_IN_MILLIS) ;
    }

    // Mifflin St Jeor equation, from https://doi.org/10.1093%2Fajcn%2F51.2.241
    public double getUserBMR(){
        if (bmr != 0) return bmr;
        int s = user.getSex() ? 5 : -161;
        double age = (System.currentTimeMillis() - user.getBirthday().getTime()) / ((double) DateUtils.YEAR_IN_MILLIS);
        bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age + s;
        return bmr;
    }

    public void uploadExercise(String category, long elapsedTime) {
        String uid = user.getUid();
        if (uid == null) {
            Log.w(TAG, "uploadExercise: couldn't upload exercise because user doesn't exist in the cloud");
            return;
        }
        Date startingTime = new Date(System.currentTimeMillis() - elapsedTime);
        Exercise exercise = new Exercise(getCalBurned(elapsedTime), category, elapsedTime, startingTime);

        FirebaseFirestore.getInstance().collection("users").document(uid).collection("exercises")
                .add(exercise);
    }

    public static void deleteExercise(String uid) {
        UserControllerKt.getCurrentUserDoc().collection("exercises").document(uid).delete();
    }

    public static float getMETofIntensity(int intensity) {
        if (intensity == 1) {
            return 5;
        } else if (intensity == 2) {
            return 10;
        } else {
            return 15;
        }
    }

    public static List<Exercise> getTodayExercises(List<Exercise> exercises){
        Date todayStartTime = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        return exercises.stream().filter(ex -> ex.getStartingTime().after(todayStartTime)).collect(Collectors.toList());
    }

    public static double getTotalCalories(List<Exercise> exercises) {
        return exercises.stream().mapToDouble(Exercise::getCalories).sum();
    }

    public static long getTotalExerciseTime(List<Exercise> exercises) {
        return exercises.stream().mapToLong(Exercise::getElapsedTime).sum();
    }
}
