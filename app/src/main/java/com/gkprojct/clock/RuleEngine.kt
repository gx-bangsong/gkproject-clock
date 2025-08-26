package com.gkprojct.clock

import java.time.LocalDateTime

/**
 * A simple representation of a calendar event for the RuleEngine to use.
 * In a real app, this would be a more complex object fetched from the device's calendar provider.
 */
data class CalendarEvent(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean
)

/**
 * The RuleEngine is responsible for evaluating a set of rules against the current system state
 * and determining which action, if any, should be taken.
 */
class RuleEngine {

    /**
     * Evaluates a list of rules against the current context.
     *
     * @param rules The list of rules to evaluate.
     * @param currentTime The current time to check against.
     * @param events A list of calendar events for the relevant period.
     * @return The `RuleAction` from the first matching, enabled rule, or null if no rules match.
     */
    fun evaluateRules(
        rules: List<Rule>,
        currentTime: LocalDateTime,
        events: List<CalendarEvent>
    ): RuleAction? {
        val activeRules = rules.filter { it.enabled }

        for (rule in activeRules) {
            val criteria = rule.criteria
            val isMatch = when (criteria) {
                is RuleCriteria.AlwaysTrue -> true
                is RuleCriteria.BasedOnTime -> {
                    val time = currentTime.toLocalTime()
                    !time.isBefore(criteria.startTime) && !time.isAfter(criteria.endTime)
                }
                is RuleCriteria.IfCalendarEventExists -> {
                    events.any { event ->
                        // Check if event is in a calendar targeted by the rule
                        // (We don't have calendar IDs on our dummy events, so we skip this check)
                        // and event matches the criteria
                        val keywordsMatch = criteria.keywords.isEmpty() || criteria.keywords.any { keyword -> event.title.contains(keyword, ignoreCase = true) }
                        val timeMatch = if (criteria.allDay) {
                            event.isAllDay
                        } else {
                            val ruleRangeStart = currentTime.minusMinutes(criteria.timeRangeMinutes.toLong())
                            val ruleRangeEnd = currentTime.plusMinutes(criteria.timeRangeMinutes.toLong())
                            !event.startTime.isAfter(ruleRangeEnd) && !event.endTime.isBefore(ruleRangeStart)
                        }
                        keywordsMatch && timeMatch
                    }
                }
                is RuleCriteria.ShiftWork -> {
                    // This is a simplified example. A real implementation would be more complex.
                    val startDate = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(criteria.startDate), java.time.ZoneId.systemDefault()).toLocalDate()
                    val today = currentTime.toLocalDate()
                    val daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(startDate, today)
                    val dayInCycle = (daysSinceStart % criteria.cycleDays).toInt()
                    dayInCycle < criteria.shiftsPerCycle // True if it's a "work" day
                }
            }
            if (isMatch) {
                return rule.action
            }
        }
        return null
    }
}
