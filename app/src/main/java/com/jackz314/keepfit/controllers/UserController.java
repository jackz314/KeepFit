package com.jackz314.keepfit.controllers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.User;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class UserController {
    private FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String TAG = "UserController";
    private Executor procES = Executors.newSingleThreadExecutor();

    private User user;
    public UserController() {
    }

    public void follow(User other_user) {
        //add other user to following list
        db.collection("users").document(curruser.getUid()).collection("following").add(db.collection("users").document(other_user.getUid()));
        //add curruser to other user's followers list
        db.collection("users").document(other_user.getUid()).collection("followers").add(db.collection("users").document(curruser.getUid()));

    }

    public void unfollow(User other_user) {
        DocumentReference doc = db.collection("users").document(curruser.getUid()).collection("following").document(other_user.getUid());
        //remove other user from following list
        doc.delete();
        //remove curruser from other user's followers list
        db.collection("users").document(other_user.getUid()).collection("followers").document(curruser.getUid()).delete();

    }

    public User getLocalUser() {
       return user;
    }

    public User getOtherUser(String UserId) {
        DocumentSnapshot otherUserData = db.collection("users")
                .document(UserId).get().getResult();
        return new User(otherUserData);
    }
}
