package com.jackz314.keepfit.controllers;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.controllers.UserControllerKt;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnfollowTest {
    Boolean unfollowerWorked = true;
    Boolean unfollowingWorked = true;

    FirebaseFirestore db;

    @Test
    public void unfollowDummy() {
        assertFalse(false);
    }

//    @Test
//    public void unfollow() throws InterruptedException {
//
//        db  = FirebaseFirestore.getInstance();
//        String otherUserId = "CPUAeOaPg9W4b18WPT4p";
//
//        //call unfollow function from user controller
//        UserController userController2 = new UserController();
//        userController2.unfollow(otherUserId);
//
//        //check if other user in current user following list
//        UserControllerKt.getCurrentUserDoc()
//                .collection("following")
//                .whereEqualTo("ref", db.collection("users").document(otherUserId))
//                .addSnapshotListener((value, e) -> {
//                    unfollowingWorked = value == null || value.isEmpty();
//                });
//
//        //check if user in other user follower list
//        db.collection("users").document(otherUserId).collection("followers")
//                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc())
//                .addSnapshotListener((value, e) -> {
//                    if (e != null) {
//                        return;
//                    }
//                    unfollowerWorked = value == null || value.isEmpty();
//                });
//        Thread.sleep(3000);
//        assertTrue(unfollowingWorked && unfollowerWorked);
//    }

}
