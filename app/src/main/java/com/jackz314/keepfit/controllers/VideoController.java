package com.jackz314.keepfit.controllers;

import android.content.Context;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.Media;

public class VideoController {

    private final Context context;
    private final FirebaseFirestore db;
    private final String mID;

    private Media livestream;

    public VideoController(Context context, String id) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mID = id;
    }

    static public void likeVideo(String mediaID) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("media").document(mediaID).update("likes", FieldValue.increment(1));
    }

    static public void unlikeVideo(String mediaID) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("media").document(mediaID).update("likes", FieldValue.increment(-1));
    }

    static public void dislikeVideo(String mediaID) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("media").document(mediaID).update("dislikes", FieldValue.increment(1));
    }

    static public void undislikeVideo(String mediaID) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("media").document(mediaID).update("dislikes", FieldValue.increment(-1));
    }

    public void updateVideoStatus() {
        db.collection("media").document(mID).update("view_count", FieldValue.increment(1));
    }
}

