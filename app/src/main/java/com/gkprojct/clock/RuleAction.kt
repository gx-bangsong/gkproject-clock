package com.gkprojct.clock

import java.time.LocalTime

sealed class RuleAction {
    object SkipNextAlarm : RuleAction()
    data class AdjustAlarmTime(val newTime: LocalTime) : RuleAction()
}
