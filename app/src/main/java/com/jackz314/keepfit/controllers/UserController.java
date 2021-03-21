package com.jackz314.keepfit.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.User;


public class UserController {
    FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

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

}
