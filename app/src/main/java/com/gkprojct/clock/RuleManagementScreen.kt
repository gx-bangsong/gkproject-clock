package com.gkprojct.clock // <-- 确保包路径正确


// --- 导入 Rule 和 RuleCriteria (从 com.gkprojct.clock 包导入) ---
// -------------------------------------------------------------

// --- 导入 RuleEntity 和 RuleDao (从 com.gkprojct.clock.vm 包导入) ---
// **确保这里的导入路径是 com.gkprojct.clock.vm**
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gkprojct.clock.vm.RuleEntity
import com.gkprojct.clock.RuleViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

// -----------------------------------------------------------------


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    onBackClick: () -> Unit,
    onAddRuleClick: () -> Unit,
    onRuleClick: (Rule) -> Unit,
    ruleViewModel: RuleViewModel // <-- **RuleViewModel 参数**
) {
    // --- 从 ViewModel 收集规则列表 ---
    // allRules 是一个 Flow<List<RuleEntity>>，使用 collectAsState 将其转换为 State<List<RuleEntity>>
    // 这里的 RuleEntity 应该是 com.gkprojct.clock.vm.RuleEntity
    val rules: List<RuleEntity> by ruleViewModel.allRules.collectAsState(initial = emptyList())
    // -------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("管理规则") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onAddRuleClick) { // 点击添加按钮时调用回调
                        Icon(Icons.Filled.Add, contentDescription = "添加规则")
                    }
                }
            )
        }
    ) { paddingValues -> // <-- 检查这个 lambda 内部的代码
        Column(
            modifier = Modifier
                .padding(paddingValues) // <-- 错误在这里使用 paddingValues
                .fillMaxSize()
                .padding(horizontal = 16.dp) // 内边距
        ) {
            // --- 根据从 ViewModel 加载的 rules 列表判断是否为空 ---
            if (rules.isEmpty()) { // <-- 使用从 ViewModel 收集的 rules
                Text("您还没有创建任何规则。")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onAddRuleClick) {
                    Text("添加第一条规则")
                }
            } else {
                Text(
                    text = "已定义的规则:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Divider() // 分隔线

                // --- 显示从 ViewModel 加载的 rules 列表 ---
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Item 之间的间距
                ) {
                    // 这里的 ruleEntity 是 com.gkprojct.clock.vm.RuleEntity 类型
                    items(rules, key = { it.id }) { ruleEntity ->
                        // --- Map RuleEntity to Rule UI model ---
                        // 这里的 Rule 是 com.gkprojct.clock.Rule 类型
                        val rule = Rule(
                            id = ruleEntity.id,
                            name = ruleEntity.name,
                            description = ruleEntity.description,
                            enabled = ruleEntity.enabled,
                            targetAlarmIds = ruleEntity.targetAlarmIds,
                            calendarIds = ruleEntity.calendarIds,
                            criteria = ruleEntity.criteria // <-- Map criteria from RuleEntity
                        )
                        // -------------------------------------
                        RuleItem(rule = rule, onClick = { onRuleClick(rule) }) // Pass the created Rule object
                        Divider() // 每个规则项下方添加分隔线
                    }
                }
                // -------------------------------------
            }
        }
    }
}

// --- 单个规则列表项 Composable (修改以显示摘要) (Keep this) ---
@Composable
fun RuleItem(rule: Rule, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使整个 Row 可点击
            .padding(vertical = 12.dp), // Item 内部垂直边距
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 子项之间平均分布空间
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) { // 文本占据大部分空间
            Text(
                text = rule.name, // <-- 使用 ruleEntity 的属性
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = rule.description, // <-- 使用 ruleEntity 的属性
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp)) // 添加一些垂直间距

            // --- 显示规则摘要信息 ---
            // Calling the extension function defined in Rule.kt
            Text(
                text = "影响 ${rule.targetAlarmIds.size} 个闹钟 | 使用 ${rule.calendarIds.size} 个日历 | 条件: ${rule.criteria.toSummaryString()}", // <-- 显示摘要
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // -----------------------
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (rule.enabled) "启用" else "禁用", // <-- 使用 ruleEntity 的属性
                fontSize = 14.sp,
                color = if (rule.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "编辑规则 ${rule.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// --- Preview (更新以使用 CriteriaTypeInfo) ---
@Preview(showBackground = true)
@Composable
fun RuleManagementScreenPreview() {
    MaterialTheme {
        // 在 Preview 中创建一个简单的 ViewModel 实例 (通常不推荐，但为了 Preview 方便)
        // 在实际应用中，ViewModel 由系统提供
        // 注意：这个示例 ViewModel 不会真正从数据库加载数据，只用于 Preview 编译
        // **确保这里使用的 RuleDao 和 RuleEntity 是 com.gkprojct.clock.vm 包下的**
        val sampleRuleDao = object : com.gkprojct.clock.vm.RuleDao { // 使用 vm 包下的 RuleDao
            // 提供一些示例数据给 Preview
            // **确保这里创建的 RuleEntity 实例包含 criteria 字段**
            private val sampleRulesFlow = MutableStateFlow(listOf(
                com.gkprojct.clock.vm.RuleEntity(UUID.randomUUID(), "假日暂停 (Preview)", "节假日自动暂停", true, setOf(UUID.randomUUID()), setOf(1L), RuleCriteria.IfCalendarEventExists(listOf("holiday"), 60)), // Sample Rule 1
                com.gkprojct.clock.vm.RuleEntity(UUID.randomUUID(), "无课暂停 (Preview)", "课程日历无课时暂停工作日闹钟", false, setOf(UUID.randomUUID(), UUID.randomUUID()), setOf(2L), RuleCriteria.BasedOnTime(java.time.LocalTime.of(9,0), java.time.LocalTime.of(17,0))), // Sample Rule 2
                com.gkprojct.clock.vm.RuleEntity(UUID.randomUUID(), "始终启用规则 (Preview)", "一个总是启用的规则", true, emptySet(), emptySet(), RuleCriteria.AlwaysTrue) // Sample Rule 3
            ))
            override fun getAllRules(): Flow<List<com.gkprojct.clock.vm.RuleEntity>> = sampleRulesFlow // 返回类型应匹配 vm 包下的 RuleEntity
            override suspend fun getRuleById(ruleId: UUID): com.gkprojct.clock.vm.RuleEntity? = sampleRulesFlow.value.find { it.id == ruleId } // 返回类型应匹配 vm 包下的 RuleEntity
            override suspend fun insertRule(rule: com.gkprojct.clock.vm.RuleEntity) { /* no-op for preview */ } // 参数类型应匹配 vm 包下的 RuleEntity
            override suspend fun updateRule(rule: com.gkprojct.clock.vm.RuleEntity) { /* no-op for preview */ } // 参数类型应匹配 vm 包下的 RuleEntity
            override suspend fun deleteRule(rule: com.gkprojct.clock.vm.RuleEntity) { /* no-op for preview */ } // 参数类型应匹配 vm 包下的 RuleEntity
            override suspend fun deleteRuleById(ruleId: UUID) { /* no-op for preview */ }
        }
        // **确保这里使用的 RuleViewModel 是 com.gkprojct.clock.vm 包下的**
        val sampleViewModel = com.gkprojct.clock.RuleViewModel(sampleRuleDao) // 使用 vm 包下的 RuleViewModel

        RuleManagementScreen(
            onBackClick = {},
            onAddRuleClick = { println("Preview: Add Rule Clicked") },
            onRuleClick = { rule -> println("Preview: Rule Clicked: ${rule.name}") },
            ruleViewModel = sampleViewModel // <-- 传递示例 ViewModel
        )
    }
}
