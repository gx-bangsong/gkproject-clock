package com.gkprojct.clock // <-- 确保包路径正确

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow


// --- 导入 Rule 和 RuleCriteria (从 com.gkprojct.clock 包导入) ---
import com.gkprojct.clock.Rule
import com.gkprojct.clock.RuleCriteria
// -------------------------------------------------------------

// --- 导入 RuleEntity 和 RuleDao (从 com.gkprojct.clock.vm 包导入) ---
import com.gkprojct.clock.vm.RuleEntity
import com.gkprojct.clock.vm.RuleDao
import com.gkprojct.clock.RuleViewModel // 导入 ViewModel
import kotlinx.coroutines.flow.Flow

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
    onSelectAlarmsClick: (Set<UUID>) -> Unit, // <-- **新增回调：点击选择应用闹钟时触发，传递当前已选中的闹钟 ID 集合**
    onDefineCriteriaClick: (RuleCriteria) -> Unit // <-- **新增回调：点击定义规则条件时触发，传递当前规则条件**
) {
    // 状态：当前正在编辑的规则数据
    var ruleName by remember { mutableStateOf(initialRule?.name ?: "") }
    var ruleDescription by remember { mutableStateOf(initialRule?.description ?: "") }
    var ruleEnabled by remember { mutableStateOf(initialRule?.enabled ?: true) }
    // 当前规则已选中的日历 ID 集合 (Keep this)
    var selectedCalendarIds by remember { mutableStateOf(initialRule?.calendarIds ?: emptySet()) }
    // --- 新增状态：当前规则已选择的应用闹钟 ID 集合 ---
    var selectedAlarmIds by remember { mutableStateOf(initialRule?.targetAlarmIds ?: emptySet()) }
    // ------------------------------------------------
    // --- 新增状态：当前规则条件 ---
    var ruleCriteria by remember { mutableStateOf<RuleCriteria>(initialRule?.criteria ?: RuleCriteria.AlwaysTrue) }
    // -----------------------------


    // 判断是添加模式还是编辑模式 (Keep this)
    val isEditing = initialRule != null

    // --- 使用 LaunchedEffect 在编辑模式下从数据库加载规则数据 --- (Keep this)
    LaunchedEffect(initialRule) {
        if (isEditing && initialRule != null) {
            // 在协程中调用 ViewModel 的 suspend 函数加载数据
            val loadedRuleEntity = ruleViewModel.getRuleById(initialRule.id)
            loadedRuleEntity?.let {
                // TODO: Map RuleEntity back to Rule UI model if you have one
                // For now, updating state directly from RuleEntity
                ruleName = it.name
                ruleDescription = it.description
                ruleEnabled = it.enabled
                selectedCalendarIds = it.calendarIds ?: emptySet()
                // --- 加载已选择的应用闹钟 ID ---
                selectedAlarmIds = it.targetAlarmIds ?: emptySet()
                // ----------------------------
                // --- 加载规则条件 ---
                ruleCriteria = it.criteria // Assuming criteria is stored in RuleEntity
                // --------------------
            }
        } else {
            // 如果是添加模式，清空状态 (虽然 State 初始化时已经处理了 null)
            ruleName = ""
            ruleDescription = ""
            ruleEnabled = true
            selectedCalendarIds = emptySet()
            selectedAlarmIds = emptySet() // 添加模式初始化为空集合
            ruleCriteria = RuleCriteria.AlwaysTrue // 添加模式初始化为默认条件
            // TODO: Clear state for other criteria details if needed
        }
    }
    // -------------------------------------------------------------

    // --- 协程作用域，用于在 Composable 中启动协程 (例如删除操作) --- (Keep this)
    val coroutineScope = rememberCoroutineScope()
    // ----------------------------------------------------------


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "编辑规则" else "添加规则") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "取消")
                    }
                },
                actions = {
                    // 保存按钮 (Keep this)
                    IconButton(onClick = {
                        // 构建要保存的规则对象
                        val ruleToSave = initialRule?.copy( // 如果是编辑模式，复制现有规则并更新字段
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            calendarIds = selectedCalendarIds,
                            targetAlarmIds = selectedAlarmIds, // <-- **保存已选择的应用闹钟 ID**
                            criteria = ruleCriteria // <-- **保存规则条件**
                        ) ?: Rule( // 如果是添加模式，创建新的 Rule 对象
                            // 对于添加模式，ID 可以由 ViewModel 或持久化层生成
                            // 这里的示例只是为了 Preview 能够编译，实际应用中请根据持久化策略处理 ID
                            id = UUID.randomUUID(), // Preview 中使用随机 ID
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            targetAlarmIds = selectedAlarmIds, // <-- **保存已选择的应用闹钟 ID**
                            calendarIds = selectedCalendarIds,
                            criteria = ruleCriteria // <-- **保存规则条件**
                        )
                        // 调用 ViewModel 的 saveRule 方法保存规则
                        ruleViewModel.saveRule(ruleToSave) // <-- **调用 ViewModel 方法保存**
                        onSaveRule(ruleToSave) // <-- 调用保存回调 (主要用于通知 MainActivity 导航)
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存规则")
                    }

                    // 删除按钮 (只在编辑模式下显示) (Keep this)
                    if (isEditing && initialRule != null) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                // 调用 ViewModel 的 deleteRule 方法删除规则
                                ruleViewModel.deleteRule(initialRule) // <-- **调用 ViewModel 方法删除**
                                onCancel() // 删除后返回规则管理界面
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
            // 规则名称输入框 (Keep this)
            OutlinedTextField(
                value = ruleName,
                onValueChange = { ruleName = it },
                label = { Text("规则名称") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(Modifier.height(16.dp))

            // 规则描述输入框 (Keep this)
            OutlinedTextField(
                value = ruleDescription,
                onValueChange = { ruleDescription = it },
                label = { Text("规则描述") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Spacer(Modifier.height(16.dp))

            // 规则启用状态开关 (Keep this)
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

            // --- 选择日历的 UI --- (Keep this)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 点击时调用回调，传递当前已选中的日历 ID 集合
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
            // -----------------------

            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            // --- 添加选择应用闹钟的 UI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 点击时调用回调，传递当前已选择的应用闹钟 ID 集合
                        onSelectAlarmsClick(selectedAlarmIds)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("选择应用闹钟")
                    Text(
                        text = "已选择 ${selectedAlarmIds.size} 个闹钟", // 显示已选闹钟数量
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
            // --------------------------

            Spacer(Modifier.height(16.dp))

            Divider()
            Spacer(Modifier.height(16.dp))

            // --- 添加定义规则条件的 UI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // 点击时调用回调，传递当前规则条件
                        onDefineCriteriaClick(ruleCriteria)
                    }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("规则条件")
                    // --- 显示规则条件摘要 ---
                    // TODO: Implement RuleCriteria.toSummaryString() extension function in Rule.kt
                    Text(
                        text = ruleCriteria.toSummaryString(), // Placeholder for criteria summary
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

// --- Preview (修改以符合新的函数签名，使用示例 ViewModel) ---
@Preview(showBackground = true)
@Composable
fun AddEditRuleScreenPreviewAdd() {
    MaterialTheme {
        val sampleRuleDao = object : RuleDao {
            private val sampleRulesFlow = MutableStateFlow(emptyList<RuleEntity>())
            override fun getAllRules(): Flow<List<RuleEntity>> = sampleRulesFlow
            override suspend fun getRuleById(ruleId: UUID): RuleEntity? = null
            override suspend fun insertRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun updateRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRuleById(ruleId: UUID) { /* no-op */ }
        }
        val sampleViewModel = RuleViewModel(sampleRuleDao)

        AddEditRuleScreen(
            initialRule = null, // 添加模式预览
            onSaveRule = { rule -> println("Preview: Save New Rule: ${rule.name}") },
            onCancel = { println("Preview: Cancel Add Rule") },
            ruleViewModel = sampleViewModel,
            onSelectCalendarsClick = { selectedIds -> println("Preview: Select Calendars Clicked with IDs: $selectedIds") },
            onSelectAlarmsClick = { selectedIds -> println("Preview: Select Alarms Clicked with IDs: $selectedIds") }, // <-- 传递示例回调
            onDefineCriteriaClick = { criteria -> println("Preview: Define Criteria Clicked with criteria: $criteria") } // <-- 传递示例回调
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
            targetAlarmIds = setOf(UUID.randomUUID(), UUID.randomUUID()), // 示例已选闹钟
            calendarIds = setOf(1L, 2L, 3L),
            criteria = RuleCriteria.AlwaysTrue // 示例规则条件
        )

        val sampleRuleDao = object : RuleDao {
            private val sampleRulesFlow = MutableStateFlow(listOf(
                RuleEntity(sampleRule.id, sampleRule.name, sampleRule.description, sampleRule.enabled, sampleRule.targetAlarmIds, sampleRule.calendarIds, sampleRule.criteria) // Provide sample data for loading
            ))
            override fun getAllRules(): Flow<List<RuleEntity>> = sampleRulesFlow
            override suspend fun getRuleById(ruleId: UUID): RuleEntity? = sampleRulesFlow.value.find { it.id == ruleId }
            override suspend fun insertRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun updateRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRule(rule: RuleEntity) { /* no-op */ }
            override suspend fun deleteRuleById(ruleId: UUID) { /* no-op */ }
        }
        val sampleViewModel = RuleViewModel(sampleRuleDao)

        AddEditRuleScreen(
            initialRule = sampleRule, // 编辑模式预览，传递示例规则
            onSaveRule = { rule -> println("Preview: Save Edited Rule: ${rule.name}") },
            onCancel = { println("Preview: Cancel Edit Rule") },
            ruleViewModel = sampleViewModel,
            onSelectCalendarsClick = { selectedIds -> println("Preview: Select Calendars Clicked with IDs: $selectedIds") },
            onSelectAlarmsClick = { selectedIds -> println("Preview: Select Alarms Clicked with IDs: $selectedIds") }, // <-- 传递示例回调
            onDefineCriteriaClick = { criteria -> println("Preview: Define Criteria Clicked with criteria: $criteria") } // <-- 传递示例回调
        )
    }
}

// Import MutableStateFlow for Preview
