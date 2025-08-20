package com.gkprojct.clock.vm

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.util.UUID

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: UUID,
    val timeInMillis: Long,
    val label: String?,
    val isEnabled: Boolean,
    val repeatingDays: Set<DayOfWeek>,
    val sound: String,
    val vibrate: Boolean,
    val appliedRules: List<String>
)
