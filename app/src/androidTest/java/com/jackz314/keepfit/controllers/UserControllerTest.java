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

public class UserControllerTest {
    Boolean followingWorked;
    Boolean followerWorked;
    Boolean unfollowerWorked;
    Boolean unfollowingWorked;

    @Test
    public void follow() throws InterruptedException {
        //create new user document in firebase and get id of document
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
        //create new user object from id string and dummy attr
        String otherUserId = "CPUAeOaPg9W4b18WPT4p";

        User otherUserObject = new User();
        Log.d("follow test","other user: "+ otherUserObject.getUid());

        //call follow function from user controller
        UserController userController = new UserController();
        userController.follow(otherUserId);

        //check if other user in current user following list
        UserControllerKt.getCurrentUserDoc()
                .collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUserId))
                        .addSnapshotListener((value, e) -> {
                            if (value.isEmpty()) {
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

    @Test
    public void unfollow() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
        //create new user object from id string and dummy attr
        String otherUserId = "CPUAeOaPg9W4b18WPT4p";

        //call unfollow function from user controller
        UserController userController = new UserController();
        userController.unfollow(otherUserId);

        //check if other user in current user following list
        UserControllerKt.getCurrentUserDoc()
                .collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUserId))
                .addSnapshotListener((value, e) -> {
                    if (value.isEmpty()) {
                        unfollowingWorked = true;
                    } else {
                        unfollowingWorked = false;
                    }});

        //check if user in other user follower list
        db.collection("users").document(otherUserId).collection("followers")
                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc())
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        return;
                    }
                    if (value.isEmpty()) {
                        unfollowerWorked = true;
                    } else {
                        unfollowerWorked = false;
                    }});
        Thread.sleep(3000);
        assertTrue(unfollowingWorked && unfollowerWorked);
    }
}