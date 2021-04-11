package com.jackz314.keepfit.views.other

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import androidx.core.content.ContextCompat
import com.jackz314.keepfit.R
import com.jackz314.keepfit.Utils.generateCircleDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CurrentDayDecorator(context: Activity?, currentDay: CalendarDay) : DayViewDecorator {
    private val drawable: Drawable
    var myDay = currentDay
    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == myDay
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(drawable)
    }

    init {
        // You can set background for Decorator via drawable here
        drawable = ContextCompat.getDrawable(context!!, R.drawable.current_day_circle)!!
//        drawable = generateSelector()
    }

    private fun generateSelector(): Drawable {
        val drawable = StateListDrawable()
        drawable.setExitFadeDuration(2)
        drawable.addState(intArrayOf(android.R.attr.state_checked), generateCircleDrawable(Color.RED))
        drawable.addState(intArrayOf(android.R.attr.state_pressed), generateCircleDrawable(Color.RED))
        drawable.addState(intArrayOf(), generateCircleDrawable(Color.TRANSPARENT))
        return drawable
    }
}