package com.jackz314.keepfit.controllers;

import android.content.Context;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.Media;

import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.ZoomSDK;

public class VideoController {

    private final Context context;
    private final FirebaseFirestore db;
    private String mID;

    private Media livestream;

    public VideoController(Context context, String id) {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        mID = id;
    }

    public void updateVideoStatus(){
        db.collection("media").document(mID).update("view_count", FieldValue.increment(1));
    }
}
