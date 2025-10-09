package com.gkprojct.clock

import java.util.UUID
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// --- Data Class for a Rule ---
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

// --- Enum for Holiday Handling ---
enum class HolidayHandlingStrategy {
    NORMAL_SCHEDULE,
    POSTPONE_SCHEDULE
}

// --- Sealed Class for Rule Actions ---
sealed class RuleAction {
    object SkipNextAlarm : RuleAction()
    data class AdjustAlarmTime(val newTime: LocalTime) : RuleAction()
}

// --- Sealed Class for Rule Criteria ---
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
        val offDays: Set<Long> = emptySet()
    ) : RuleCriteria()
    data class FreeShift(
        val workDays: Set<Long> = emptySet()
    ) : RuleCriteria()
}

// --- Extension function to get a summary string for the UI ---
fun RuleCriteria.toSummaryString(): String {
    return when (this) {
        is RuleCriteria.AlwaysTrue -> "始终启用"
        is RuleCriteria.IfCalendarEventExists -> {
            val eventType = if (allDay) "全天事件" else "定时事件"
            val keywordsSummary = if (keywords.isNotEmpty()) "关键词: ${keywords.joinToString()}" else "任何事件"
            "日历事件: $eventType, $keywordsSummary"
        }
        is RuleCriteria.BasedOnTime -> {
            val formatter = DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT)
            "时间范围: ${startTime.format(formatter)} - ${endTime.format(formatter)}"
        }
        is RuleCriteria.ShiftWork -> {
            val exceptionSummary = if (offDays.isNotEmpty()) ", ${offDays.size}个例外" else ""
            "轮班制: ${cycleDays}天 / ${shiftsPerCycle}班$exceptionSummary"
        }
        is RuleCriteria.FreeShift -> {
            "自由排班: 已选择 ${workDays.size} 天"
        }
    }
}
