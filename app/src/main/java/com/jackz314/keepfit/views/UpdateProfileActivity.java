package com.jackz314.keepfit.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.models.User;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateProfileActivity extends AppCompatActivity {

    private TextView mDisplayBirthday;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText mUsernameEditText;
    private EditText mBiographyEditText;
    private EditText mHeightEditText;
    private EditText mWeightEditText;
    private Calendar mBirthday = Calendar.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private Map mCurrentData;

    private String originalName;
    private String originalBio;
    private int originalWeight;
    private int originalHeight;
    private Date originalBirthday;
    private String originalPhoto;
    boolean imgChosen = false;
    boolean imgUploaded = false;

    /////
    // views for button
    private Button btnSelect;
    // view for image view
    private ImageView imageView;


    // Uri indicates, where the image will be picked from
    private Uri filePath = null;
    String imgLink = "";

    StorageReference storageReference;

    // request code
    private final int PICK_IMAGE_REQUEST = 22;
    /////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);


        mUsernameEditText = (EditText) findViewById(R.id.editTextUsername);
        mBiographyEditText = (EditText) findViewById(R.id.editTextTextBio);
        mHeightEditText = (EditText) findViewById(R.id.editTextHeight);
        mWeightEditText = (EditText) findViewById(R.id.editTextWeight);

        /////
        storageReference = FirebaseStorage.getInstance().getReference();
        // initialise views
        btnSelect = findViewById(R.id.btnChoose);
        imageView = findViewById(R.id.imgView);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                selectImage();
            }
        });


        Button finishEdit = findViewById(R.id.finish_new_user_btn);

        //get current user data
        DocumentReference userDoc = db.collection("users").document(mFirebaseUser.getUid());
        userDoc.get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot dataResult = task.getResult();
                    originalName = (String) dataResult.getString("name");
                    originalBio =(String) dataResult.getString("biography");
                    originalWeight = dataResult.getLong("weight").intValue();
                    originalHeight = dataResult.getLong("height").intValue();
                    originalBirthday = dataResult.getDate("birthday");
                    originalPhoto = dataResult.getString("profile_pic");
                });



        finishEdit.setOnClickListener(view -> {
            if (imgChosen == imgUploaded) {
                mUsernameEditText = findViewById(R.id.editTextUsername);
                String strUsername = mUsernameEditText.getText().toString();
                if (TextUtils.isEmpty(strUsername)) {
                    //get current data
                    strUsername = originalName;
                }

                String bio = "";
                if (mBiographyEditText.getText().toString().matches("")) {
                    //get current data
                    bio = originalBio;
                } else {
                    bio = mBiographyEditText.getText().toString();
                }

                Spinner spinner = findViewById(R.id.sex);
                boolean sex = true;
                if (spinner.getSelectedItem().toString().matches("Female")) {
                    sex = false;
                }

                double height = 0.0;
                if (!mHeightEditText.getText().toString().matches("")) {
                    height = Double.parseDouble(mHeightEditText.getText().toString());
                    height = 2.54 * height;
                } else {
                    //get current data
                    height = originalHeight;
                }

                double weight = 0.0;
                if (!mWeightEditText.getText().toString().matches("")) {
                    weight = Double.parseDouble(mWeightEditText.getText().toString());
                    weight = 0.453592 * weight;
                } else {
                    //get current data
                    weight = originalWeight;
                }

                Map<String, Object> user = new HashMap<>();
                user.put("biography", bio);
                //birthday check
                if (mBirthday.get(Calendar.YEAR) >= java.time.LocalDate.now().getYear()) {
                    user.put("birthday", originalBirthday);
                } else {
                    user.put("birthday", mBirthday.getTime());
                }
                user.put("email", mFirebaseUser.getEmail());
                user.put("height", height);
                user.put("name", strUsername);

                String photoStr;
                if (filePath == null) {
                    photoStr = originalPhoto;
                    Log.d("Update Profile", "Link: null");
                } else {
                    photoStr = imgLink;
                    Log.d("Update Profile", "Link: not null");
                }
                Log.d("Update Profile", "Link: " + photoStr);
                user.put("profile_pic", photoStr);
                user.put("sex", sex);
                user.put("weight", weight);

                if (!strUsername.equals(mFirebaseUser.getDisplayName())) {
                    mFirebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(strUsername).build());
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(mFirebaseUser.getUid())
                        .update(user)
                        .addOnSuccessListener(aVoid -> finish())
                        .addOnFailureListener(e -> Toast.makeText(UpdateProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
                setResult(Activity.RESULT_OK);
            } else {
               Toast.makeText(UpdateProfileActivity.this, "Please wait for image to upload", Toast.LENGTH_LONG).show();
            }
        });

        Spinner spinner = findViewById(R.id.sex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mDisplayBirthday = (TextView) findViewById(R.id.birthday);
        mDisplayBirthday.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    UpdateProfileActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog,
                    mDateSetListener,
                    year, month, day);
            dialog.show();
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String date = month + "/" + day + "/" + year;
                mDisplayBirthday.setText(date);
                mBirthday = Calendar.getInstance();
                mBirthday.set(year, month, day);
            }
        };
    }


    // Select Image method
    private void selectImage()
    {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            imgChosen = true;
            // Get the Uri of data

            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                imageView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }

            Log.d("Update Profile", "Link: " + filePath);
        }
        uploadImage();
    }



    // UploadImage method
    private void uploadImage() {
            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + mFirebaseUser.getUid());
            // adding listeners on upload
            if (filePath != null) {
                Toast.makeText(UpdateProfileActivity.this,"Profile picture uploading", Toast.LENGTH_LONG).show();
                ref.putFile(filePath)
                        .addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                    @Override
                                    public void onSuccess(
                                            UploadTask.TaskSnapshot taskSnapshot) {
                                        Log.d("Update Profile", "Uploaded!");
                                        Toast.makeText(UpdateProfileActivity.this, "Profile picture uploaded!", Toast.LENGTH_SHORT).show();
                                        /////from upload video
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isComplete()) ;
                                        Uri uri = uriTask.getResult();

                                        imgLink = uri.toString();
                                        Log.d("Update Profile", imgLink);
                                        imgUploaded = true;
                                        /////
                                    }
                                })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("Update Profile", "Failed to upload");
                                Toast.makeText(UpdateProfileActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(
                                new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(
                                            UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress
                                                = (100.0
                                                * taskSnapshot.getBytesTransferred()
                                                / taskSnapshot.getTotalByteCount());
                                        Log.d("Update Profile", "Uploaded " + (int) progress + "%");
                                    }
                                });
            } 
    }


}
