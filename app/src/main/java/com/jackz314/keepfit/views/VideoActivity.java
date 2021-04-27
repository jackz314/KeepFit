package com.jackz314.keepfit.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.LivestreamController;
import com.jackz314.keepfit.models.Comment;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.other.BackPressingMediaController;
import com.jackz314.keepfit.views.other.BackPressingMediaController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.controllers.VideoController;
import com.jackz314.keepfit.models.Media;
import com.jackz314.keepfit.views.other.CommentRecyclerAdapter;
import com.jackz314.keepfit.views.other.FeedRecyclerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.google.firebase.Timestamp.now;

public class VideoActivity extends AppCompatActivity{
    private static final String TAG = "VideoActivity";

    private VideoView mVideoView;
    private BackPressingMediaController mMediaController;

    private CommentRecyclerAdapter commentRecyclerAdapter;

    private VideoController mVideoController;
    private FirebaseUser ub;

    private RecyclerView commentRecycler;

    private final ArrayList<Comment> commentList = new ArrayList<>();

    private final Executor procES = Executors.newSingleThreadExecutor();
    private LinearLayoutManager linearLayoutManager;

    Button uploadBtn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    EditText editText;
    TextView emptyText;
    TextView offCommentText;
    CircleImageView prof_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        commentRecycler = findViewById(R.id.comment_recycler);
        commentRecycler.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentRecycler.setLayoutManager(linearLayoutManager);

        uploadBtn = findViewById(R.id.comment_upload_btn);
        editText = findViewById(R.id.comment_text_input);
        emptyText = findViewById(R.id.empty_comment_text);
        prof_img = findViewById(R.id.comment_profile_pic);

        offCommentText = findViewById(R.id.unavailable_comment_text);
        offCommentText.setVisibility(INVISIBLE);
        emptyText.setVisibility(INVISIBLE);
        setUploadCommentVisibility(INVISIBLE);

        Intent intent = getIntent();
        String value = intent.getStringExtra("uri");
        String mediaID = intent.getStringExtra("media");
        addToHistory(mediaID);
        VideoView videoView = findViewById(R.id.video_view);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mVideoController = new VideoController(getBaseContext(), mediaID);


        try{
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        catch (NullPointerException ignored){}

        mVideoController.updateVideoStatus();

        mVideoView = videoView;
        Uri uri = Uri.parse(value);
        mVideoView.setVideoURI(uri);

        mMediaController = new BackPressingMediaController(VideoActivity.this, VideoActivity.this);
        mVideoView.setMediaController(mMediaController);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.start();


        mVideoView.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "onCreate: couldn't play video with video view, using backup");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(webIntent);
            finish();
            return true;
        });

        ub = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(ub.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Glide.with(VideoActivity.this)
                                .load(document.getString("profile_pic"))
                                .fitCenter()
                                .placeholder(R.drawable.ic_account_circle_24)
                                .into(prof_img);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        DocumentReference docRef = db.collection("media").document(mediaID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.getBoolean("is_commentable")){
                            loadComments(mediaID);
                            emptyText.setVisibility(INVISIBLE);
                            setUploadCommentVisibility(VISIBLE);
                        }
                        else{
                            emptyText.setVisibility(INVISIBLE);
                            offCommentText.setVisibility(VISIBLE);
                            setUploadCommentVisibility(INVISIBLE);
                        }
                    } else {
                        Log.d(TAG, "No Comment");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });






        uploadBtn.setOnClickListener(view -> {
            if(editText.getText().toString().equals("")){
                Toast.makeText(getBaseContext(),"Empty Comment!", Toast.LENGTH_LONG).show();
            }
            else{
                uploadCommentFirebase(mediaID);
            }
        });

    }

    public void loadComments(String mediaID){
        if(!commentList.isEmpty())
            commentList.clear();


        db.collection("comments").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for(DocumentSnapshot qs: task.getResult()){
                    if(qs.getString("media").equals(mediaID)){

                        Comment comment = new Comment(qs.getId(),qs.getString("text"),qs.getString("user"),qs.getString("media"), qs.getDate("upload_time"));
                        commentList.add(comment);
                    }
                }

                commentList.sort(CommentDateComparator);
                commentRecyclerAdapter = new CommentRecyclerAdapter(VideoActivity.this, commentList, VideoActivity.this);
                commentRecycler.setAdapter(commentRecyclerAdapter);
                if(!commentList.isEmpty()){
                    emptyText.setVisibility(INVISIBLE);
                }
                else{
                    emptyText.setVisibility(VISIBLE);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VideoActivity.this, "Problem ---l---", Toast.LENGTH_SHORT).show();
                Log.v("---l---", e.getMessage());
            }
        });
    }

    public static Comparator<Comment> CommentDateComparator = new Comparator<Comment>() {

        public int compare(Comment c1, Comment c2) {
            Date c1date = c1.getUploadTime();
            Date c2date = c2.getUploadTime();

            //return c1date.compareTo(c2date);

            return c2date.compareTo(c1date);
        }};


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void openVideoActivity() {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    public void addToHistory(String mediaID){
        Map<String, Object> obj = new HashMap<>();
        obj.put("watched", new Timestamp(new Date()));

        UserControllerKt.getCurrentUserDoc().collection("history").document(mediaID).set(obj);
    }

    private void uploadCommentFirebase(String mediaID) {
        final ProgressDialog progressDialog = new ProgressDialog(this);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference uidRef = rootRef.collection("users").document(uid);

        Timestamp timestamp = now();

        Map<String, Object> comment = new HashMap<>();
        comment.put("user", uid);
        comment.put("media", mediaID);
        comment.put("text", editText.getText().toString());
        comment.put("upload_time", timestamp);

        DocumentReference commentRef = db.collection("comments").document();
        commentRef.set(comment).addOnCompleteListener(task -> {
            Toast.makeText(getApplicationContext(),"Comment Uploaded!", Toast.LENGTH_LONG).show();
        });

        loadComments(mediaID);
    }

    public void deleteComment(String uid , String cid, String mediaID){
        String curruserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(curruserID.equals(uid)){
            db.collection("comments").document(cid)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(), "Comment Deleted!", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
        }
        else {

            AlertDialog alertDialog = new AlertDialog.Builder(VideoActivity.this)
                    .setMessage("Cannot delete others comments!")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                    {@Override
                        public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }}).create();
            alertDialog.show();
        }
        loadComments(mediaID);
    }

    private void setUploadCommentVisibility(int visibility){
        editText.setVisibility(visibility);
        uploadBtn.setVisibility(visibility);
        prof_img.setVisibility(visibility);
    }

    @Override
    protected void onResume() {
        mVideoView.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mVideoView.pause();
        mMediaController.hide();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mVideoView.stopPlayback();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        mMediaController.hide();
        super.onStop();

    }

}
