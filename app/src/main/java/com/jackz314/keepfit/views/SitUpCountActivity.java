package com.jackz314.keepfit.views;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.ExerciseController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityExerciseBinding;
import com.jackz314.keepfit.databinding.ActivitySitUpCountBinding;
import com.jackz314.keepfit.models.User;
import com.jackz314.keepfit.views.other.StopwatchTextView;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SitUpCountActivity extends AppCompatActivity {
    private static final String TAG = "SitUpCountActivity";

    private ActivitySitUpCountBinding b;

    private StopwatchTextView stopwatch;

    private User user;
    private ExerciseController exerciseController;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private boolean doubleBackToExitPressedOnce = false;

    private String exerciseType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySitUpCountBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());


        int intensity = getIntent().getIntExtra(GlobalConstants.EXERCISE_INTENSITY, 2);
        Log.e(TAG, "Intensity " + intensity);
        float met = ExerciseController.getMETofIntensity(intensity);
        String intensityStr = ExerciseController.getStrOfIntensity(intensity);
        b.exerciseIntensity.setText(intensityStr + " Intensity");

        Disposable disposable = UserControllerKt.getCurrentUser().subscribe(user -> {
            this.user = user;
            exerciseController = new ExerciseController(user, met);
        }, e -> {
            Log.e(TAG, "onCreate: failed to get current user", e);
            Toast.makeText(this, "Failed to get your information, some data might be inaccurate", Toast.LENGTH_SHORT).show();
            user = new User(); // use defaults to estimate stuff
            user.setHeight(170);
            user.setWeight(65);
            user.setBirthday(Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            user.setSex(true);
            exerciseController = new ExerciseController(user, met);
        });
        compositeDisposable.add(disposable);

        exerciseType = getIntent().getStringExtra(GlobalConstants.EXERCISE_TYPE);
        if (exerciseType != null) {
            ExerciseController.setMostRecentExercise(this, exerciseType);
            exerciseType = Utils.toTitleCase(exerciseType);
        }
        b.exerciseTitle.setText(exerciseType);

        //sit up frequency based on intensity
        int sitUpInterval;
        if (intensity == 1) {
            sitUpInterval = 3500;
        } else if (intensity == 2) {
            sitUpInterval = 2000;
        } else {
            sitUpInterval = 1000;
        }
        stopwatch = new StopwatchTextView(b.exerciseTimeText, 50, this);
        stopwatch.setOnTimeUpdateListener(elapsedTime -> {
            if (exerciseController != null) {
                runOnUiThread(() -> b.sitUpCount.setText(String.valueOf((int)elapsedTime/sitUpInterval)));
            }
        }, 10);
        stopwatch.start();

        b.exercisePauseResumeBtn.setOnClickListener(v -> {
            if (stopwatch.getState() == StopwatchTextView.TimerState.PAUSED) {
                stopwatch.resume();
                b.exercisePauseResumeBtn.setImageResource(R.drawable.ic_baseline_pause_24);
            } else {
                stopwatch.pause();
                b.exercisePauseResumeBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24);
            }
        });

        b.exerciseStopBtn.setOnClickListener(v -> finishExercise());
    }

    private void finishExercise() {
        long elapsedTime = stopwatch.getElapsedTime();
        stopwatch.stop();
        exerciseController.uploadExercise(exerciseType, elapsedTime);
        Toast.makeText(this, "You exercised for " + String.format(Locale.getDefault(),
                "%d minutes", TimeUnit.MILLISECONDS.toMinutes(elapsedTime)), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishExercise();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to stop exercising", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

}
