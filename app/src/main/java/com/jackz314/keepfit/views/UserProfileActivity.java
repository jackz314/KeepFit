package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "UserProfileActivity";

    private FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean following;
    private UserController ucontrol = new UserController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Will pass other_user object with Intent
        Intent intent = getIntent();
        User otherUser = (User) intent.getSerializableExtra("other");
        //User other_user = new User();

        if (otherUser != null) {
            TextView other_name = findViewById(R.id.user_name_text);
            other_name.setText(otherUser.getName());
            TextView other_email = findViewById(R.id.user_email_text);
            other_email.setText(otherUser.getEmail());
            CircleImageView profImg = findViewById(R.id.user_profile_picture);
            Glide.with(profImg)
                    .load(otherUser.getProfilePic())
                    .fitCenter()
                    .placeholder(R.drawable.ic_account_circle_24)
                    .into(profImg);
        }

        Button follow_btn = findViewById(R.id.followButton);

        db.collection("users").document(curruser.getUid()).collection("following")
                .whereEqualTo("ref", db.collection("users").document(otherUser.getUid()))
                .addSnapshotListener((QuerySnapshot value, FirebaseFirestoreException e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (value.size() == 0) { // not following
                        follow_btn.setText("Follow");
                        following = false;
                    } else {
                        follow_btn.setText("Unfollow");
                        following = true;
                    }
                });


        follow_btn.setOnClickListener(view -> {
            if (following) {
                ucontrol.unfollow(otherUser);
            } else {
                ucontrol.follow(otherUser);
            }
        });
    }
}
