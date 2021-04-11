package com.jackz314.keepfit.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.models.Exercise;
import com.jackz314.keepfit.models.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
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

    // get corrected MET value
    // https://dx.doi.org/10.1073%2Fpnas.4.12.370
    public double getCMet() {
        double rmr;
        if (user.getSex()) { // male
            rmr = 66.4730 + 5.0033 * user.getHeight() + 13.7516 * user.getWeight() - 6.7550 * getAgeInYears();
        } else {
            rmr = 655.0955 + 1.8496 * user.getHeight() + 9.5634 * user.getWeight() - 4.6756 * getAgeInYears();
        }
        return met * 3.5 / rmr;
    }

    // bmr / 24 * met
    public double getCalMultiplier() {
        if (calMultiplier != 0) return calMultiplier;
        calMultiplier = getUserBMR() / 24 * getCMet();
        return calMultiplier;
    }

    public double getCalBurned(long elapsedTime) {
        return getCalMultiplier() * (elapsedTime / (double) DateUtils.HOUR_IN_MILLIS);
    }

    // Mifflin St Jeor equation, from https://doi.org/10.1093%2Fajcn%2F51.2.241
    public double getUserBMR() {
        if (bmr != 0) return bmr;
        int s = user.getSex() ? 5 : -161;
        double age = getAgeInYears();
        bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age + s;
        return bmr;
    }

    private double getAgeInYears() {
        return (System.currentTimeMillis() - user.getBirthday().getTime()) / ((double) DateUtils.YEAR_IN_MILLIS);
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

    public static void setMostRecentExercise(Context context, String exercise) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(GlobalConstants.RECENT_EXERCISE_KEY, exercise).apply();
    }

    public static String getMostRecentExercise(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(GlobalConstants.RECENT_EXERCISE_KEY, null);
    }
}
