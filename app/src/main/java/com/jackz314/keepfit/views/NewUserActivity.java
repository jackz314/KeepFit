package com.jackz314.keepfit.views;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
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
    private EditText mEmailEditText;
    private EditText mHeightEditText;
    private EditText mWeightEditText;
    private Calendar mBirthday = Calendar.getInstance();

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

            Spinner spinner = findViewById(R.id.sex);
            boolean sex = true;
            if (spinner.getSelectedItem().toString().matches("Female")) {
                sex = false;
            }

            Map<String, Object> user = new HashMap<>();
            Double height = 0.0;
            if (!mHeightEditText.getText().toString().matches("")) {
                height = Double.parseDouble(mHeightEditText.getText().toString());
                height = 2.54 * height;
            }

            Double weight = 0.0;
            if (!mWeightEditText.getText().toString().matches("")) {
                weight = Double.parseDouble(mWeightEditText.getText().toString());
                weight = 0.453592 * weight;
            }

            user.put( "biography", mBiographyEditText.getText().toString());
            user.put("birthday", mBirthday.getTime());
            user.put("email", mEmailEditText.getText().toString());
            user.put("height", height);
            user.put("name", userName);
            user.put("profile_pic", "");
            user.put("sex", sex);
            user.put("weight", weight);


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(userName)
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                            return;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(NewUserActivity.this, "Error creating profile", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                mBirthday = Calendar.getInstance();
                mBirthday.set(year, month, day);
            }
        };

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(NewUserActivity.this, "Please create your new profile.", Toast.LENGTH_SHORT).show();
    }


}