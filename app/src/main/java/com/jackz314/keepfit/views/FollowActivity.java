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
        //Will pass other_user object with Intent
        Intent intent = getIntent();
        User other_user = (User)intent.getSerializableExtra("other");
        //User other_user = new User();


        if (other_user != null) {
            TextView other_name = findViewById(R.id.user_name_text);
            other_name.setText(other_user.getName());
            TextView other_email = findViewById(R.id.user_email_text);
            other_email.setText(other_user.getEmail());
            CircleImageView prof_img = findViewById(R.id.user_profile_picture);
            Uri prof_pic = Uri.parse(other_user.getProfilePic());
            prof_img.setImageURI(prof_pic);
        }

        Button follow_btn = findViewById(R.id.followButton);




        db.collection("users").document(curruser.getUid()).collection("following").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot findFollowingCollection = task.getResult();
                            if (findFollowingCollection.isEmpty()) {
                                //if it's empty then user is not following anyone => don't want to work with null values
                                follow_btn.setText("Follow");
                                following = false;
                            } else {
                                db.collection("users").document(curruser.getUid()).collection("following")
                                        .whereEqualTo("ref", db.collection("users").document(other_user.getUid())).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot isFollowing = task.getResult();
                                                    if (isFollowing.isEmpty()) {
                                                        follow_btn.setText("Follow");
                                                        following = false;
                                                    } else {
                                                        follow_btn.setText("Unfollow");
                                                        following = true;
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });


        follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserController ucontrol = new UserController();
                if (following) {
                    ucontrol.unfollow(other_user);
                } else {
                    ucontrol.follow(other_user);
                }
                finish();
                startActivity(getIntent());
            }
        });
    }
}
