package com.jackz314.keepfit.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;

public class Exercise implements Serializable {
    @DocumentId
    public String uid;
    private double calories;
    private String category;
    private long elapsedTime;
    private Date startingTime;

    public Exercise(){}

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

    @PropertyName("elapsed_time")
    public long getElapsedTime() {
        return elapsedTime;
    }

    @PropertyName("elapsed_time")
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    @PropertyName("starting_time")
    public Date getStartingTime() {
        return startingTime;
    }

    @PropertyName("starting_time")
    public void setStartingTime(Date startingTime) {
        this.startingTime = startingTime;
    }
}
