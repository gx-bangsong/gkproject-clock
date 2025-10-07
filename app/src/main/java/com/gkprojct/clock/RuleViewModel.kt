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

class RuleViewModel(private val ruleDao: RuleDao) : ViewModel() {

    val allRules: Flow<List<RuleEntity>> = ruleDao.getAllRules()

    val allRulesAsUiModel: Flow<List<Rule>> = allRules.map { ruleEntities ->
        ruleEntities.map { entity ->
            Rule(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                enabled = entity.enabled,
                targetAlarmIds = entity.targetAlarmIds,
                calendarIds = entity.calendarIds,
                criteria = entity.criteria
            )
        }
    }

    fun saveRule(rule: Rule) {
        val ruleEntity = RuleEntity(
            id = rule.id,
            name = rule.name,
            description = rule.description,
            enabled = rule.enabled,
            targetAlarmIds = rule.targetAlarmIds,
            calendarIds = rule.calendarIds,
            criteria = rule.criteria
        )
        viewModelScope.launch {
            ruleDao.insertRule(ruleEntity)
        }
    }

    fun deleteRule(rule: Rule) {
        val ruleEntity = RuleEntity(
            id = rule.id,
            name = rule.name,
            description = rule.description,
            enabled = rule.enabled,
            targetAlarmIds = rule.targetAlarmIds,
            calendarIds = rule.calendarIds,
            criteria = rule.criteria
        )
        viewModelScope.launch {
            ruleDao.deleteRule(ruleEntity)
        }
    }

    suspend fun getRuleById(ruleId: UUID): RuleEntity? {
        return ruleDao.getRuleById(ruleId)
    }
}

class RuleViewModelFactory(private val ruleDao: RuleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RuleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RuleViewModel(ruleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}