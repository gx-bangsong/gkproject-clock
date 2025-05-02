package com.gkprojct.clock.vm // <-- **确保包路径正确**

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// --- 导入 RuleCriteria (从 com.gkprojct.clock 包导入) ---
import com.gkprojct.clock.RuleCriteria
// -----------------------------------------------------


// --- Room Entity for Rule Persistence ---
// 这是一个用于 Room 数据库的规则实体
@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: UUID,
    val name: String,
    val description: String,
    val enabled: Boolean,
    val targetAlarmIds: Set<UUID>,
    val calendarIds: Set<Long>,
    val criteria: RuleCriteria // <-- **确保 RuleEntity 包含 criteria 字段**
)
// ------------------------------------
