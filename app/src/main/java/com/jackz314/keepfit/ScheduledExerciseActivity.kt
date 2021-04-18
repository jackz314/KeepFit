package com.jackz314.keepfit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jackz314.keepfit.databinding.ActivityScheduledExerciseBinding

class ScheduledExerciseActivity : AppCompatActivity() {
    private lateinit var b: ActivityScheduledExerciseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityScheduledExerciseBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}