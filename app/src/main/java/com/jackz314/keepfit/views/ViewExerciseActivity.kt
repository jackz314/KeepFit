package com.jackz314.keepfit.views

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.UtilsKt
import com.jackz314.keepfit.controllers.ExerciseController
import com.jackz314.keepfit.databinding.ActivityViewExerciseBinding
import com.jackz314.keepfit.models.Exercise
import java.util.*


private const val TAG = "ViewExerciseActivity"

class ViewExerciseActivity : AppCompatActivity() {

    private lateinit var b: ActivityViewExerciseBinding
    private lateinit var exercise: Exercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityViewExerciseBinding.inflate(layoutInflater)
        setContentView(b.root)

        val ex = intent.getSerializableExtra(GlobalConstants.EXERCISE_OBJ)
        if (ex == null) {
            Log.e(TAG, "onCreate: couldn't get exercise object")
            Toast.makeText(this, "Couldn't get exercise object!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            exercise = ex as Exercise
            b.viewExerciseCategory.text = exercise.category
            b.viewExerciseDate.text = DateUtils.getRelativeDateTimeString(this, exercise.startingTime.time,
                    DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_SHOW_TIME)
            b.viewExerciseDuration.text = UtilsKt.formatDurationTextString(exercise.elapsedTime / DateUtils.SECOND_IN_MILLIS)
            b.viewExerciseCal.text = String.format(Locale.getDefault(), "%.3f", exercise.calories)
            b.viewExerciseCalPerMin.text = String.format(Locale.getDefault(), "%.3f", exercise.calories / (exercise.elapsedTime.toDouble() / DateUtils.MINUTE_IN_MILLIS))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_view_exercise, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.delete_exercise_btn) {
            ExerciseController.deleteExercise(exercise.uid)
            Toast.makeText(this, "Exercise deleted!", Toast.LENGTH_SHORT).show()
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}