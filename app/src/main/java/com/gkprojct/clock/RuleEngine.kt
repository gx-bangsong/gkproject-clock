package com.gkprojct.clock

import android.content.ContentResolver
import android.provider.CalendarContract
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class RuleEngine(private val contentResolver: ContentResolver) {

    fun evaluate(rule: Rule, evaluationTime: Instant): Boolean {
        if (!rule.enabled) {
            return false
        }

        return when (val criteria = rule.criteria) {
            is RuleCriteria.AlwaysTrue -> true
            is RuleCriteria.BasedOnTime -> {
                val evaluationLocalTime = evaluationTime.atZone(ZoneId.systemDefault()).toLocalTime()
                !evaluationLocalTime.isBefore(criteria.startTime) && !evaluationLocalTime.isAfter(criteria.endTime)
            }
            is RuleCriteria.IfCalendarEventExists -> {
                checkCalendarEvents(rule.calendarIds, criteria, evaluationTime)
            }
            is RuleCriteria.ShiftWork -> {
                checkShiftWork(criteria, evaluationTime)
            }
        }
    }

    private fun checkCalendarEvents(calendarIds: Set<Long>, criteria: RuleCriteria.IfCalendarEventExists, evaluationTime: Instant): Boolean {
        if (calendarIds.isEmpty()) {
            return false
        }

        val selection = "${CalendarContract.Events.CALENDAR_ID} IN (${calendarIds.joinToString(",")})"
        val selectionArgs = arrayOf<String>()

        val projection = arrayOf(
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.TITLE
        )

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val dtStart = it.getLong(0)
                val dtEnd = it.getLong(1)
                val allDay = it.getInt(2) == 1
                val title = it.getString(3) ?: ""

                val eventStart = Instant.ofEpochMilli(dtStart)
                val eventEnd = Instant.ofEpochMilli(dtEnd)

                val matchesKeywords = criteria.keywords.isEmpty() || criteria.keywords.any { keyword -> title.contains(keyword, ignoreCase = true) }
                val matchesAllDay = criteria.allDay == allDay

                if (matchesKeywords && matchesAllDay) {
                    if (allDay) {
                        val evaluationDate = evaluationTime.atZone(ZoneId.systemDefault()).toLocalDate()
                        val eventStartDate = eventStart.atZone(ZoneId.systemDefault()).toLocalDate()
                        if (evaluationDate.isEqual(eventStartDate)) {
                            return true
                        }
                    } else {
                        val checkTime = evaluationTime.plus(criteria.timeRangeMinutes.toLong(), ChronoUnit.MINUTES)
                        if (!checkTime.isBefore(eventStart) && !checkTime.isAfter(eventEnd)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun checkShiftWork(criteria: RuleCriteria.ShiftWork, evaluationTime: Instant): Boolean {
        val startDate = Instant.ofEpochMilli(criteria.startDate).atZone(ZoneId.systemDefault()).toLocalDate()
        val evaluationDate = evaluationTime.atZone(ZoneId.systemDefault()).toLocalDate()

        if (evaluationDate.isBefore(startDate)) {
            return false
        }

        // If the evaluation date is a holiday, it's not a work day.
        if (isHoliday(evaluationDate, criteria.holidayCalendarIds)) {
            return false
        }

        val daysBetween = when (criteria.holidayHandling) {
            HolidayHandlingStrategy.NORMAL_SCHEDULE -> {
                ChronoUnit.DAYS.between(startDate, evaluationDate)
            }
            HolidayHandlingStrategy.POSTPONE_SCHEDULE -> {
                val holidays = countHolidays(startDate, evaluationDate, criteria.holidayCalendarIds)
                ChronoUnit.DAYS.between(startDate, evaluationDate) - holidays
            }
        }

        val dayInCycle = (daysBetween % criteria.cycleDays).toInt()
        return dayInCycle < criteria.shiftsPerCycle
    }

    private fun isHoliday(date: LocalDate, holidayCalendarIds: Set<Long>): Boolean {
        if (holidayCalendarIds.isEmpty()) return false
        return countHolidays(date, date, holidayCalendarIds) > 0
    }

    private fun countHolidays(startDate: LocalDate, endDate: LocalDate, holidayCalendarIds: Set<Long>): Long {
        if (holidayCalendarIds.isEmpty()) return 0

        val startTimeMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTimeMillis = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val selection = "(${CalendarContract.Events.CALENDAR_ID} IN (${holidayCalendarIds.joinToString(",")})) AND " +
                "(${CalendarContract.Events.DTSTART} < $endTimeMillis) AND (${CalendarContract.Events.DTEND} > $startTimeMillis) AND " +
                "(${CalendarContract.Events.ALL_DAY} = 1)"

        val projection = arrayOf(CalendarContract.Events.DTSTART)
        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        var holidayCount = 0L
        cursor?.use {
            val holidayDates = mutableSetOf<LocalDate>()
            while (it.moveToNext()) {
                val eventStart = Instant.ofEpochMilli(it.getLong(0)).atZone(ZoneId.systemDefault()).toLocalDate()
                if (!eventStart.isBefore(startDate) && !eventStart.isAfter(endDate)) {
                    holidayDates.add(eventStart)
                }
            }
            holidayCount = holidayDates.size.toLong()
        }
        return holidayCount
    }
}
