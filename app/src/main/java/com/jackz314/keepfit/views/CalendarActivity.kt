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
import com.jackz314.keepfit.models.ScheduledExercise
import com.jackz314.keepfit.views.other.*
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.util.*

private const val TAG = "CalendarActivity"

class CalendarActivity : AppCompatActivity() {
    private lateinit var b: ActivityCalendarBinding
    private val pastExerciseList = mutableListOf<Exercise>()
    private val scheduledExerciseList = mutableListOf<ScheduledExercise>()
    private val selectedDayPastExerciseList = mutableListOf<Exercise>()
    private val selectedDayScheduledExerciseList = mutableListOf<ScheduledExercise>()
    private val eventDecorator = EventDecorator(Color.RED)
    private val scheduleDecorator = ScheduledDecorator(Color.RED)
    private lateinit var exerciseLogRecyclerAdapter: ExerciseRecyclerAdapter
    private lateinit var scheduledExerciseRecyclerAdapter: ScheduledExerciseRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.calendarView.currentDate = CalendarDay.today()
        b.calendarView.addDecorator(CurrentDayDecorator(this, CalendarDay.today()))
        val dateMilli = intent.getLongExtra(GlobalConstants.CALENDAR_DATE, System.currentTimeMillis())
        b.calendarView.selectedDate = UtilsKt.millisToCalendarDay(dateMilli)

        scheduledExerciseRecyclerAdapter = ScheduledExerciseRecyclerAdapter(this, selectedDayScheduledExerciseList)
        scheduledExerciseRecyclerAdapter.setClickListener { _, position ->
            startActivity(Intent(this, ScheduledExerciseActivity::class.java).apply {
                putExtra(GlobalConstants.SCHEDULED_EXERCISE, selectedDayScheduledExerciseList[position])
            })
        }

        exerciseLogRecyclerAdapter = ExerciseRecyclerAdapter(this, selectedDayPastExerciseList)
        exerciseLogRecyclerAdapter.setClickListener { _, position ->
            startActivity(Intent(this, ViewExerciseActivity::class.java).apply {
                putExtra(GlobalConstants.EXERCISE_OBJ, selectedDayPastExerciseList[position])
            })
        }

        b.scheduledExerciseRecycler.adapter = scheduledExerciseRecyclerAdapter
        val scheduleLayoutManager = LinearLayoutManager(this)
        b.scheduledExerciseRecycler.layoutManager = scheduleLayoutManager
        b.scheduledExerciseRecycler.isNestedScrollingEnabled = false
        b.scheduledExerciseRecycler.addItemDecoration(DividerItemDecoration(b.scheduledExerciseRecycler.context, scheduleLayoutManager.orientation))

        b.scheduledAddBtn.setOnClickListener {
            startActivity(Intent(this, ScheduledExerciseActivity::class.java)
                    .apply { putExtra(GlobalConstants.SCHEDULE_PRESET_DATE, UtilsKt.nowOrFuture(UtilsKt.calendarDayToDate(b.calendarView.selectedDate))) })
        }

        b.exerciseLogRecycler.adapter = exerciseLogRecyclerAdapter
        val layoutManager = LinearLayoutManager(this)
        b.exerciseLogRecycler.layoutManager = layoutManager
        b.exerciseLogRecycler.isNestedScrollingEnabled = false
        b.exerciseLogRecycler.addItemDecoration(DividerItemDecoration(b.exerciseLogRecycler.context, layoutManager.orientation))

        UserControllerKt.currentUserDoc.collection("scheduled_exercises").orderBy("time", Query.Direction.ASCENDING).addSnapshotListener { value, e ->
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            scheduledExerciseList.clear()
            scheduledExerciseList.addAll(value.toObjects(ScheduledExercise::class.java))
            updateScheduledDots()
            updateSelectedDay(b.calendarView.selectedDate!!)
        }

        UserControllerKt.currentUserDoc.collection("exercises").orderBy("starting_time", Query.Direction.ASCENDING).addSnapshotListener { value, e ->
            if (e != null || value == null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            pastExerciseList.clear()
            pastExerciseList.addAll(value.toObjects(Exercise::class.java))
            updateLogDots()
            updateSelectedDay(b.calendarView.selectedDate!!)
        }

        b.calendarView.setOnDateChangedListener { _, date, selected -> if (selected) updateSelectedDay(date) }
        b.calendarView.addDecorator(eventDecorator)
        b.calendarView.addDecorator(scheduleDecorator)
    }

    private fun updateLogDots() {
        eventDecorator.setEventDates(pastExerciseList.map(Exercise::getStartingTime).map(Date::getTime).map(UtilsKt::millisToCalendarDay))
        b.calendarView.invalidateDecorators()
    }

    private fun updateScheduledDots() {
        scheduleDecorator.setScheduledDates(scheduledExerciseList.map(ScheduledExercise::getTime).map(Date::getTime).map(UtilsKt::millisToCalendarDay))
        b.calendarView.invalidateDecorators()
    }

    private fun updateSelectedDay(selectedDay: CalendarDay) {
//        b.scheduledAddBtn.visibility = if(selectedDay.isBefore(CalendarDay.today())) View.GONE else View.VISIBLE
        selectedDayScheduledExerciseList.clear()
        for (exercise in scheduledExerciseList) {
            val day = UtilsKt.millisToCalendarDay(exercise.time.time)
            if (selectedDay == day) selectedDayScheduledExerciseList.add(exercise)
        }
        scheduledExerciseRecyclerAdapter.notifyDataSetChanged()

        selectedDayPastExerciseList.clear()
        for (exercise in pastExerciseList) {
            val day = UtilsKt.millisToCalendarDay(exercise.startingTime.time)
            if (selectedDay == day) selectedDayPastExerciseList.add(exercise)
        }
        exerciseLogRecyclerAdapter.notifyDataSetChanged()

        b.noExerciseText.visibility =
                if (selectedDayPastExerciseList.isEmpty() && selectedDayScheduledExerciseList.isEmpty()) View.VISIBLE else View.GONE

    }


}