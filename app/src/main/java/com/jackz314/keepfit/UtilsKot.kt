package com.jackz314.keepfit

import java.time.Duration

object UtilsKot {
    @JvmStatic
    fun formatDurationString(duration: Long?): String {
        if (duration == null) return "0:00"
        val d = Duration.ofSeconds(duration)
        val hrs = d.toHours()
        val mins = d.minusHours(hrs).toMinutes()
        val secs = d.minusMinutes(mins).seconds
        return if (hrs > 0){
            "${hrs}:${"%02d".format(mins)}:${"%02d".format(secs)}"
        } else {
            "${mins}:${"%02d".format(secs)}"
        }
    }
}