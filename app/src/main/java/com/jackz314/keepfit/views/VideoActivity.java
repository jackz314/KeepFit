package com.jackz314.keepfit.views;





import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.VideoController;
import com.jackz314.keepfit.models.Media;

public class VideoActivity extends AppCompatActivity{
    private VideoView mVideoView;
    private MediaController mMediaController;

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

        mVideoController = new VideoController(getBaseContext(), mediaID);

        try{
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        catch (NullPointerException e){}

        mVideoController.updateVideoStatus();

        mVideoView = videoView;
        Uri uri = Uri.parse(value);
        mVideoView.setVideoURI(uri);



        MediaController mediaController = new MediaController(this);
        mMediaController = mediaController;
        mVideoView.setMediaController(new MediaController(this){
            public boolean dispatchKeyEvent(KeyEvent event)
            {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                    ((Activity) getContext()).finish();

                return super.dispatchKeyEvent(event);
            }
        });
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
