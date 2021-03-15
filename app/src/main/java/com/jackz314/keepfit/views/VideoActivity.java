package com.jackz314.keepfit.views;





import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;

public class VideoActivity extends AppCompatActivity{
    private VideoView mVideoView;
    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);

        Intent intent = getIntent();
        String value = intent.getStringExtra("uri");
        VideoView videoView = findViewById(R.id.video_view);

        mVideoView = videoView;
        Uri uri = Uri.parse(value);
        mVideoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        mMediaController = mediaController;
        mVideoView.setMediaController(mMediaController);
        mMediaController.setAnchorView(mVideoView);
    }

    public void openVideoActivity() {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

}
