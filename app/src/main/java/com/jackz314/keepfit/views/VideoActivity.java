package com.jackz314.keepfit.views;





import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;

public class VideoActivity extends AppCompatActivity{
    private VideoView mVideoView;
    private MediaController mMediaController;

    private int mVideoWidth;
    private int mVideoHeight;

    Button deleteBtn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);



        Intent intent = getIntent();
        String value = intent.getStringExtra("uri");
        String creator = intent.getStringExtra("creator");
        VideoView videoView = findViewById(R.id.video_view);

        try{
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        catch (NullPointerException e){}

        mVideoView = videoView;
        Uri uri = Uri.parse(value);
        mVideoView.setVideoURI(uri);

        MediaController mediaController = new MediaController(this);
        mMediaController = mediaController;
        mVideoView.setMediaController(mMediaController);
        mMediaController.setAnchorView(mVideoView);
        mVideoView.start();

        if(mediaController.isPressed()){
            this.getSupportActionBar().show();
        }
        else{
            this.getSupportActionBar().hide();
        }

//        deleteBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
//                AlertDialog diaBox = AskOption();
//                diaBox.show();
//            }
//        });
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to DELETE this Video?")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteVideo();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;
    }

    private void deleteVideo() {


       // StorageReference reference = storageReference
       // db.collection("media").
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
