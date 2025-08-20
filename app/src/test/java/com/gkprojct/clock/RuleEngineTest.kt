package com.gkprojct.clock

import android.content.ContentResolver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.time.LocalTime
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
        val rule = Rule(
            name = "Test",
            description = "",
            enabled = true,
            criteria = RuleCriteria.AlwaysTrue,
            action = RuleAction.SkipNextAlarm
        )
        val result = ruleEngine.evaluate(rule, Instant.now())
        assertTrue(result)
    }

    @Test
    fun `evaluate with disabled rule returns false`() {
        val rule = Rule(
            name = "Test",
            description = "",
            enabled = false,
            criteria = RuleCriteria.AlwaysTrue,
            action = RuleAction.SkipNextAlarm
        )
        val result = ruleEngine.evaluate(rule, Instant.now())
        assertFalse(result)
    }

    @Test
    fun `evaluate with BasedOnTime returns true when within time range`() {
        val rule = Rule(
            name = "Test",
            description = "",
            enabled = true,
            criteria = RuleCriteria.BasedOnTime(LocalTime.of(8, 0), LocalTime.of(17, 0)),
            action = RuleAction.SkipNextAlarm
        )
        val evaluationTime = Instant.parse("2023-10-27T10:00:00Z") // 10:00 AM UTC
        val result = ruleEngine.evaluate(rule, evaluationTime)
        assertTrue(result)
    }

    @Test
    fun `evaluate with BasedOnTime returns false when outside time range`() {
        val rule = Rule(
            name = "Test",
            description = "",
            enabled = true,
            criteria = RuleCriteria.BasedOnTime(LocalTime.of(8, 0), LocalTime.of(17, 0)),
            action = RuleAction.SkipNextAlarm
        )
        val evaluationTime = Instant.parse("2023-10-27T18:00:00Z") // 6:00 PM UTC
        val result = ruleEngine.evaluate(rule, evaluationTime)
        assertFalse(result)
    }

    // TODO: Add more tests for IfCalendarEventExists and ShiftWork with mocked ContentResolver
}
