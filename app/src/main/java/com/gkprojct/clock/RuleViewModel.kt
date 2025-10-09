package com.gkprojct.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gkprojct.clock.vm.RuleDao
import com.gkprojct.clock.vm.RuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

// Rule ViewModel、UIのルール関連データとビジネスロジックを管理
class RuleViewModel(private val ruleDao: RuleDao) : ViewModel() {

    // データベースからのすべてのルールのFlow、ComposableでStateとして収集
    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()

    val allRulesAsUiModel: Flow<List<Rule>> = allRules.map { ruleEntities ->
        ruleEntities.map { entity ->
            // ここで RuleEntity を Rule UIモデルにマッピング
            // allDay パラメータは Rule.kt の RuleCriteria.IfCalendarEventExists で定義
            // マッピング時に allDay を設定
            val criteria = when (entity.criteria) {
            is RuleCriteria.IfCalendarEventExists -> entity.criteria.copy(allDay = entity.criteria.allDay) // The fix was already suggested here
                else -> entity.criteria
            }
            Rule(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                enabled = entity.enabled,
                targetAlarmIds = entity.targetAlarmIds,
                calendarIds = entity.calendarIds,
                criteria = criteria,
                action = entity.action
            )
        }
    }

    // ルールを保存 (挿入または更新)
    fun saveRule(rule: Rule) {
        val ruleEntity = RuleEntity(
            id = rule.id,
            name = rule.name,
            description = rule.description,
            enabled = rule.enabled,
            targetAlarmIds = rule.targetAlarmIds,
            calendarIds = rule.calendarIds,
            criteria = rule.criteria,
            action = rule.action
        )
        viewModelScope.launch {
            ruleDao.insertRule(ruleEntity)
        }
    }

    // ルールを削除
    fun deleteRule(ruleId: UUID) {
        viewModelScope.launch {
            ruleDao.deleteRuleById(ruleId)
        }
    }

    // IDでルールを取得
    suspend fun getRuleById(ruleId: UUID): RuleEntity? {
        return ruleDao.getRuleById(ruleId)
    }
}

// Rule ViewModel Factory、RuleViewModelインスタンスを作成
class RuleViewModelFactory(private val ruleDao: RuleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RuleViewModel(ruleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
