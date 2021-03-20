package com.jackz314.keepfit.views;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.ActivityNewUserBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class NewUserActivity extends AppCompatActivity {

    private ActivityNewUserBinding b;

    private TextView mDisplayBirthday;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText mUsernameEditText;
    private EditText mBiographyEditText;
    private EditText mBirthdayEditText;
    private Timestamp mBirthday;
    private EditText mEmailEditText;
    private EditText mHeightEditText;
    private EditText mWeightEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityNewUserBinding.inflate(getLayoutInflater());
        View rootView = b.getRoot();
        setContentView(rootView);
        b.finishNewUserBtn.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();
            mUsernameEditText = (EditText) findViewById(R.id.editTextUsername);
            String strUsername = mUsernameEditText.getText().toString();
            if (strUsername.matches((""))) {
                Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = mUsernameEditText.getText().toString();
            mBiographyEditText = (EditText) findViewById(R.id.editTextTextBio);
            mEmailEditText = (EditText) findViewById(R.id.editTextEmailAddress);
            mHeightEditText = (EditText) findViewById(R.id.editTextHeight);
            mWeightEditText = (EditText) findViewById(R.id.editTextWeight);

            Map<String, Object> user = new HashMap<>();
            user.put( "biography", mBiographyEditText.getText().toString());
            user.put("birthday", 0);
            user.put("email", mEmailEditText.getText().toString());
            user.put("height", 0);
            user.put("name", userName);
            user.put("profile_pic", "");
            user.put("sec", true);
            user.put("weight", 0);


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(userName)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });
                    /*.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });*/
            /*reference.push().setValue("sample_user_2").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    finish();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "onFailure: " + e.getMessage());
                }
            });*/
            /*finish();*/
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
                        NewUserActivity.this,
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
            }
        };



    }


}