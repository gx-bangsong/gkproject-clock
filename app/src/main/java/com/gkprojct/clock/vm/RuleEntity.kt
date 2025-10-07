package com.gkprojct.clock.vm

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gkprojct.clock.RuleCriteria
import java.util.UUID

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val targetAlarmIds: Set<UUID>,
    val calendarIds: Set<Long>,
    val criteria: RuleCriteria
)