package com.jackz314.keepfit.views;

import android.app.Activity;
import android.app.DatePickerDialog;
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

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

    String originalName;
    String originalBio;
    double originalWeight;
    double originalHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);



        mUsernameEditText = (EditText) findViewById(R.id.editTextUsername);
        mBiographyEditText = (EditText) findViewById(R.id.editTextTextBio);
        mHeightEditText = (EditText) findViewById(R.id.editTextHeight);
        mWeightEditText = (EditText) findViewById(R.id.editTextWeight);;


        Button finishEdit = findViewById(R.id.finish_new_user_btn);

        finishEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //get current user data
                db.collection("users").document(mFirebaseUser.getUid()).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot dataResult = task.getResult();
                                originalName = (String) dataResult.get("name");
                                originalBio =(String) dataResult.get("biography");
                                originalWeight = (double) dataResult.get("weight");
                                originalHeight = (double) dataResult.get("height");
                            }
                        });


                mUsernameEditText = (EditText) findViewById(R.id.editTextUsername);
                String strUsername = mUsernameEditText.getText().toString();
                if (TextUtils.isEmpty(strUsername)) {
                    //get current data
                    strUsername = originalName;
                    Log.d("Update Profile", "Name is empty");
                }  else {
                    Log.d("Update Profile", "Name is not empty");
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
                user.put( "biography", bio);
                user.put("birthday", mBirthday.getTime());
                user.put("email", mFirebaseUser.getEmail());
                user.put("height", height);
                user.put("name", strUsername);
                user.put("profile_pic", mFirebaseUser.getPhotoUrl().toString());
                user.put("sex", sex);
                user.put("weight", weight);


                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(mFirebaseUser.getUid())
                        .set(user)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(UpdateProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                            }
                        });
                setResult(Activity.RESULT_OK);
            }
        });

        Spinner spinner = findViewById(R.id.sex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mDisplayBirthday = (TextView) findViewById(R.id.birthday);
        mDisplayBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = month + "/" + day + "/" + year;
                mDisplayBirthday.setText(date);
                mBirthday = Calendar.getInstance();
                mBirthday.set(year, month, day);
            }
        };
    }
}