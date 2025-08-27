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
import org.mockito.kotlin.anyOrNull // Correct import
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
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
    fun `evaluateRules with AlwaysTrue returns correct action`() {
        val rule = createTestRule(criteria = RuleCriteria.AlwaysTrue)
        val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.now())
        assertEquals(rule.action, result)
    }

    @Test
    fun `evaluateRules with disabled rule returns null`() {
        val rule = createTestRule(enabled = false, criteria = RuleCriteria.AlwaysTrue)
        val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.now())
        assertNull(result)
    }

    @Test
    fun `evaluateRules with BasedOnTime returns correct action when within time range`() {
        val rule = createTestRule(criteria = RuleCriteria.BasedOnTime(LocalTime.of(8, 0), LocalTime.of(17, 0)))
        val evaluationTime = LocalDateTime.parse("2023-10-27T10:00:00")
        val result = ruleEngine.evaluateRules(listOf(rule), evaluationTime)
        assertEquals(rule.action, result)
    }

    @Test
fun `evaluateRules with IfCalendarEventExists returns correct action for matching event`() {
    val criteria = RuleCriteria.IfCalendarEventExists(keywords = listOf("Work"), timeRangeMinutes = 60, allDay = false)
    val rule = createTestRule(criteria = criteria, calendarIds = setOf(1L))

    val now = Instant.now()
    val eventStart = now.plus(30, ChronoUnit.MINUTES)
    val eventEnd = now.plus(90, ChronoUnit.MINUTES)

    // Create a mock cursor instead of a real MatrixCursor
    val mockCursor = mock(MatrixCursor::class.java)

    // Define the mock behavior
    `when`(mockCursor.moveToNext()).thenReturn(true, false) // moves once, then returns false
    `when`(mockCursor.getLong(0)).thenReturn(eventStart.toEpochMilli()) // DTSTART
    `when`(mockCursor.getLong(1)).thenReturn(eventEnd.toEpochMilli())   // DTEND
    `when`(mockCursor.getInt(2)).thenReturn(0)                          // ALL_DAY
    `when`(mockCursor.getString(3)).thenReturn("Work Meeting")          // TITLE

    `when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(mockCursor)

    val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.now())
    assertEquals(rule.action, result)
}

    @Test
    fun `evaluateRules with ShiftWork returns correct action for work day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        // Add currentShiftIndex
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        //`when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(null)

        val evaluationTime = startDate.plus(1, ChronoUnit.DAYS)
        val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.ofInstant(evaluationTime, java.time.ZoneId.systemDefault()))
        assertEquals(rule.action, result)
    }

    @Test
    fun `evaluateRules with ShiftWork returns null for off day`() {
        val startDate = Instant.now().minus(2, ChronoUnit.DAYS)
        // Add currentShiftIndex
        val criteria = RuleCriteria.ShiftWork(cycleDays = 4, shiftsPerCycle = 2, startDate = startDate.toEpochMilli(), currentShiftIndex = 0)
        val rule = createTestRule(criteria = criteria)

        //`when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(null)

        val evaluationTime = startDate.plus(3, ChronoUnit.DAYS)
        val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.ofInstant(evaluationTime, java.time.ZoneId.systemDefault()))
        assertNull(result)
    }

    @Test
fun `evaluateRules with ShiftWork returns null on a holiday with NORMAL_SCHEDULE`() {
    val today = Instant.now()
    val criteria = RuleCriteria.ShiftWork(
        cycleDays = 4, shiftsPerCycle = 2, startDate = today.toEpochMilli(),
        holidayCalendarIds = setOf(1L), holidayHandling = HolidayHandlingStrategy.NORMAL_SCHEDULE, currentShiftIndex = 0
    )
    val rule = createTestRule(criteria = criteria)

    // Create a mock cursor instead of a real MatrixCursor
    val mockCursor = mock(MatrixCursor::class.java)
    `when`(mockCursor.moveToNext()).thenReturn(true, false)
    `when`(mockCursor.getLong(0)).thenReturn(today.truncatedTo(ChronoUnit.DAYS).toEpochMilli())

    `when`(mockContentResolver.query(any(), any(), any(), any(), anyOrNull())).thenReturn(mockCursor)

    val result = ruleEngine.evaluateRules(listOf(rule), LocalDateTime.ofInstant(today, java.time.ZoneId.systemDefault()))
    assertNull(result)
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
