package com.jackz314.keepfit.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityFollowBinding;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "Follow Activity" ;
    FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    private ActivityFollowBinding b;
    private FirebaseFirestore db;
    private FollowRecyclerAdapter followerRecyclerAdapter;
    Boolean following;
    private List<Media> mList = new ArrayList<>();
    private Executor procES = Executors.newSingleThreadExecutor();
    private ListenerRegistration registration;
    private LivestreamController livestreamController;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        //Will pass other_user object with Intent

        Intent intent = getIntent();
        User other_user = (User)intent.getSerializableExtra("other");
        //User other_user = new User();
        db = FirebaseFirestore.getInstance();
        followerRecyclerAdapter = new FollowRecyclerAdapter(this, mList);
        livestreamController = new LivestreamController(this);
        b = ActivityFollowBinding.inflate(getLayoutInflater());
        View v =b.getRoot();
        setContentView(v);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        b.exerciseLogRecycler.setLayoutManager(layoutManager);

        if (!mList.isEmpty() && b.exerciseLogRecycler.getVisibility() == View.VISIBLE) {
            b.exerciseLogRecycler.setVisibility(View.GONE);
            b.noVideosText.setVisibility(View.VISIBLE);
        }

//        if (other_user != null) {

        b.userNameText.setText(other_user.getName());
        b.userEmailText.setText(other_user.getEmail());
        String bio = other_user.getBiography();
        if(bio.isEmpty()){
            b.biography.setText("Hello!");
        }
        else {
            b.biography.setText("Bio: "+other_user.getBiography());
        }
        CircleImageView prof_img = findViewById(R.id.user_profile_picture);
        Glide.with(b.getRoot())
                .load(other_user.getProfilePic())
                .fitCenter()
                .placeholder(R.drawable.ic_account_circle_24)
                .into(b.userProfilePicture);
        //fix
        b.userNameText.setCompoundDrawablesWithIntrinsicBounds(0,0, other_user.getSex() ? R.drawable.ic_baseline_male_24 : R.drawable.ic_baseline_female_24,0);
        b.userHeightText.setText(Utils.centimeterToFeet(other_user.getHeight()));
        b.userWeightText.setText((int)(other_user.getWeight() *  2.205) + " lbs");


        //Fills list with videos that belong to the user
        populateList(other_user);
        followerRecyclerAdapter.setClickListener((view, position) -> {
            // TODO: 3/6/21 replace with activity intent

            Media media = mList.get(position);
            if(media.isLivestream()) {
                livestreamController.setLivestream(media);
                livestreamController.joinLivestream();
            }

            else{
                Intent videoPlay = new Intent(this, VideoActivity.class);

                //String videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.sample;
                videoPlay.putExtra("uri", media.getLink());
                videoPlay.putExtra("media", media.getUid());
                startActivity(videoPlay);
            }

            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(mediaList.get(position).getLink())));
        });


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.exerciseLogRecycler.getContext(),
                layoutManager.getOrientation());
        b.exerciseLogRecycler.addItemDecoration(dividerItemDecoration);
        b.exerciseLogRecycler.setAdapter(followerRecyclerAdapter);
//        }


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
                    ucontrol.unfollow(other_user);
                } else {
                    ucontrol.follow(other_user);
                }
            }
        });
    }

    public void populateList(User other){

        db = FirebaseFirestore.getInstance();
        registration = db.collection("media")
//                .whereEqualTo("state", "CA")
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    procES.execute(() -> {
                        mList.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : value) {
                            Media media = new Media(queryDocumentSnapshot);
                            Log.d(TAG,"getCreatorRef.getId(): "+media.getCreatorRef().getId());
                            if((media.getCreatorRef().getId()).equals(other.getUid())) {
                                mList.add(new Media(queryDocumentSnapshot));
                            }
                        }

                        // TODO: 3/6/21 change to item based notify (notifyItemRemoved)
                        this.runOnUiThread(() -> {

                            if (b != null) {
                                if (!mList.isEmpty()){
                                    b.noVideosText.setVisibility(View.GONE);
                                    b.exerciseLogRecycler.setVisibility(View.VISIBLE);
                                } else {
                                    b.noVideosText.setVisibility(View.VISIBLE);
                                    b.exerciseLogRecycler.setVisibility(View.GONE);
                                }
                            }

                            followerRecyclerAdapter.notifyDataSetChanged();
                        });
                        Log.d(TAG, "media collection update: " + mList);
                    });
                });
    }
    public void closeProfile(View view){
        finish();
    }

    @Override
    protected void onDestroy() {
        if(registration!=null) registration.remove();
        super.onDestroy();
    }
}