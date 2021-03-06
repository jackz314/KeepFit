package com.jackz314.keepfit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jackz314.keepfit.databinding.ActivityNewUserBinding;
import com.jackz314.keepfit.databinding.FragmentMeBinding;

public class NewUserActivity extends AppCompatActivity {

    private ActivityNewUserBinding b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        b.finishNewUserBtn.setOnClickListener(view -> {

        });
    }
}