package com.gkprojct.clock

import java.util.UUID
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Rule(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val targetAlarmIds: Set<UUID> = emptySet(),
    val calendarIds: Set<Long> = emptySet(),
    val criteria: RuleCriteria = RuleCriteria.AlwaysTrue,
    val action: RuleAction = RuleAction.SkipNextAlarm
)

enum class HolidayHandlingStrategy {
    NORMAL_SCHEDULE,
    POSTPONE_SCHEDULE
}

sealed class RuleAction {
    object SkipNextAlarm : RuleAction()
    data class AdjustAlarmTime(val newTime: LocalTime) : RuleAction()
}

sealed class RuleCriteria {
    object AlwaysTrue : RuleCriteria()
    data class IfCalendarEventExists(
        val keywords: List<String>,
        val timeRangeMinutes: Int,
        val allDay: Boolean
    ) : RuleCriteria()
    data class BasedOnTime(val startTime: LocalTime, val endTime: LocalTime) : RuleCriteria()
    data class ShiftWork(
        val cycleDays: Int,
        val shiftsPerCycle: Int,
        val startDate: Long,
        val currentShiftIndex: Int,
        val holidayCalendarIds: Set<Long> = emptySet(),
        val holidayHandling: HolidayHandlingStrategy = HolidayHandlingStrategy.NORMAL_SCHEDULE,
        val offDays: Set<Long> = emptySet() // Safe exception for off-days
    ) : RuleCriteria()
    data class FreeShift(
        val workDays: Set<Long> = emptySet()
    ) : RuleCriteria()
}

fun RuleCriteria.toSummaryString(): String {
    return when (this) {
        is RuleCriteria.AlwaysTrue -> "Always enabled"
        is RuleCriteria.IfCalendarEventExists -> {
            val eventType = if (allDay) "all-day event" else "timed event"
            val keywordsSummary = if (keywords.isNotEmpty()) "keywords: ${keywords.joinToString()}" else "any event"
            "Calendar event: $eventType, $keywordsSummary"
        }
        is RuleCriteria.BasedOnTime -> {
            val formatter = DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT)
            "Time range: ${startTime.format(formatter)} - ${endTime.format(formatter)}"
        }
        is RuleCriteria.ShiftWork -> {
            val exceptionSummary = if (offDays.isNotEmpty()) ", ${offDays.size} off-days" else ""
            "Periodic Shift: ${cycleDays} days / ${shiftsPerCycle} shifts$exceptionSummary"
        }
        is RuleCriteria.FreeShift -> {
            "Free Shift: ${workDays.size} work days"
        }
    }
}