package com.gkprojct.clock // <-- Ensure package path is correct

import java.util.UUID
import java.time.LocalTime // Import LocalTime
import java.time.DayOfWeek // Import DayOfWeek
import java.time.format.DateTimeFormatter // Import DateTimeFormatter
import java.time.format.TextStyle // Import TextStyle
import java.util.Locale // Import Locale
import kotlin.collections.Set // Import Set explicitly if needed, though usually inferred

// --- Import Alarm from the correct file ---
// Assuming Alarm is defined in AlarmScreen.kt
import com.gkprojct.clock.Alarm


// --- 定义规则的数据结构 (UI Model) ---
// 这是一个用于 UI 层的规则数据模型
data class Rule(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val enabled: Boolean = true,
    val targetAlarmIds: Set<UUID> = emptySet(),
    val calendarIds: Set<Long> = emptySet(),
    val criteria: RuleCriteria = RuleCriteria.AlwaysTrue,
    val action: RuleAction = RuleAction.SkipNextAlarm // Default action
)

// 规则判断条件的密封类
sealed class RuleCriteria {
    object AlwaysTrue : RuleCriteria()
    data class IfCalendarEventExists(
        val keywords: List<String>,
        val timeRangeMinutes: Int,
        val allDay: Boolean = false // false for specific time, true for all-day event
    ) : RuleCriteria()
    data class BasedOnTime(val startTime: LocalTime, val endTime: LocalTime) : RuleCriteria()
enum class HolidayHandlingStrategy {
    NORMAL_SCHEDULE, // 假日后依然按照排班规则正常排班
    POSTPONE_SCHEDULE // 假日后按照继续假日前一天的排班
}

    data class ShiftWork(
        val cycleDays: Int, // e.g., 4 days
        val shiftsPerCycle: Int, // e.g., 2 shifts
        val startDate: Long, // Start date of the cycle in millis
        val currentShiftIndex: Int,
        val holidayCalendarIds: Set<Long> = emptySet(),
        val holidayHandling: HolidayHandlingStrategy = HolidayHandlingStrategy.NORMAL_SCHEDULE
    ) : RuleCriteria()
    // TODO: Add other specific criteria types
}

// --- 辅助函数：将 RuleCriteria 转换为摘要字符串 ---
// This extension function should be defined here with the sealed class
fun RuleCriteria.toSummaryString(): String {
    return when (this) {
        is RuleCriteria.AlwaysTrue -> "始终启用"
        is RuleCriteria.IfCalendarEventExists -> {
            val keywordsSummary = if (keywords.isNotEmpty()) "包含关键词: ${keywords.joinToString(", ")}" else "无关键词"
            val timeRangeSummary = if (timeRangeMinutes > 0) "，时间范围: ${timeRangeMinutes} 分钟" else ""
            "基于日历事件 ($keywordsSummary$timeRangeSummary)"
        }
        is RuleCriteria.BasedOnTime -> {
            "基于时间 (${startTime} - ${endTime})"
        }
        // TODO: Add summary for other criteria types
        else -> "未知条件"
    }
}
