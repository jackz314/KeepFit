package com.jackz314.keepfit.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.widget.Toast;

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
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityUserProfileBinding;
import com.jackz314.keepfit.databinding.FragmentFeedBinding;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "Follow Activity" ;
    FirebaseUser curruser = FirebaseAuth.getInstance().getCurrentUser();
    private ActivityUserProfileBinding b;
    private FirebaseFirestore db;
    private FollowRecyclerAdapter followerRecyclerAdapter;
    boolean following;
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
        User otherUser = (User)intent.getSerializableExtra(GlobalConstants.USER_PROFILE);
        //User other_user = new User();
        db = FirebaseFirestore.getInstance();
        followerRecyclerAdapter = new FollowRecyclerAdapter(this, mList);
        livestreamController = new LivestreamController(this);
        b = ActivityUserProfileBinding.inflate(getLayoutInflater());
        View v =b.getRoot();
        setContentView(v);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        b.exerciseLogRecycler.setLayoutManager(layoutManager);



        if (!mList.isEmpty() && b.exerciseLogRecycler.getVisibility() == View.VISIBLE) {
            b.exerciseLogRecycler.setVisibility(View.GONE);
            b.noVideosText.setVisibility(View.VISIBLE);
        }

//        if (other_user != null) {

        b.userNameText.setText(otherUser.getName());
        b.userEmailText.setText(otherUser.getEmail());
        String bio = otherUser.getBiography();
        if(bio.isEmpty()){
            b.biography.setText("Hello!");
        }
        else {
            b.biography.setText("Bio: "+otherUser.getBiography());
        }
        CircleImageView prof_img = findViewById(R.id.user_profile_picture);
        Glide.with(b.getRoot())
                .load(otherUser.getProfilePic())
                .fitCenter()
                .placeholder(R.drawable.ic_account_circle_24)
                .into(b.userProfilePicture);

        b.userNameText.setCompoundDrawablesWithIntrinsicBounds(0,0, otherUser.getSex() ? R.drawable.ic_baseline_male_24 : R.drawable.ic_baseline_female_24,0);
        b.userHeightText.setText(Utils.centimeterToFeet(otherUser.getHeight()));
        b.userWeightText.setText((int)(otherUser.getWeight() *  2.205) + " lbs");
        b.userBirthdayText.setText(DateUtils.formatDateTime(getContext(), otherUser.getBirthday().getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

        Button followBtn = findViewById(R.id.followButton);
        if (otherUser.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            followBtn.setVisibility(View.GONE);
        }
        UserControllerKt.getCurrentUserDoc().collection("following").whereEqualTo("ref", db.collection("users").document(otherUser.getUid()))
                .addSnapshotListener((value, e) -> {
                    if (e != null || value == null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    if (value.isEmpty()) {
                        followBtn.setText(String.valueOf('+'));
                        following = false;
                    } else {
                        followBtn.setText(String.valueOf('-'));
                        following = true;
                    }});

        //Fills list with videos that belong to the user
        populateList(otherUser);
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



        followButtonChange(followBtn, otherUser);
        followBtn.setOnClickListener(view -> {
            UserController ucontrol = new UserController();
            if (following) {
                ucontrol.unfollow(otherUser.getUid());
            } else {
                ucontrol.follow(otherUser.getUid());
            }
        });
    }



    public void followButtonChange(Button follow_btn, User other_user) {
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

    public Context getContext() {
        return getApplication().getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        if(registration!=null) registration.remove();
        super.onDestroy();
    }
}