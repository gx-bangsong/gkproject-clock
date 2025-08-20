package com.gkprojct.clock

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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleCriteriaDefinitionScreen(
    initialCriteria: RuleCriteria,
    onBackClick: () -> Unit,
    onCriteriaSelected: (RuleCriteria) -> Unit,
    onSelectHolidayCalendarsClick: () -> Unit
) {
    var currentCriteriaType by remember { mutableStateOf(initialCriteria) }

    // --- State for IfCalendarEventExists ---
    var calendarEventKeywordsText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.keywords.joinToString(", ") else "") }
    var calendarEventTimeRangeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.timeRangeMinutes.toString() else "0") }
    var isAllDayEvent by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.allDay else false) }

    // --- State for BasedOnTime ---
    var startTimeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) else "00:00") }
    var endTimeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.endTime.format(DateTimeFormatter.ofPattern("HH:mm")) else "23:59") }
    var timeInputError by remember { mutableStateOf<String?>(null) }

    // --- State for ShiftWork ---
    var cycleDaysText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.cycleDays.toString() else "4") }
    var shiftsPerCycleText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.shiftsPerCycle.toString() else "2") }
    var shiftStartDate by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) Instant.ofEpochMilli(initialCriteria.startDate).atZone(ZoneId.systemDefault()).toLocalDate() else LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var shiftWorkHolidayHandling by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.holidayHandling else HolidayHandlingStrategy.NORMAL_SCHEDULE) }

    val criteriaTypesWithDescription = remember {
        listOf(
            CriteriaTypeInfo(RuleCriteria.AlwaysTrue, "始终启用", "规则将始终处于启用状态"),
            CriteriaTypeInfo(RuleCriteria.IfCalendarEventExists(emptyList(), 0, false), "基于日历事件", "当选定日历中存在匹配事件时触发"),
            CriteriaTypeInfo(RuleCriteria.BasedOnTime(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT), "基于时间段", "在特定时间段内触发"),
            CriteriaTypeInfo(RuleCriteria.ShiftWork(4, 2, System.currentTimeMillis(), 0), "轮班制", "根据轮班周期触发")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定义规则条件") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.Filled.ArrowBack, contentDescription = "返回") } },
                actions = {
                    val currentType = currentCriteriaType
                    if (currentType !is RuleCriteria.AlwaysTrue) {
                        IconButton(onClick = {
                            val criteriaToSave = when (currentType) {
                                is RuleCriteria.IfCalendarEventExists -> {
                                    val keywords = calendarEventKeywordsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    val timeRange = calendarEventTimeRangeText.toIntOrNull() ?: 0
                                    RuleCriteria.IfCalendarEventExists(keywords, timeRange, isAllDayEvent)
                                }
                                is RuleCriteria.BasedOnTime -> {
                                    try {
                                        val startTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"))
                                        val endTime = LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("HH:mm"))
                                        timeInputError = null
                                        RuleCriteria.BasedOnTime(startTime, endTime)
                                    } catch (e: DateTimeParseException) {
                                        timeInputError = "时间格式错误，请使用 HH:mm 格式"
                                        null
                                    }
                                }
                                is RuleCriteria.ShiftWork -> {
                                    val cycleDays = cycleDaysText.toIntOrNull() ?: 0
                                    val shiftsPerCycle = shiftsPerCycleText.toIntOrNull() ?: 0
                                    val startDateMillis = shiftStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                    if (cycleDays > 0 && shiftsPerCycle > 0) {
                                        RuleCriteria.ShiftWork(cycleDays, shiftsPerCycle, startDateMillis, 0, emptySet(), shiftWorkHolidayHandling)
                                    } else {
                                        null // Invalid input
                                    }
                                }
                                else -> null
                            }
                            criteriaToSave?.let { onCriteriaSelected(it) }
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "保存条件")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).padding(16.dp)) {
            // UI for selecting criteria type
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = criteriaTypesWithDescription.find { it.criteria::class == currentCriteriaType::class }?.name ?: "选择条件类型",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("条件类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    criteriaTypesWithDescription.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection.name) },
                            onClick = {
                                currentCriteriaType = selection.criteria
                                expanded = false
                                if (selection.criteria is RuleCriteria.AlwaysTrue) {
                                    onCriteriaSelected(selection.criteria)
                                }
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            when (val current = currentCriteriaType) {
                is RuleCriteria.AlwaysTrue -> {
                    Text("此规则将始终处于启用状态，除非您手动禁用它。")
                }
                is RuleCriteria.IfCalendarEventExists -> {
                    OutlinedTextField(value = calendarEventKeywordsText, onValueChange = { calendarEventKeywordsText = it }, label = { Text("关键词 (用逗号分隔)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = calendarEventTimeRangeText, onValueChange = { calendarEventTimeRangeText = it }, label = { Text("事件时间范围 (分钟)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isAllDayEvent, onCheckedChange = { isAllDayEvent = it })
                        Text("全天事件")
                    }
                }
                is RuleCriteria.BasedOnTime -> {
                    OutlinedTextField(value = startTimeText, onValueChange = { startTimeText = it }, label = { Text("开始时间 (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = endTimeText, onValueChange = { endTimeText = it }, label = { Text("结束时间 (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                    timeInputError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                }
                is RuleCriteria.ShiftWork -> {
                    OutlinedTextField(value = cycleDaysText, onValueChange = { cycleDaysText = it }, label = { Text("轮班周期 (天)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(value = shiftsPerCycleText, onValueChange = { shiftsPerCycleText = it }, label = { Text("周期内班次数") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showDatePicker = true }) {
                        Text("选择开始日期: ${shiftStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onSelectHolidayCalendarsClick) {
                        Text("选择假日日历")
                    }
                    Spacer(Modifier.height(16.dp))
                    var strategyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = strategyExpanded, onExpandedChange = { strategyExpanded = !strategyExpanded }) {
                        OutlinedTextField(
                            value = shiftWorkHolidayHandling.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("假日处理方式") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = strategyExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = strategyExpanded, onDismissRequest = { strategyExpanded = false }) {
                            HolidayHandlingStrategy.values().forEach { strategy ->
                                DropdownMenuItem(
                                    text = { Text(strategy.name) },
                                    onClick = {
                                        shiftWorkHolidayHandling = strategy
                                        strategyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    if (showDatePicker) {
                        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = shiftStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        shiftStartDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                    }
                                    showDatePicker = false
                                }) { Text("确认") }
                            },
                            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            }
        }
    }
}

data class CriteriaTypeInfo(
    val criteria: RuleCriteria,
    val name: String,
    val description: String
)

@Preview(showBackground = true)
@Composable
fun RuleCriteriaDefinitionScreenPreview() {
    MaterialTheme {
        RuleCriteriaDefinitionScreen(
            initialCriteria = RuleCriteria.ShiftWork(4, 2, System.currentTimeMillis(), 0),
            onBackClick = {},
            onCriteriaSelected = {}
        )
    }
}
