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
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //get document references for users
        Map<String, Object> docFollowerData = new HashMap<>();
        Map<String, Object> docFollowingData = new HashMap<>();
        docFollowerData.put("ref", db.collection("users").document(curruser.getUid()));
        docFollowingData.put("ref", db.collection("users").document(other_user.getUid()));

        db.collection("users")
                .document(curruser.getUid())
                .collection("following").add(docFollowingData);


        db.collection("users")
                .document(other_user.getUid())
                .collection("followers")
                .add(docFollowerData);

    }

    public void unfollow(User otherUser) {
        //remove from
        UserControllerKt.getCurrentUserDoc()
                .collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUser.getUid())).get()
                .addOnCompleteListener(task -> {
                    // BTW, `getResult()` will throw an exception if the task fails unless you first check for `task.isSuccessful()`
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> removeFollowingList = task.getResult().getDocuments();
                        for (DocumentSnapshot doc: removeFollowingList) {
                            doc.getReference().delete();
                        }
                    }
                });

        db.collection("users")
                .document(otherUser.getUid())
                .collection("followers")
                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc()).get()
                .addOnCompleteListener(task -> {
                    // BTW, `getResult()` will throw an exception if the task fails unless you first check for `task.isSuccessful()`
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> removeFollowerList = task.getResult().getDocuments();
                        for (DocumentSnapshot doc: removeFollowerList) {
                            doc.getReference().delete();
                        }
                    }
                });

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
