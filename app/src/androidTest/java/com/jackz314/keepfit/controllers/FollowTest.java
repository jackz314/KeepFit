package com.jackz314.keepfit.controllers;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
                    followingWorked = value != null && !value.isEmpty();
                });

        //check if user in other user follower list
        db.collection("users").document(otherUserId).collection("followers")
                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc())
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        return;
                    }
                    followerWorked = !value.isEmpty();
                });
        Thread.sleep(3000);
        assertTrue(followingWorked && followerWorked);
    }


}