package com.jackz314.keepfit.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class UserController {
    private final FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final String TAG = "UserController";
    private final Executor procES = Executors.newSingleThreadExecutor();

    Boolean alreadyFollowing;

    private User user;

    public UserController() {
    }

    public void follow(String other_user) {
        //get document references for users
        Map<String, Object> docFollowerData = new HashMap<>();
        Map<String, Object> docFollowingData = new HashMap<>();
        docFollowerData.put("ref", db.collection("users").document(curruser.getUid()));
        docFollowingData.put("ref", db.collection("users").document(other_user));

        db.collection("users")
                .document(curruser.getUid())
                .collection("following").add(docFollowingData);


        db.collection("users")
                .document(other_user)
                .collection("followers")
                .add(docFollowerData);
    }

    public void unfollow(String otherUser) {
        //remove from
        UserControllerKt.getCurrentUserDoc()
                .collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUser)).get()
                .addOnCompleteListener(task -> {
                    // BTW, `getResult()` will throw an exception if the task fails unless you first check for `task.isSuccessful()`
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> removeFollowingList = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : removeFollowingList) {
                            doc.getReference().delete();
                        }
                    }
                });

        db.collection("users")
                .document(otherUser)
                .collection("followers")
                .whereEqualTo("ref", UserControllerKt.getCurrentUserDoc()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> removeFollowerList = task.getResult().getDocuments();
                        for (DocumentSnapshot doc : removeFollowerList) {
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
