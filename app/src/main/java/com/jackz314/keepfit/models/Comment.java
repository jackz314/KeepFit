package com.jackz314.keepfit.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class Comment implements Serializable {

    private static final String TAG = "Comment";

    private String cid;
    private String text;
    private Date upload_time;
    private String uid;
    private String mid;
    private String username;

    public Comment() {
        text = "";
    }

    public Comment(DocumentSnapshot doc) {
        if (!doc.exists()) return;
        cid = doc.getId();
        text = doc.getString("text");
        uid = doc.getString("user");
        mid = doc.getString("media");
        upload_time = doc.getDate("upload_time");
    }

    public Comment(String cid, String text, String uid, String mid, Date uploadTime) {
        this.cid = cid;
        this.text = text;
        this.uid = uid;
        this.mid = mid;
        this.upload_time = uploadTime;
    }

    public static Comment populateFromCid(String cid) {
        try {
            return new Comment(Tasks.await(FirebaseFirestore.getInstance().collection("comment").document(cid).get()));
        } catch (ExecutionException | InterruptedException e) {
            Log.e(TAG, "populateFromUid: error getting user from cid", e);
        }
        return null;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public Date getUploadTime() {
        return upload_time;
    }

    public void setUploadTime(Date upload_time) {
        this.upload_time = upload_time;
    }

    @NotNull
    @Override
    public String toString() {
        return "Comment{" +
                "cid='" + cid + '\'' +
                ", text='" + text + '\'' +
                ", user='" + uid + '\'' +
                ", media='" + mid + '\'' +
                ", upload_time=" + upload_time +
                '}';
    }

    @NonNull
    public Comment copy() {
        return new Comment(cid, text, uid, mid, upload_time);
    }
}
