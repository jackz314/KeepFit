package com.jackz314.keepfit.helper;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Helper {

    public static Task<Void> createTempAccount(String email, String password) {
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).continueWithTask(task -> {
            if (task.isSuccessful()){
                Map<String, Object> user = new HashMap<>();
                user.put( "biography", "test account");
                user.put("birthday", new Date(2000,1,1));
                user.put("email", email);
                user.put("height", 180);
                user.put("name", "Test Account");
                user.put("profile_pic", "");
                user.put("sex", true);
                user.put("weight", 75);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                return db.collection("users")
                        .document(task.getResult().getUser().getUid())
                        .set(user);   
            } else {
                return FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).continueWithTask(task1 -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put( "biography", "test account");
                    user.put("birthday", new Date(2000,1,1));
                    user.put("email", email);
                    user.put("height", 180);
                    user.put("name", "Test Account");
                    user.put("profile_pic", "");
                    user.put("sex", true);
                    user.put("weight", 75);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    return db.collection("users")
                            .document(task1.getResult().getUser().getUid())
                            .set(user);
                });
            }
        });
        
    }
}
