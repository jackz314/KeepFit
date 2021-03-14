package com.jackz314.keepfit.views;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.databinding.ActivityNewUserBinding;
import com.jackz314.keepfit.databinding.FragmentMeBinding;

import java.util.Calendar;


public class NewUserActivity extends AppCompatActivity {

    private ActivityNewUserBinding b;

    private TextView mDisplayBirthday;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private EditText mUsernameEditText;

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
            finish();
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