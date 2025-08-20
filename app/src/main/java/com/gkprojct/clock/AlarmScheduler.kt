package com.gkprojct.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.DayOfWeek
import java.util.Calendar

object AlarmScheduler {

    fun schedule(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateNextTriggerTime(alarm.time, alarm.repeatingDays)

        if (triggerTime != null) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarm ${alarm.label} scheduled for ${Calendar.getInstance().apply { timeInMillis = triggerTime }.time}")
        } else {
            Log.d("AlarmScheduler", "Alarm ${alarm.label} is a one-time alarm in the past. Not scheduling.")
        }
    }

    fun cancel(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Alarm ${alarm.label} cancelled.")
    }

    fun calculateNextTriggerTime(alarmTime: Calendar, repeatingDays: Set<DayOfWeek>): Long? {
        val now = Calendar.getInstance()

        if (repeatingDays.isEmpty()) {
            // One-time alarm
            return if (alarmTime.after(now)) alarmTime.timeInMillis else null
        }

        // Repeating alarm
        val alarmHour = alarmTime.get(Calendar.HOUR_OF_DAY)
        val alarmMinute = alarmTime.get(Calendar.MINUTE)

        val calendarDays = repeatingDays.map { it.toCalendarDay() }.sorted()

        var nextTrigger = now.clone() as Calendar
        nextTrigger.set(Calendar.HOUR_OF_DAY, alarmHour)
        nextTrigger.set(Calendar.MINUTE, alarmMinute)
        nextTrigger.set(Calendar.SECOND, 0)
        nextTrigger.set(Calendar.MILLISECOND, 0)

        val today = now.get(Calendar.DAY_OF_WEEK) // Sunday is 1, Saturday is 7

        for (day in calendarDays) {
            if (day > today || (day == today && nextTrigger.after(now))) {
                val daysToAdd = (day - today + 7) % 7
                nextTrigger.add(Calendar.DAY_OF_YEAR, daysToAdd)
                return nextTrigger.timeInMillis
            }
        }

        // If we are here, all repeating days for this week are in the past.
        // Schedule for the first available day next week.
        val firstDay = calendarDays.first()
        val daysToAdd = (firstDay - today + 7) % 7
        nextTrigger.add(Calendar.DAY_OF_YEAR, daysToAdd)
        if(nextTrigger.before(now)) {
            nextTrigger.add(Calendar.DAY_OF_YEAR, 7)
        }
        return nextTrigger.timeInMillis
    }

    private fun DayOfWeek.toCalendarDay(): Int {
        return when (this) {
            DayOfWeek.SUNDAY -> Calendar.SUNDAY
            DayOfWeek.MONDAY -> Calendar.MONDAY
            DayOfWeek.TUESDAY -> Calendar.TUESDAY
            DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
            DayOfWeek.THURSDAY -> Calendar.THURSDAY
            DayOfWeek.FRIDAY -> Calendar.FRIDAY
            DayOfWeek.SATURDAY -> Calendar.SATURDAY
        }
    }
}
