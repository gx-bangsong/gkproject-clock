package com.gkprojct.clock // <-- 确保包路径正确

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import java.time.format.DateTimeParseException
import java.time.format.DateTimeFormatter // Import DateTimeFormatter


// --- 导入 RuleCriteria (从 com.gkprojct.clock 包导入) ---
import com.gkprojct.clock.RuleCriteria
// -----------------------------------------------------
// Import Rule (Needed for Preview)
// import com.gkprojct.clock.Rule
// Import RuleEntity (Needed for Preview)
// import com.gkprojct.clock.RuleEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleCriteriaDefinitionScreen(
    initialCriteria: RuleCriteria,
    onBackClick: () -> Unit,
    onCriteriaSelected: (RuleCriteria) -> Unit
) {
    var currentCriteriaType by remember { mutableStateOf(initialCriteria) }

    // --- 状态变量：用于 IfCalendarEventExists 条件的配置 ---
    var calendarEventKeywordsText by remember {
        mutableStateOf(
            if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.keywords.joinToString(", ") else ""
        )
    }
    var calendarEventTimeRangeText by remember {
        mutableStateOf(
            if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.timeRangeMinutes.toString() else ""
        )
    }
    // -----------------------------------------------------

    // --- 状态变量：用于 BasedOnTime 条件的配置 ---
    // 使用 LocalTime.MIDNIGHT 作为默认值
    var startTimeText by remember {
        mutableStateOf(
            if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) else LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }
    var endTimeText by remember {
        mutableStateOf(
            if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.endTime.format(DateTimeFormatter.ofPattern("HH:mm")) else LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
        )
    }
    // -----------------------------------------------------

    // 状态：时间解析错误提示
    var timeInputError by remember { mutableStateOf<String?>(null) }


    LaunchedEffect(currentCriteriaType) {
        // 在切换条件类型时，重置错误提示和特定条件的配置状态
        timeInputError = null
        when (currentCriteriaType) {
            is RuleCriteria.AlwaysTrue -> {
                calendarEventKeywordsText = ""
                calendarEventTimeRangeText = ""
                startTimeText = LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
                endTimeText = LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            is RuleCriteria.IfCalendarEventExists -> {
                val criteria = currentCriteriaType as RuleCriteria.IfCalendarEventExists
                calendarEventKeywordsText = criteria.keywords.joinToString(", ")
                calendarEventTimeRangeText = criteria.timeRangeMinutes.toString()
                startTimeText = LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
                endTimeText = LocalTime.MIDNIGHT.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            is RuleCriteria.BasedOnTime -> {
                val criteria = currentCriteriaType as RuleCriteria.BasedOnTime
                startTimeText = criteria.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                endTimeText = criteria.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                calendarEventKeywordsText = ""
                calendarEventTimeRangeText = ""
            }
            // TODO: Add cases for other criteria types to reset/load their states
        }
    }


    val criteriaTypesWithDescription = remember {
        listOf(
            CriteriaTypeInfo(RuleCriteria.AlwaysTrue, "始终启用", "规则将始终处于启用状态"),
            CriteriaTypeInfo(RuleCriteria.IfCalendarEventExists(emptyList(), 0), "基于日历事件", "当选定日历中存在匹配事件时触发"),
            CriteriaTypeInfo(RuleCriteria.BasedOnTime(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT), "基于时间段", "在特定时间段内触发"),
            // TODO: Add other criteria types with names and descriptions
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定义规则条件") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    when (currentCriteriaType) {
                        is RuleCriteria.IfCalendarEventExists -> {
                            IconButton(onClick = {
                                val keywords = calendarEventKeywordsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val timeRange = calendarEventTimeRangeText.toIntOrNull() ?: 0

                                val configuredCriteria = RuleCriteria.IfCalendarEventExists(keywords, timeRange)

                                onCriteriaSelected(configuredCriteria)
                            }) {
                                Icon(Icons.Filled.Save, contentDescription = "保存条件")
                            }
                        }
                        is RuleCriteria.BasedOnTime -> {
                            IconButton(onClick = {
                                timeInputError = null // 清除之前的错误提示
                                try {
                                    val startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"))
                                    val endTime = LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("HH:mm"))
                                    val configuredCriteria = RuleCriteria.BasedOnTime(startTime, endTime)
                                    onCriteriaSelected(configuredCriteria)
                                } catch (e: DateTimeParseException) {
                                    timeInputError = "时间格式错误，请使用 HH:mm 格式" // 设置错误提示
                                    println("Error parsing time: ${e.message}")
                                } catch (e: Exception) {
                                    timeInputError = "保存时间条件时发生错误" // 设置错误提示
                                    println("Error saving time criteria: ${e.message}")
                                }
                            }) {
                                Icon(Icons.Filled.Save, contentDescription = "保存时间条件")
                            }
                        }
                        else -> {
                            // Do nothing - no save button needed for other criteria types (like AlwaysTrue)
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
                .padding(horizontal = 16.dp)
        ) {
            when (currentCriteriaType) {
                is RuleCriteria.AlwaysTrue -> {
                    Text(
                        text = "当前条件: 始终启用",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                        text = "此规则将始终处于启用状态，除非您手动禁用它。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "选择其他条件类型:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(criteriaTypesWithDescription.filter { it.criteria != RuleCriteria.AlwaysTrue }) { criteriaInfo ->
                            CriteriaTypeItem(
                                name = criteriaInfo.name,
                                description = criteriaInfo.description,
                                onClick = {
                                    currentCriteriaType = criteriaInfo.criteria
                                    // For simple types like AlwaysTrue, we might select directly
                                    // For complex types, we just switch the UI for configuration
                                    if (criteriaInfo.criteria is RuleCriteria.AlwaysTrue) {
                                        onCriteriaSelected(criteriaInfo.criteria)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                is RuleCriteria.IfCalendarEventExists -> {
                    Text(
                        text = "配置日历事件条件",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                        text = "当选定日历中存在包含以下关键词的事件时，规则将被触发。时间范围指定在事件发生前多久检查。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = calendarEventKeywordsText,
                        onValueChange = { calendarEventKeywordsText = it },
                        label = { Text("关键词 (用逗号分隔)") },
                        placeholder = { Text("例如: 会议, 工作, 提醒") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = calendarEventTimeRangeText,
                        onValueChange = { calendarEventTimeRangeText = it },
                        label = { Text("事件时间范围 (分钟)") },
                        placeholder = { Text("例如: 30 (事件发生前 30 分钟内检查)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                    )
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "选择其他条件类型:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(criteriaTypesWithDescription.filter { it.criteria != currentCriteriaType }) { criteriaInfo ->
                            CriteriaTypeItem(
                                name = criteriaInfo.name,
                                description = criteriaInfo.description,
                                onClick = {
                                    currentCriteriaType = criteriaInfo.criteria
                                    if (criteriaInfo.criteria is RuleCriteria.AlwaysTrue) {
                                        onCriteriaSelected(criteriaInfo.criteria)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                is RuleCriteria.BasedOnTime -> {
                    Text(
                        text = "配置基于时间段条件",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Text(
                        text = "规则将在指定的每日时间段内触发。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = startTimeText,
                        onValueChange = { startTimeText = it },
                        label = { Text("开始时间") },
                        placeholder = { Text("例如: 08:00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = endTimeText,
                        onValueChange = { endTimeText = it },
                        label = { Text("结束时间") },
                        placeholder = { Text("例如: 17:00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    if (timeInputError != null) {
                        Text(
                            text = timeInputError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "选择其他条件类型:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(criteriaTypesWithDescription.filter { it.criteria != currentCriteriaType }) { criteriaInfo ->
                            CriteriaTypeItem(
                                name = criteriaInfo.name,
                                description = criteriaInfo.description,
                                onClick = {
                                    currentCriteriaType = criteriaInfo.criteria
                                    if (criteriaInfo.criteria is RuleCriteria.AlwaysTrue) {
                                        onCriteriaSelected(criteriaInfo.criteria)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
                else -> {
                    Text(
                        text = "未知或未实现的条件类型: ${currentCriteriaType::class.simpleName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "选择其他条件类型:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(criteriaTypesWithDescription.filter { it.criteria != currentCriteriaType }) { criteriaInfo ->
                            CriteriaTypeItem(
                                name = criteriaInfo.name,
                                description = criteriaInfo.description,
                                onClick = {
                                    currentCriteriaType = criteriaInfo.criteria
                                    if (criteriaInfo.criteria is RuleCriteria.AlwaysTrue) {
                                        onCriteriaSelected(criteriaInfo.criteria)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

// --- Data class to hold Criteria Type Info (Name and Description) (Keep this) ---
data class CriteriaTypeInfo(
    val criteria: RuleCriteria,
    val name: String,
    val description: String
)

// --- 单个条件类型列表项 Composable (修改以接收 name 和 description) (Keep this) ---
@Composable
fun CriteriaTypeItem(name: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "配置条件",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- Preview (更新以使用 CriteriaTypeInfo) ---
@Preview(showBackground = true)
@Composable
fun RuleCriteriaDefinitionScreenPreviewAlwaysTrue() {
    MaterialTheme {
        RuleCriteriaDefinitionScreen(
            initialCriteria = RuleCriteria.AlwaysTrue,
            onBackClick = { println("Preview: Back Clicked") },
            onCriteriaSelected = { criteria -> println("Preview: Criteria Selected: $criteria") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RuleCriteriaDefinitionScreenPreviewIfCalendarEventExists() {
    MaterialTheme {
        RuleCriteriaDefinitionScreen(
            initialCriteria = RuleCriteria.IfCalendarEventExists(listOf("meeting", "work"), 30),
            onBackClick = { println("Preview: Back Clicked") },
            onCriteriaSelected = { criteria -> println("Preview: Criteria Selected: $criteria") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RuleCriteriaDefinitionScreenPreviewBasedOnTime() {
    MaterialTheme {
        RuleCriteriaDefinitionScreen(
            initialCriteria = RuleCriteria.BasedOnTime(java.time.LocalTime.of(8, 0), java.time.LocalTime.of(17, 0)),
            onBackClick = { println("Preview: Back Clicked") },
            onCriteriaSelected = { criteria -> println("Preview: Criteria Selected: $criteria") }
        )
    }
}
