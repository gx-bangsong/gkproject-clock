package com.gkprojct.clock.vm

import com.gkprojct.clock.HolidayHandlingStrategy
import com.gkprojct.clock.RuleAction
import com.gkprojct.clock.RuleCriteria
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime
import java.util.*

class RuleConvertersTest {

    private val converters = RuleConverters()

    @Test
    fun `converts AlwaysTrue criteria`() {
        val criteria = RuleCriteria.AlwaysTrue
        val json = converters.fromRuleCriteria(criteria)
        val result = converters.toRuleCriteria(json)
        assertEquals(criteria, result)
    }

    @Test
    fun `converts IfCalendarEventExists criteria`() {
        val criteria = RuleCriteria.IfCalendarEventExists(listOf("work", "meeting"), 60, true)
        val json = converters.fromRuleCriteria(criteria)
        val result = converters.toRuleCriteria(json)
        assertEquals(criteria, result)
    }

    @Test
    fun `converts BasedOnTime criteria`() {
        val criteria = RuleCriteria.BasedOnTime(LocalTime.of(9, 0), LocalTime.of(17, 30))
        val json = converters.fromRuleCriteria(criteria)
        val result = converters.toRuleCriteria(json)
        assertEquals(criteria, result)
    }

    @Test
    fun `converts ShiftWork criteria`() {
        val criteria = RuleCriteria.ShiftWork(4, 2, System.currentTimeMillis(), 0, setOf(1L, 2L), HolidayHandlingStrategy.POSTPONE_SCHEDULE)
        val json = converters.fromRuleCriteria(criteria)
        val result = converters.toRuleCriteria(json)
        assertEquals(criteria, result)
    }

    @Test
    fun `converts SkipNextAlarm action`() {
        val action = RuleAction.SkipNextAlarm
        val json = converters.fromRuleAction(action)
        val result = converters.toRuleAction(json)
        assertEquals(action, result)
    }

    @Test
    fun `converts AdjustAlarmTime action`() {
        val action = RuleAction.AdjustAlarmTime(LocalTime.of(7, 45))
        val json = converters.fromRuleAction(action)
        val result = converters.toRuleAction(json)
        assertEquals(action, result)
    }

    @Test
    fun `converts uuid set`() {
        val set = setOf(UUID.randomUUID(), UUID.randomUUID())
        val json = converters.fromUuidSet(set)
        val result = converters.toUuidSet(json)
        assertEquals(set, result)
    }

    @Test
    fun `converts long set`() {
        val set = setOf(1L, 12345L, 987654321L)
        val json = converters.fromLongSet(set)
        val result = converters.toLongSet(json)
        assertEquals(set, result)
    }
}
