package com.jackz314.keepfit.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Exercise {
    private double calories;
    private String category;
    @PropertyName("elapsed_time")
    private long elapsedTime;
    @PropertyName("starting_time")
    private Date startingTime;

    public Exercise(double calories, String category, long elapsedTime, Date startingTime) {
        this.calories = calories;
        this.category = category;
        this.elapsedTime = elapsedTime;
        this.startingTime = startingTime;
    }
}
