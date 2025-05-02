package com.gkprojct.clock // <-- 确保包路径正确

// --- 导入 RuleEntity 和 RuleDao (从 com.gkprojct.clock.vm 包导入) ---
// ---------------------------------------------------------------

// --- 导入 Rule (从 com.gkprojct.clock 包导入) ---
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gkprojct.clock.vm.RuleDao
import com.gkprojct.clock.vm.RuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

// ----------------------------------------------


// Rule ViewModel，用于管理规则相关的 UI 数据和业务逻辑
class RuleViewModel(private val ruleDao: RuleDao) : ViewModel() {

    // Flow of all rules from the database, collected as State in Composable
    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()

    // 保存规则 (插入或更新)
    fun saveRule(rule: Rule) {
        // 在保存到数据库前，将 Rule UI 模型转换为 RuleEntity
        val ruleEntity = RuleEntity(
            id = rule.id,
            name = rule.name,
            description = rule.description,
            enabled = rule.enabled,
            targetAlarmIds = rule.targetAlarmIds,
            calendarIds = rule.calendarIds,
            criteria = rule.criteria // <-- **确保这里引用的 RuleEntity 有 criteria 参数**
        )
        viewModelScope.launch {
            // Room 会根据主键自动判断是插入还是更新
            ruleDao.insertRule(ruleEntity) // 使用 insertRule，Room 会处理 REPLACE 策略
        }
    }

    // 删除规则
    fun deleteRule(rule: Rule) {
        // 在删除前，将 Rule UI 模型转换为 RuleEntity
        val ruleEntity = RuleEntity(
            id = rule.id,
            name = rule.name, // 需要其他字段来构建 RuleEntity，即使只用 ID 删除
            description = rule.description,
            enabled = rule.enabled,
            targetAlarmIds = rule.targetAlarmIds,
            calendarIds = rule.calendarIds,
            criteria = rule.criteria // <-- **确保这里引用的 RuleEntity 有 criteria 参数**
        )
        viewModelScope.launch {
            ruleDao.deleteRule(ruleEntity)
        }
    }

    // 根据 ID 获取规则
    suspend fun getRuleById(ruleId: UUID): RuleEntity? {
        // 直接从 DAO 获取 RuleEntity
        return ruleDao.getRuleById(ruleId)
    }

    // TODO: Add other ViewModel methods as needed (e.g., filtering rules, triggering rule logic)
}

// Rule ViewModel Factory，用于创建 RuleViewModel实例
class RuleViewModelFactory(private val ruleDao: RuleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RuleViewModel(ruleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
