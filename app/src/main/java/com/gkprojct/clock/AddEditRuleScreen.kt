package com.gkprojct.clock // <-- 确保包路径正确


// --- 导入 Rule 和 RuleCriteria (从 com.gkprojct.clock 包导入) ---
// -------------------------------------------------------------

// --- 导入 RuleEntity 和 RuleDao (从 com.gkprojct.clock.vm 包导入) ---
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gkprojct.clock.vm.RuleDao
import com.gkprojct.clock.vm.RuleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// -----------------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    // 如果是编辑模式，传递要编辑的规则；如果是添加模式，传递 null
    initialRule: Rule? = null,
    onSaveRule: (Rule) -> Unit, // 保存规则时的回调，传递保存后的规则对象 (此回调现在主要用于导航)
    onCancel: () -> Unit, // 取消操作时的回调
    ruleViewModel: RuleViewModel, // ViewModel
    onSelectCalendarsClick: (Set<Long>) -> Unit, // 点击选择日历时触发，传递当前已选中的日历 ID 集合
    onSelectAlarmsClick: (Set<UUID>) -> Unit, // 点击选择应用闹钟时触发，传递当前已选中的闹钟 ID 集合
    onDefineCriteriaClick: (RuleCriteria) -> Unit // <-- **新增回调：点击定义规则条件时触发，传递当前规则条件**
) {
    // 状态：当前正在编辑的规则数据
    var ruleName by remember { mutableStateOf(initialRule?.name ?: "") }
    var ruleDescription by remember { mutableStateOf(initialRule?.description ?: "") }
    var ruleEnabled by remember { mutableStateOf(initialRule?.enabled ?: true) }
    // 当前规则已选中的日历 ID 集合 (Keep this)
    var selectedCalendarIds by remember { mutableStateOf(initialRule?.calendarIds ?: emptySet()) }
    // 当前规则已选择的应用闹钟 ID 集合 (Keep this)
    var selectedAlarmIds by remember { mutableStateOf(initialRule?.targetAlarmIds ?: emptySet()) }
    // --- 新增状态：当前规则条件 ---
    var ruleCriteria by remember { mutableStateOf<RuleCriteria>(initialRule?.criteria ?: RuleCriteria.AlwaysTrue) }
    // -----------------------------


    // 判断是添加模式还是编辑模式 (Keep this)
    val isEditing = initialRule != null

    LaunchedEffect(initialRule) {
        if (isEditing) {
            // 从 ViewModel 加载 RuleEntity，并更新 UI 状态
            val loadedRuleEntity = ruleViewModel.getRuleById(initialRule.id)
            loadedRuleEntity?.let {
                ruleName = it.name
                ruleDescription = it.description
                ruleEnabled = it.enabled
                selectedCalendarIds = it.calendarIds
                selectedAlarmIds = it.targetAlarmIds
                ruleCriteria = it.criteria // <-- 从 RuleEntity 加载 criteria
            }
        } else {
            // 添加模式，重置状态
            ruleName = ""
            ruleDescription = ""
            ruleEnabled = true
            selectedCalendarIds = emptySet()
            selectedAlarmIds = emptySet()
            ruleCriteria = RuleCriteria.AlwaysTrue
        }
    }

    val coroutineScope = rememberCoroutineScope()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑规则" else "添加规则") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "取消")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val ruleToSave = initialRule?.copy(
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            calendarIds = selectedCalendarIds,
                            targetAlarmIds = selectedAlarmIds,
                            criteria = ruleCriteria
                        ) ?: Rule(
                            id = UUID.randomUUID(),
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            targetAlarmIds = selectedAlarmIds,
                            calendarIds = selectedCalendarIds,
                            criteria = ruleCriteria
                        )
                        // Saving the rule is now handled by the onSaveRule lambda provided by MainActivity
                        onSaveRule(ruleToSave)
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存规则")
                    }

                    if (isEditing) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                ruleViewModel.deleteRule(initialRule) // Delete using Rule object
                                onCancel()
                            }
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除规则")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = ruleName,
                onValueChange = { ruleName = it },
                label = { Text("规则名称") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = ruleDescription,
                onValueChange = { ruleDescription = it },
                label = { Text("规则描述") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("启用规则")
                Switch(
                    checked = ruleEnabled,
                    onCheckedChange = { ruleEnabled = it }
                )
            }
            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectCalendarsClick(selectedCalendarIds)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("选择日历")
                    Text(
                        text = "已选择 ${selectedCalendarIds.size} 个日历",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "选择日历",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectAlarmsClick(selectedAlarmIds)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("选择应用闹钟")
                    Text(
                        text = "已选择 ${selectedAlarmIds.size} 个闹钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "选择应用闹钟",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            // --- 添加定义规则条件的 UI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDefineCriteriaClick(ruleCriteria)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("规则条件")
                    // --- 显示规则条件摘要 ---
                    // 调用 Rule.kt 中定义的扩展函数
                    Text(
                        text = ruleCriteria.toSummaryString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // -----------------------
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "定义规则条件",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // --------------------------

            // TODO: Add Save and Cancel buttons at the bottom if not in TopAppBar
        }
    }
}

// --- REMOVE THE DUPLICATE toSummaryString() FUNCTION FROM HERE ---
// fun RuleCriteria.toSummaryString(): String { ... }
// -------------------------------------------------------------


// --- Preview (更新以使用 CriteriaTypeInfo) ---
@Preview(showBackground = true)
@Composable
fun AddEditRuleScreenPreviewAdd() {
    MaterialTheme {
        val sampleRuleDao = object : RuleDao { // Use vm package RuleDao
            private val sampleRulesFlow = MutableStateFlow(emptyList<RuleEntity>()) // Use vm package RuleEntity
            override fun getAllRules(): Flow<List<RuleEntity>> = sampleRulesFlow
            override suspend fun getRuleById(ruleId: UUID): RuleEntity? = null
            override suspend fun insertRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun updateRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRuleById(ruleId: UUID) { /* no-op */ }
        }
        val sampleViewModel = RuleViewModel(sampleRuleDao) // Use vm package RuleViewModel

        AddEditRuleScreen(
            initialRule = null, // 添加模式预览
            onSaveRule = { rule -> println("Preview: Save New Rule: ${rule.name}") },
            onCancel = { println("Preview: Cancel Add Rule") },
            ruleViewModel = sampleViewModel,
            onSelectCalendarsClick = { selectedIds -> println("Preview: Select Calendars Clicked with IDs: $selectedIds") },
            onSelectAlarmsClick = { selectedIds -> println("Preview: Select Alarms Clicked with IDs: $selectedIds") },
            onDefineCriteriaClick = { criteria -> println("Preview: Define Criteria Clicked with criteria: $criteria") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditRuleScreenPreviewEdit() {
    MaterialTheme {
        val sampleRule = Rule(
            id = UUID.randomUUID(),
            name = "示例编辑规则 (Preview)",
            description = "这是一个用于编辑模式的示例规则。",
            enabled = false,
            targetAlarmIds = setOf(UUID.randomUUID(), UUID.randomUUID()),
            calendarIds = setOf(1L, 2L, 3L),
            criteria = RuleCriteria.BasedOnTime(java.time.LocalTime.of(7, 0), java.time.LocalTime.of(9, 30)) // 示例规则条件为基于时间
        )

        val sampleRuleDao = object : RuleDao { // Use vm package RuleDao
            private val sampleRulesFlow = MutableStateFlow(listOf(
                RuleEntity(sampleRule.id, sampleRule.name, sampleRule.description, sampleRule.enabled, sampleRule.targetAlarmIds, sampleRule.calendarIds, sampleRule.criteria) // Use vm package RuleEntity
            ))
            override fun getAllRules(): Flow<List<RuleEntity>> = sampleRulesFlow
            override suspend fun getRuleById(ruleId: UUID): RuleEntity? = sampleRulesFlow.value.find { it.id == ruleId }
            override suspend fun insertRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun updateRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRuleById(ruleId: UUID) { /* no-op */ }
        }
        val sampleViewModel = RuleViewModel(sampleRuleDao) // Use vm package RuleViewModel

        AddEditRuleScreen(
            initialRule = sampleRule,
            onSaveRule = { rule -> println("Preview: Save Edited Rule: ${rule.name}") },
            onCancel = { println("Preview: Cancel Edit Rule") },
            ruleViewModel = sampleViewModel,
            onSelectCalendarsClick = { selectedIds -> println("Preview: Select Calendars Clicked with IDs: $selectedIds") },
            onSelectAlarmsClick = { selectedIds -> println("Preview: Select Alarms Clicked with IDs: $selectedIds") },
            onDefineCriteriaClick = { criteria -> println("Preview: Define Criteria Clicked with criteria: $criteria") }
        )
    }
}
