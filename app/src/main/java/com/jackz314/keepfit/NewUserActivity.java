package com.jackz314.keepfit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jackz314.keepfit.databinding.ActivityNewUserBinding;
import com.jackz314.keepfit.databinding.FragmentMeBinding;

public class NewUserActivity extends AppCompatActivity {

    private ActivityNewUserBinding b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityNewUserBinding.inflate(getLayoutInflater());
        View rootView = b.getRoot();
        setContentView(rootView);
        b.finishNewUserBtn.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            Toast.makeText(this, "DONE", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}