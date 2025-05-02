package com.gkprojct.clock.vm // <-- **确保包路径正确**

// --- 导入 RuleEntity (从同一个包导入) ---
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// ---------------------------------------

// Room DAO (Data Access Object) 来定义数据库操作
@Dao
interface RuleDao {

    // 获取所有规则，并以 Flow 的形式发出，以便在数据变化时自动更新 UI
    @Query("SELECT * FROM rules ORDER BY name ASC") // 按名称升序排序
    fun getAllRules(): Flow<List<RuleEntity>>

    // 根据 ID 获取单个规则
    @Query("SELECT * FROM rules WHERE id = :ruleId LIMIT 1")
    suspend fun getRuleById(ruleId: UUID): RuleEntity?

    // 插入新规则
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 如果存在相同主键的规则，则替换
    suspend fun insertRule(rule: RuleEntity)

    // 更新现有规则
    @Update
    suspend fun updateRule(rule: RuleEntity)

    // 删除规则
    @Delete
    suspend fun deleteRule(rule: RuleEntity)

    // 根据 ID 删除规则
    @Query("DELETE FROM rules WHERE id = :ruleId")
    suspend fun deleteRuleById(ruleId: UUID)

    // TODO: Add more query methods as needed (e.g., get enabled rules, get rules for a specific calendar)
}
