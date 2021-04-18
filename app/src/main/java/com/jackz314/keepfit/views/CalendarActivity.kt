package com.jackz314.keepfit.views

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.ScheduledExerciseActivity
import com.jackz314.keepfit.UtilsKt
import com.jackz314.keepfit.controllers.UserControllerKt
import com.jackz314.keepfit.databinding.ActivityCalendarBinding
import com.jackz314.keepfit.models.Exercise
import com.jackz314.keepfit.views.other.CurrentDayDecorator
import com.jackz314.keepfit.views.other.EventDecorator
import com.jackz314.keepfit.views.other.ExerciseRecyclerAdapter
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*

private const val TAG = "CalendarActivity"

class CalendarActivity : AppCompatActivity() {
    private lateinit var b: ActivityCalendarBinding
    private val exerciseList = mutableListOf<Exercise>()
    private val selectedDayExerciseHistoryList = mutableListOf<Exercise>()
    private val eventDecorator = EventDecorator(Color.RED)
    private lateinit var exerciseRecyclerAdapter: ExerciseRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.calendarView.currentDate = CalendarDay.today()
        b.calendarView.addDecorator(CurrentDayDecorator(this, CalendarDay.today()))
        val dateMilli = intent.getLongExtra(GlobalConstants.CALENDAR_DATE, System.currentTimeMillis())
        b.calendarView.selectedDate = UtilsKt.millisToCalendarDate(dateMilli)

        exerciseRecyclerAdapter = ExerciseRecyclerAdapter(this, selectedDayExerciseHistoryList)
        exerciseRecyclerAdapter.setClickListener { view: View?, position: Int ->
            val exercise = exerciseList[position]
            val intent = Intent(this, ViewExerciseActivity::class.java)
            intent.putExtra(GlobalConstants.EXERCISE_OBJ, exercise)
            startActivity(intent)
        }

        b.exerciseLogRecycler.adapter = exerciseRecyclerAdapter
        val layoutManager = LinearLayoutManager(this)
        b.exerciseLogRecycler.layoutManager = layoutManager
        b.exerciseLogRecycler.isNestedScrollingEnabled = false
        val dividerItemDecoration = DividerItemDecoration(b.exerciseLogRecycler.context,
                layoutManager.orientation)
        b.exerciseLogRecycler.addItemDecoration(dividerItemDecoration)

        b.scheduledAddBtn.setOnClickListener { startActivity(Intent(this, ScheduledExerciseActivity::class.java)) }

        UserControllerKt.currentUserDoc.collection("exercises").orderBy("starting_time", Query.Direction.ASCENDING).addSnapshotListener { value, e ->
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            exerciseList.clear()
            exerciseList.addAll(value.toObjects(Exercise::class.java))
            updateCalendarDots()
            updateSelectedDay(b.calendarView.selectedDate!!)
        }

        b.calendarView.setOnDateChangedListener { widget, date, selected -> if (selected) updateSelectedDay(date) }

        b.calendarView.addDecorator(eventDecorator)
    }

    private fun updateCalendarDots() {
        eventDecorator.setEventDates(exerciseList.map(Exercise::getStartingTime).map(Date::getTime).map(UtilsKt::millisToCalendarDate))
        b.calendarView.invalidateDecorators()
    }

    private fun updateSelectedDay(selectedDay: CalendarDay) {
        selectedDayExerciseHistoryList.clear()
        for (exercise in exerciseList) {
            val day = UtilsKt.millisToCalendarDate(exercise.startingTime.time)
            if (selectedDay == day) {
                selectedDayExerciseHistoryList.add(exercise)
            }
        }
        exerciseRecyclerAdapter.notifyDataSetChanged()
        if (selectedDayExerciseHistoryList.isEmpty()) {
            b.noExerciseText.visibility = View.VISIBLE
        } else {
            b.noExerciseText.visibility = View.GONE
        }
    }


}