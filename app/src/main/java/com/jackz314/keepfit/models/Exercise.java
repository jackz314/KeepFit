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

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }
}
