package com.jackz314.keepfit.models;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class ScheduledExercise {
    @DocumentId
    public String uid;
    private Date time;
    private String category;
    private int intensity;

    public ScheduledExercise() {
    }

    public ScheduledExercise(Date time, String category, int intensity) {
        this.time = time;
        this.category = category;
        this.intensity = intensity;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }
}
