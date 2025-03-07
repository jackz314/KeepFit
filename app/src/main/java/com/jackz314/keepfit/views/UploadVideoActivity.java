package com.jackz314.keepfit.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jackz314.keepfit.R;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.firebase.Timestamp.now;


public class UploadVideoActivity extends AppCompatActivity {

    private static final String TAG = "UploadVideoActivity";
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    EditText editText;
    EditText titleText;
    Button btn;
    EditText categoryText;
    boolean[] selectedCategory;
    ArrayList<Integer> categoryList = new ArrayList<>();
    StorageReference storageReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        editText = findViewById(R.id.choose_file_to_upload);
        titleText = findViewById(R.id.video_title_input);
        categoryText = findViewById(R.id.categories_input);
        btn = findViewById(R.id.btn_video_upload);

        storageReference = FirebaseStorage.getInstance().getReference();

        List<String> cgs = Arrays.asList(getResources().getStringArray(R.array.exercise_categories));

        String[] categoryArray = new String[cgs.size()];
        for (int j = 0; j < categoryArray.length; j++) {
            categoryArray[j] = cgs.get(j);
        }


        selectedCategory = new boolean[categoryArray.length];

        categoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        UploadVideoActivity.this
                );

                builder.setTitle("Select Category");
                builder.setCancelable(false);

                builder.setMultiChoiceItems(categoryArray, selectedCategory, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i, boolean b) {
                        if (b) {
                            categoryList.add(i);
                            Collections.sort(categoryList);
                        } else {
                            categoryList.remove(i);
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int j = 0; j < categoryList.size(); j++) {
                            stringBuilder.append(categoryArray[categoryList.get(j)]);
                            if (j != categoryList.size() - 1) {
                                stringBuilder.append(", ");
                            }
                        }

                        categoryText.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < selectedCategory.length; j++) {
                            //Remove all selection
                            selectedCategory[j] = false;
                            categoryList.clear();
                            categoryText.setText("");
                        }
                    }
                });

                builder.show();

            }


        });

        btn.setEnabled(false);
        editText.setOnClickListener(view -> selectVideo());

    }

    private void selectVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent videoFileSelect = Intent.createChooser(intent, "VIDEO FILE SELECT");
        startActivityForResult(videoFileSelect, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 12 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            btn.setEnabled(true);

            String path = data.getDataString();
            String filename = path.substring(path.lastIndexOf("/") + 1);
            editText.setText(filename);
            btn.setOnClickListener(view -> {

                Uri fileInfo = data.getData();
                Cursor returnCursor =
                        getContentResolver().query(fileInfo, null, null, null, null);
                returnCursor.moveToFirst();
                //long sizeIndex = returnCursor.getLong(Math.min(0, returnCursor.getColumnIndex(OpenableColumns.SIZE)));
                AssetFileDescriptor afd = null;
                try {
                    afd = getContentResolver().openAssetFileDescriptor(fileInfo, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                long sizeIndex = afd.getLength();

                if (sizeIndex < 5 * 1024 * 1024) {
                    Toast.makeText(UploadVideoActivity.this, "File size okay", Toast.LENGTH_LONG).show();
                    uploadVideoFirebase(data.getData());
                    Long s = sizeIndex / (1024 * 1024);
                    Log.d("XXXXXXX", s + "MB");
                } else {
                    Toast.makeText(UploadVideoActivity.this, "File size should be less than 5MB", Toast.LENGTH_SHORT).show();
                    Long s = sizeIndex / (1024 * 1024);
                    Log.d("XXXXXXX", s + "MB");
                }
            });
        }
    }

    private void uploadVideoFirebase(Uri data) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is loading...");
        progressDialog.show();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference uidRef = rootRef.collection("users").document(uid);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getApplicationContext(), data);
        String result = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Long durationNumber = Long.parseLong(result) / 1000;


        retriever.release();

        String filePath = getPathFromURI(data);

        String path = uidRef.toString();
        String[] segments = path.split("@");
        String userID = segments[segments.length - 1];

        StorageReference reference = storageReference.child(titleText.getText().toString() + "@" + user.getUid() + ".mp4");


        reference.putFile(data).addOnSuccessListener(taskSnapshot -> {

            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isComplete()) ;
            Uri uri = uriTask.getResult();

            PutVideo putvideo = new PutVideo(editText.getText().toString(), uri.toString());

            Long durationLong = durationNumber;
            String categoriesString = categoryText.getText().toString();

            String link = uri.toString();
            Timestamp timestamp = now();

            String raw = categoryText.getText().toString();
            List<String> categoryList = Arrays.stream(raw.split(",")).map(String::trim).collect(Collectors.toList());

            String titleTmp = titleText.getText().toString();
            if (titleTmp.length() == 0) {
                titleTmp = "Untitled";
            }

            boolean isCommentable = ((CheckBox) findViewById(R.id.comment_checkbox)).isChecked();
            Log.e(TAG, String.valueOf(isCommentable));

            Map<String, Object> media = new HashMap<>();
            media.put("categories", categoryList);
            media.put("creator", uidRef);
            media.put("is_livestream", false);
            media.put("is_commentable", isCommentable);
            media.put("link", link);
            media.put("likes", 0);
            media.put("duration", durationLong);
            media.put("start_time", timestamp);
            media.put("title", titleTmp);
            media.put("view_count", 0);
            media.put("thumbnail", "");

            DocumentReference mediaRef = db.collection("media").document();
            mediaRef.set(media);
            Map<String, Object> refData = new HashMap<>();
            refData.put("ref", mediaRef);
            db.collection("users").document(user.getUid()).collection("videos").document().set(refData)
                    .addOnCompleteListener(task -> {
                        Toast.makeText(UploadVideoActivity.this, "File Uploaded", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                        finish();
                    });
        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            progressDialog.setMessage("File Uploading.." + (int) progress + "%");
        });

    }

    public String getPathFromURI(Uri ContentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver()
                .query(ContentUri, proj, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();

            res = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            cursor.close();
        }


        return res;
    }

    public class PutVideo {
        public String title;
        public String url;

        public PutVideo() {
        }

        public PutVideo(String name, String url) {
            this.title = name;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}