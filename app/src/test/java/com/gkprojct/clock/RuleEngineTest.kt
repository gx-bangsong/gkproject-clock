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
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import java.time.Instant
import java.time.LocalDateTime
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

    // --- Corrected Tests ---
    @Test
    fun `evaluate with AlwaysTrue returns true`() {
        val rule = createTestRule(criteria = RuleCriteria.AlwaysTrue)
        val evaluationTime = Instant.now()
        assertTrue(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with disabled rule returns false`() {
        val rule = createTestRule(enabled = false, criteria = RuleCriteria.AlwaysTrue)
        val evaluationTime = Instant.now()
        assertFalse(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with BasedOnTime returns true when within time range`() {
        val rule = createTestRule(criteria = RuleCriteria.BasedOnTime(LocalTime.of(8, 0), LocalTime.of(17, 0)))
        // Convert LocalDateTime to Instant for the evaluation time
        val evaluationTime = LocalDateTime.parse("2023-10-27T10:00:00").atZone(ZoneId.systemDefault()).toInstant()
        assertTrue(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with IfCalendarEventExists returns true for matching event`() {
        val criteria = RuleCriteria.IfCalendarEventExists(keywords = listOf("Work"), timeRangeMinutes = 60, allDay = false)
        val rule = createTestRule(criteria = criteria, calendarIds = setOf(1L))

        val now = Instant.now()
        val eventStart = now.plus(30, ChronoUnit.MINUTES)
        val eventEnd = now.plus(90, ChronoUnit.MINUTES)

        val mockCursor = mock(MatrixCursor::class.java)

        `when`(mockCursor.moveToNext()).thenReturn(true, false)
        `when`(mockCursor.getLong(0)).thenReturn(eventStart.toEpochMilli())
        `when`(mockCursor.getLong(1)).thenReturn(eventEnd.toEpochMilli())
        `when`(mockCursor.getInt(2)).thenReturn(0)
        `when`(mockCursor.getString(3)).thenReturn("Work Meeting")

        `when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(mockCursor)

        assertTrue(ruleEngine.evaluate(rule, now))
    }

    @Test
    fun `evaluate with ShiftWork returns true for work day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        val evaluationTime = startDate.plus(1, ChronoUnit.DAYS)
        assertTrue(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with ShiftWork returns false for off day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        val evaluationTime = startDate.plus(3, ChronoUnit.DAYS)
        assertFalse(ruleEngine.evaluate(rule, evaluationTime))
    }

    @Test
    fun `evaluate with ShiftWork returns false on a holiday with NORMAL_SCHEDULE`() {
        val today = Instant.now()
        val criteria = RuleCriteria.ShiftWork(
            cycleDays = 4, shiftsPerCycle = 2, startDate = today.toEpochMilli(),
            holidayCalendarIds = setOf(1L), holidayHandling = HolidayHandlingStrategy.NORMAL_SCHEDULE, currentShiftIndex = 0
        )
        val rule = createTestRule(criteria = criteria)

        val mockCursor = mock(MatrixCursor::class.java)
        `when`(mockCursor.moveToNext()).thenReturn(true, false)
        `when`(mockCursor.getLong(0)).thenReturn(today.truncatedTo(ChronoUnit.DAYS).toEpochMilli())

        `when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(mockCursor)

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
