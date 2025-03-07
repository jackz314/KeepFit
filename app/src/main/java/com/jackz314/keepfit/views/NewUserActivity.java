package com.jackz314.keepfit.views;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.ActivityNewUserBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class NewUserActivity extends AppCompatActivity {

    private final FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private ActivityNewUserBinding b;
    private TextView mDisplayBirthday;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText mUsernameEditText;
    private EditText mBiographyEditText;
    private EditText mHeightEditText;
    private EditText mWeightEditText;
    private Calendar mBirthday = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityNewUserBinding.inflate(getLayoutInflater());
        View rootView = b.getRoot();
        setContentView(rootView);
        mUsernameEditText = (EditText) findViewById(R.id.editTextUsername);
        mBiographyEditText = (EditText) findViewById(R.id.editTextTextBio);
        mHeightEditText = (EditText) findViewById(R.id.editTextHeight);
        mWeightEditText = (EditText) findViewById(R.id.editTextWeight);
        mUsernameEditText.setText(mFirebaseUser.getDisplayName());
        b.finishNewUserBtn.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();

            mUsernameEditText = findViewById(R.id.editTextUsername);
            String strUsername = mUsernameEditText.getText().toString();
            if (strUsername.trim().matches((""))) {
                Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
                return;
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
            }

            double weight = 0.0;
            if (!mWeightEditText.getText().toString().matches("")) {
                weight = Double.parseDouble(mWeightEditText.getText().toString());
                weight = 0.453592 * weight;
            }

            String photoUrl = "";
            if (mFirebaseUser.getPhotoUrl() != null) {
                photoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            Map<String, Object> user = new HashMap<>();
            user.put("biography", mBiographyEditText.getText().toString());
            user.put("birthday", mBirthday.getTime());
            user.put("email", mFirebaseUser.getEmail());
            user.put("height", height);
            user.put("name", strUsername);
            user.put("profile_pic", photoUrl);
            user.put("sex", sex);
            user.put("weight", weight);

            if (!mFirebaseUser.getDisplayName().equals(strUsername)) {
                mFirebaseUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(strUsername).build());
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(mFirebaseUser.getUid())
                    .set(user)
                    .addOnSuccessListener(aVoid -> finish())
                    .addOnFailureListener(e -> Toast.makeText(NewUserActivity.this, "Error creating profile", Toast.LENGTH_SHORT).show());
        });

        Spinner spinner = findViewById(R.id.sex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sex, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        mDisplayBirthday = findViewById(R.id.birthday);
        mDisplayBirthday.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    NewUserActivity.this,
                    android.R.style.Theme_DeviceDefault_Dialog,
                    mDateSetListener,
                    year, month, day);
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = month + "/" + day + "/" + year;
            mDisplayBirthday.setText(date);
            mBirthday = Calendar.getInstance();
            mBirthday.set(year, month, day, 0, 0);
        };

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(NewUserActivity.this, "Please create your new profile.", Toast.LENGTH_SHORT).show();
    }


}