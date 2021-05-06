package com.jackz314.keepfit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import com.jackz314.keepfit.controllers.ExerciseController
import com.jackz314.keepfit.controllers.SchedulingController
import com.jackz314.keepfit.databinding.ActivityScheduledExerciseBinding
import com.jackz314.keepfit.models.ScheduledExercise
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class ScheduledExerciseActivity : AppCompatActivity() {
    private lateinit var b: ActivityScheduledExerciseBinding
    private lateinit var saveScheduleItem: MenuItem
    private lateinit var deleteScheduleItem: MenuItem
    private var originalSchedule: ScheduledExercise? = null
    private lateinit var newSchedule: ScheduledExercise
    private var selectedCategoryIndex = 0
    private var timeSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityScheduledExerciseBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.scheduleExerciseTime.setOnClickListener { setExerciseTime() }
        b.scheduleExerciseDate.setOnClickListener { setExerciseDate() }
        b.scheduleExerciseCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ExerciseController.getExerciseCategoryArray(this))
    }

    private fun setSaveItemStatus(saveEnabled: Boolean) {
        saveScheduleItem.isEnabled = saveEnabled
        saveScheduleItem.isVisible = saveEnabled
    }

    private fun setDateText(date: Date, formatTime: Boolean = true) {
        b.scheduleExerciseDate.text = if (DateUtils.isToday(date.time)) "Today" else
            DateUtils.formatDateTime(this, date.time, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)
        if (formatTime) b.scheduleExerciseTime.text = DateUtils.formatDateTime(this, date.time, DateUtils.FORMAT_SHOW_TIME)
    }

    private fun setExerciseTime() {
        val time = Instant.ofEpochMilli(newSchedule.time.time).atZone(ZoneId.systemDefault())
        TimePickerDialog(this, { _, hour, minute ->
            newSchedule.time = Date.from(time.with(LocalTime.of(hour, minute)).toInstant())
            timeSet = true
            setDateText(newSchedule.time)
            setSaveItemStatus(!newSchedule.time.equals(originalSchedule?.time))
        }, time.hour, time.minute, DateFormat.is24HourFormat(this)).show()
    }

    private fun setExerciseDate() {
        val time = Instant.ofEpochMilli(newSchedule.time.time).atZone(ZoneId.systemDefault())
        DatePickerDialog(this, { _, year, month, day ->
            newSchedule.time = Date.from(time.with(LocalDate.of(year, month, day)).toInstant())
            setDateText(newSchedule.time, false)
            setSaveItemStatus(timeSet && !newSchedule.time.equals(originalSchedule?.time))
        }, time.year, time.monthValue, time.dayOfMonth).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_schedule_exercise, menu)
        saveScheduleItem = menu!!.findItem(R.id.schedule_save_item)
        deleteScheduleItem = menu.findItem(R.id.schedule_delete_item)

        val exerciseArr = ExerciseController.getExerciseCategoryArray(this)
        if (intent.hasExtra(GlobalConstants.SCHEDULED_EXERCISE)) {
            timeSet = true
            originalSchedule = intent.getSerializableExtra(GlobalConstants.SCHEDULED_EXERCISE) as ScheduledExercise
            newSchedule = ScheduledExercise(originalSchedule)
            selectedCategoryIndex = exerciseArr.indexOf(newSchedule.category.trim().capitalize(Locale.getDefault()))
            if (selectedCategoryIndex == -1) selectedCategoryIndex = exerciseArr.indexOf("Other")
            b.scheduleExerciseCategory.setSelection(selectedCategoryIndex)
            setDateText(newSchedule.time)
            deleteScheduleItem.isVisible = true
        } else {
            val presetDate = if (intent.hasExtra(GlobalConstants.SCHEDULE_PRESET_DATE)) intent.getSerializableExtra(GlobalConstants.SCHEDULE_PRESET_DATE) as Date else Date()
            newSchedule = ScheduledExercise(presetDate, "", 2)
            saveScheduleItem.isEnabled = false
            saveScheduleItem.isVisible = true
            setDateText(presetDate, false)
        }

        b.scheduleExerciseCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                setSaveItemStatus(timeSet && selectedCategoryIndex != position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.schedule_save_item) {
            newSchedule.intensity = getIntensityValue(b.scheduleExerciseIntensity.checkedChipId)
            newSchedule.category = ExerciseController.getExerciseCategoryArray(this)[b.scheduleExerciseCategory.selectedItemPosition]
            if (!SchedulingController.scheduleExercise(this, newSchedule)) {
                Toast.makeText(this, "Cannot schedule exercise in the past :(", Toast.LENGTH_SHORT).show()
            } else {
                if (originalSchedule != null) SchedulingController.cancelScheduledExercise(this, originalSchedule!!)
                Toast.makeText(this, "Exercise scheduled!", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else if (item.itemId == R.id.schedule_delete_item) {
            SchedulingController.cancelScheduledExercise(this, originalSchedule!!)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getIntensityValue(@IdRes id: Int) = when (id) {
        R.id.prompt_intensity_low -> 1
        R.id.prompt_intensity_medium -> 2
        R.id.prompt_intensity_high -> 3
        else -> 2
    }

}