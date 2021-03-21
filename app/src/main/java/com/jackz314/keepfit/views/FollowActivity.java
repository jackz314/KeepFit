package com.jackz314.keepfit.views;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.models.User;

public class FollowActivity extends AppCompatActivity {
    FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Boolean following;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Will pass other user object with Intent
//        Intent intent = getIntent();
//        User other_user = intent.getSerializableExtra("other");
        User other_user = new User();


        Button follow_btn = findViewById(R.id.followButton);
        //if following user, then button says unfollow, if not following user, button says follow
        DocumentReference doc = db.collection("users").document(curruser.getUid()).collection("following").document(other_user.getUid());
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        follow_btn.setText("Unfollow");
                        following = true;
                    } else {
                        follow_btn.setText("Follow");
                        following = false;
                    }
                }
            }
        });


        follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserController ucontrol = new UserController();
                if (following) {
                    ucontrol.Unfollow(other_user);
                } else {
                    ucontrol.Follow(other_user);
                }
            }
        });
    }
}