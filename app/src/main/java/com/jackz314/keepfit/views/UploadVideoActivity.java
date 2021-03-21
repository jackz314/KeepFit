package com.jackz314.keepfit.views;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.UploadTask;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.controllers.VideoController;
import com.jackz314.keepfit.models.Media;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.Timestamp.now;


public class UploadVideoActivity extends AppCompatActivity {

    private static final String TAG = "UploadVideoActivity";

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    EditText editText;
    EditText titleText;
    Button btn;

    StorageReference storageReference;
    DatabaseReference databaseReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        editText = findViewById(R.id.choose_file_to_upload);
        titleText = findViewById(R.id.video_title_input);
        btn = findViewById(R.id.btn_video_upload);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        btn.setEnabled(false);
        editText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                selectVideo();
            }
        });

    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"VIDEO FILE SELECT"),12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==12 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            btn.setEnabled(true);

            String path = data.getDataString();
            String filename = path.substring(path.lastIndexOf("/")+1);
            editText.setText(filename);

            //editText.setText(data.getDataString().);
            btn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    String link = new String();
                    uploadVideoFirebase(data.getData());

                }

            });
            ;
        }
    }

    private void uploadVideoFirebase(Uri data) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is loading...");
        progressDialog.show();

        StorageReference reference = storageReference.child("upload"+System.currentTimeMillis()+".mp4");

        reference.putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isComplete());
                Uri uri = uriTask.getResult();



                VideoController videoController = new VideoController(editText.getText().toString(), uri.toString());
                databaseReference.child(databaseReference.push().getKey()).setValue(videoController);
                Toast.makeText(UploadVideoActivity.this,"File Uploaded", Toast.LENGTH_LONG).show();
                progressDialog.dismiss();

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
                DocumentReference uidRef = rootRef.collection("users").document(uid);

                String link = reference.getDownloadUrl().toString();
                Timestamp timestamp = now();

                Map<String, Object> media = new HashMap<>();
                media.put("creator", uidRef);
                media.put("is_livestream", false);
                media.put("link", uriTask.toString());
                media.put("start_time", timestamp);
                media.put("thumbnail", "");
                media.put("title", titleText.getText().toString());
                media.put("view_count", 0);

                db.collection("media").document("upload_test").set(media);

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                double progress=(100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                progressDialog.setMessage("File Uploading.." +(int) progress + "%");
            }
        });
    }

}