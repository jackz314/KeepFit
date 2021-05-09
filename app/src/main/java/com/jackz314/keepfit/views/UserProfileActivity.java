package com.jackz314.keepfit.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityUserProfileBinding;
import com.jackz314.keepfit.models.Exercise;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.other.ExerciseRecyclerAdapter;
import com.jackz314.keepfit.views.other.FollowRecyclerAdapter;

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
    private FollowRecyclerAdapter videoFollowerRecyclerAdapter;

    private final List<Exercise> exerciseList = new ArrayList<>();
    private ExerciseRecyclerAdapter exerciseRecyclerAdapter;

    boolean following;
    private final List<Media> mList = new ArrayList<>();
    private final Executor procES = Executors.newSingleThreadExecutor();
    private ListenerRegistration registration;
    private LivestreamController livestreamController;
    private ListenerRegistration lr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        User otherUser = (User)intent.getSerializableExtra(GlobalConstants.USER_PROFILE);

        db = FirebaseFirestore.getInstance();
        videoFollowerRecyclerAdapter = new FollowRecyclerAdapter(this, mList);
        livestreamController = new LivestreamController(this);
        b = ActivityUserProfileBinding.inflate(getLayoutInflater());
        View v =b.getRoot();
        setContentView(v);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        b.videoLogRecycler.setLayoutManager(layoutManager);

        if (!mList.isEmpty() && b.videoLogRecycler.getVisibility() == View.VISIBLE) {
            b.videoLogRecycler.setVisibility(View.GONE);
            b.noVideosText.setVisibility(View.VISIBLE);
        }

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
        if (otherUser.getBirthday() == null) {
            b.userBirthdayText.setText(" ¯\\_(ツ)_/¯");
        } else {
            b.userBirthdayText.setText(DateUtils.formatDateTime(getContext(), otherUser.getBirthday().getTime(),
                    DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

        }


        String otherUserUid = otherUser.getUid();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (otherUserUid != null && currentUser != null) {
            Button followBtn = findViewById(R.id.followButton);
            if (otherUserUid.equals(currentUser.getUid())){
                followBtn.setVisibility(View.GONE);
            }
            lr = UserControllerKt.getCurrentUserDoc().collection("following").whereEqualTo("ref", db.collection("users").document(otherUserUid))
                    .addSnapshotListener((value, e) -> {
                        if (e != null || value == null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        if (value.isEmpty()) {
                            followBtn.setText(String.valueOf('+'));
                            following = false;
                            Log.d(TAG, "onCreate: follow: false");
                        } else {
                            followBtn.setText(String.valueOf('-'));
                            following = true;
                            Log.d(TAG, "onCreate: follow: true");
                        }});

            //Fills list with videos that belong to the user
            populateList(otherUser);
            videoFollowerRecyclerAdapter.setClickListener((view, position) -> {

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

            });


            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(b.videoLogRecycler.getContext(),
                    layoutManager.getOrientation());
            b.videoLogRecycler.addItemDecoration(dividerItemDecoration);
            b.videoLogRecycler.setAdapter(videoFollowerRecyclerAdapter);

            // exercise stuff
            exerciseRecyclerAdapter = new ExerciseRecyclerAdapter(getContext(), exerciseList);
            exerciseRecyclerAdapter.setClickListener((view, position) -> {
                Exercise exercise = exerciseList.get(position);
                Intent eIntent = new Intent(this, ViewExerciseActivity.class); //unsure about "this"
                eIntent.putExtra(GlobalConstants.EXERCISE_OBJ, exercise);
                startActivity(eIntent);
            });
            b.exerciseLogRecycler.setAdapter(exerciseRecyclerAdapter);
            LinearLayoutManager exerciseLayoutManager = new LinearLayoutManager(getContext());
            b.exerciseLogRecycler.setLayoutManager(exerciseLayoutManager);
            b.exerciseLogRecycler.setNestedScrollingEnabled(false);
            DividerItemDecoration exerciseDividerItemDecoration = new DividerItemDecoration(b.exerciseLogRecycler.getContext(),
                    layoutManager.getOrientation());
            b.exerciseLogRecycler.addItemDecoration(exerciseDividerItemDecoration);

            db.collection("users").document(otherUserUid).collection("exercises").orderBy("starting_time", Query.Direction.DESCENDING).addSnapshotListener(((value, error) -> {
                        if (error != null || value == null) {
                            Log.w(TAG, "Listen failed.", error);
                            return;
                        }
                        exerciseList.clear();
                        exerciseList.addAll(value.toObjects(Exercise.class));
                        exerciseRecyclerAdapter.notifyDataSetChanged();

                        if (exerciseList.isEmpty()) {
                            b.exerciseLogRecycler.setVisibility(View.GONE);
                            b.noExercisesText.setVisibility(View.VISIBLE);
                        } else {
                            b.exerciseLogRecycler.setVisibility(View.VISIBLE);
                            b.noExercisesText.setVisibility(View.GONE);
                        }
            }));
            //app:layout_constraintVertical_bias="0.461"


            followBtn.setOnClickListener(view -> {
                UserController ucontrol = new UserController();
                if (following) {
                    Log.d(TAG, "onCreate: unfollowing");
                    ucontrol.unfollow(otherUserUid);
                } else {
                    Log.d(TAG, "onCreate: following");
                    ucontrol.follow(otherUserUid);
                }
            });
        }
    }

    public void populateList(User other){

        db = FirebaseFirestore.getInstance();
        registration = db.collection("media")
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

                        this.runOnUiThread(() -> {
                            if (b != null) {
                                if (!mList.isEmpty()){
                                    b.noVideosText.setVisibility(View.GONE);
                                    b.videoLogRecycler.setVisibility(View.VISIBLE);
                                } else {
                                    b.noVideosText.setVisibility(View.VISIBLE);
                                    b.videoLogRecycler.setVisibility(View.GONE);
                                }
                            }

                            videoFollowerRecyclerAdapter.notifyDataSetChanged();
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
        if(lr != null) lr.remove();
        if(registration!=null) registration.remove();
        super.onDestroy();
    }
}