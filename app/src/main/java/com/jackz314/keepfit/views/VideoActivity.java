package com.jackz314.keepfit.views;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.BackPressingMediaController;
import com.jackz314.keepfit.controllers.VideoController;

public class VideoActivity extends AppCompatActivity{
    private static final String TAG = "VideoActivity";

    private VideoView mVideoView;
    private BackPressingMediaController mMediaController;

    private int mVideoWidth;
    private int mVideoHeight;
    private VideoController mVideoController;

    Button deleteBtn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        Intent intent = getIntent();
        String value = intent.getStringExtra("uri");
        String mediaID = intent.getStringExtra("media");
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

        mVideoView.setOnErrorListener((mp, what, extra) -> {
            Log.d(TAG, "onCreate: couldn't play video with video view, using backup");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(webIntent);
            finish();
            return true;
        });

        mMediaController = new BackPressingMediaController(this, VideoActivity.this);
        mVideoView.setMediaController(mMediaController);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.start();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void openVideoActivity() {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }



}
