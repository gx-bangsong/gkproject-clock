package com.gkprojct.clock

import android.content.ContentResolver
import android.database.MatrixCursor
import android.provider.CalendarContract
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class RuleEngineTest {

    @Mock
    private lateinit var mockContentResolver: ContentResolver

    private lateinit var ruleEngine: RuleEngine

    @Before
    fun setup() {
        ruleEngine = RuleEngine(mockContentResolver)
    }

    @Test
    fun `evaluate with AlwaysTrue returns true`() {
        val rule = createTestRule(criteria = RuleCriteria.AlwaysTrue)
        assertTrue(ruleEngine.evaluate(rule, Instant.now()))
    }

    @Test
    fun `evaluate with disabled rule returns false`() {
        val rule = createTestRule(enabled = false, criteria = RuleCriteria.AlwaysTrue)
        assertFalse(ruleEngine.evaluate(rule, Instant.now()))
    }

    @Test
    fun `evaluate with BasedOnTime returns true when within time range`() {
        val rule = createTestRule(criteria = RuleCriteria.BasedOnTime(LocalTime.of(8, 0), LocalTime.of(17, 0)))
        val evaluationTime = Instant.parse("2023-10-27T10:00:00Z")
        assertTrue(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with IfCalendarEventExists returns true for matching event`() {
        val criteria = RuleCriteria.IfCalendarEventExists(keywords = listOf("Work"), timeRangeMinutes = 60, allDay = false)
        val rule = createTestRule(criteria = criteria, calendarIds = setOf(1L))

        val now = Instant.now()
        val eventStart = now.plus(30, ChronoUnit.MINUTES)
        val eventEnd = now.plus(90, ChronoUnit.MINUTES)

        val cursor = MatrixCursor(arrayOf(CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND, CalendarContract.Events.ALL_DAY, CalendarContract.Events.TITLE))
        cursor.addRow(arrayOf(eventStart.toEpochMilli(), eventEnd.toEpochMilli(), 0, "Work Meeting"))

        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)

        assertTrue(ruleEngine.evaluate(rule, now))
    }

    @Test
    fun `evaluate with ShiftWork returns true for work day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        // Mock no holidays
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(null)

        // Evaluation date is 2 days after start date, which is day 2 of a 4-day cycle.
        // Since shiftsPerCycle is 2, day 0 and 1 are work days. Day 2 is an off day.
        // Let's evaluate for day 1
        val evaluationTime = startDate.plus(1, ChronoUnit.DAYS)
        assertTrue(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with ShiftWork returns false for off day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(null)

        val evaluationTime = startDate.plus(3, ChronoUnit.DAYS) // Day 3 is an off day
        assertFalse(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with ShiftWork returns false on a holiday with NORMAL_SCHEDULE`() {
        val today = Instant.now()
        val criteria = RuleCriteria.ShiftWork(
            cycleDays = 4, shiftsPerCycle = 2, startDate = today.toEpochMilli(), currentShiftIndex = 0,
            holidayCalendarIds = setOf(1L), holidayHandling = HolidayHandlingStrategy.NORMAL_SCHEDULE
        )
        val rule = createTestRule(criteria = criteria, calendarIds = setOf(1L))

        val cursor = MatrixCursor(arrayOf(CalendarContract.Events.DTSTART))
        cursor.addRow(arrayOf(today.truncatedTo(ChronoUnit.DAYS).toEpochMilli()))
        `when`(mockContentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)

        assertFalse(ruleEngine.evaluate(rule, today))
    }

    private fun createTestRule(enabled: Boolean = true, criteria: RuleCriteria, calendarIds: Set<Long> = emptySet()): Rule {
        return Rule(
            id = UUID.randomUUID(),
            name = "Test Rule",
            description = "Test Description",
            enabled = enabled,
            targetAlarmIds = setOf(UUID.randomUUID()),
            calendarIds = calendarIds,
            criteria = criteria,
            action = RuleAction.SkipNextAlarm
        )
    }
}
