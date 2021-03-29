package com.jackz314.keepfit.controllers;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.User;

import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class FollowTest {
    Boolean followingWorked;
    Boolean followerWorked;


    FirebaseFirestore db;

    @Test
    public void follow() throws InterruptedException {

        db = FirebaseFirestore.getInstance();
        String otherUserId = "CPUAeOaPg9W4b18WPT4p";

        //call follow function from user controller
        UserController userController = new UserController();
        userController.follow(otherUserId);

        //check if other user in current user following list
        UserControllerKt.getCurrentUserDoc()
                .collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUserId))
                        .addSnapshotListener((value, e) -> {
                            if (value == null || value.isEmpty()) {
                                followingWorked = false;
                            } else {
                                followingWorked = true;
                            }});

        //check if user in other user follower list
        db.collection("users").document(otherUserId).collection("followers")
                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc())
                        .addSnapshotListener((value, e) -> {
                            if (e != null || value == null) {
                                return;
                            }
                            if (value.isEmpty()) {
                                followerWorked = false;
                            } else {
                                followerWorked = true;
                            }});
        Thread.sleep(3000);
        assertTrue(followingWorked && followerWorked);
    }


}