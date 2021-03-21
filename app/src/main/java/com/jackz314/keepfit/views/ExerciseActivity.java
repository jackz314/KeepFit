package com.jackz314.keepfit.views;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.ExerciseController;
import com.jackz314.keepfit.controllers.UserControllerKt;
import com.jackz314.keepfit.databinding.ActivityExerciseBinding;
import com.jackz314.keepfit.models.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class ExerciseActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseActivity";

    private ActivityExerciseBinding b;

    private StopwatchTextView stopwatch;

    private User user;
    private ExerciseController exerciseController;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityExerciseBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // set transparent status bar and navigation
        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        w.setStatusBarColor(Color.TRANSPARENT);
        w.setNavigationBarColor(Color.TRANSPARENT);

        int intensity = getIntent().getIntExtra(GlobalConstants.EXERCISE_INTENSITY, 2);
        float met = ExerciseController.getMETofIntensity(intensity);
        String intensityStr;
        if (intensity == 1) intensityStr = "Light";
        else if (intensity == 2) intensityStr = "Moderate";
        else intensityStr = "Vigorous";
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
            user.setBirthday(Date.from(LocalDate.of(2000,1,1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
            user.setSex(true);
            exerciseController = new ExerciseController(user, met);
        });
        compositeDisposable.add(disposable);

        String exerciseType = getIntent().getStringExtra(GlobalConstants.EXERCISE_TYPE);
        if (exerciseType != null) exerciseType = Utils.toTitleCase(exerciseType);
        b.exerciseTitle.setText(exerciseType);

        stopwatch = new StopwatchTextView(b.exerciseTimeText, 10);
        stopwatch.setOnTimeUpdateListener(elapsedTime -> {
            if (exerciseController != null) {
                runOnUiThread(() -> b.exerciseCaloriesText.setText(String.format(Locale.getDefault(), "%.3f", exerciseController.getCalBurned(elapsedTime))));
            }
        }, 100); // update every second
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

        String finalExerciseType = exerciseType;
        b.exerciseStopBtn.setOnClickListener(v -> {
            long elapsedTime = stopwatch.getElapsedTime();
            stopwatch.stop();
            exerciseController.uploadExercise(finalExerciseType, elapsedTime);
            Toast.makeText(this, "You exercised for " + String.format(Locale.getDefault(),
                    "%d minutes", TimeUnit.MILLISECONDS.toMinutes(elapsedTime)), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}