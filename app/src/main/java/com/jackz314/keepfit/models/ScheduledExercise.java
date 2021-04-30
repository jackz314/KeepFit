package com.jackz314.keepfit.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ScheduledExercise implements Serializable {
    @DocumentId
    public String uid;
    private Date time;
    private String category = "";
    private int intensity = 1;

    public ScheduledExercise() {
    }

    public ScheduledExercise(ScheduledExercise exercise) {
        uid = exercise.uid;
        time = new Date(exercise.time.getTime());
        category = exercise.category;
        intensity = exercise.intensity;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduledExercise that = (ScheduledExercise) o;
        return intensity == that.intensity &&
                Objects.equals(time, that.time) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, category, intensity);
    }

    @Override
    public String toString() {
        return "ScheduledExercise{" +
                "uid='" + uid + '\'' +
                ", time=" + time +
                ", category='" + category + '\'' +
                ", intensity=" + intensity +
                '}';
    }
}
