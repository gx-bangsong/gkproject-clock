package com.gkprojct.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleCriteriaDefinitionScreen(
    initialCriteria: RuleCriteria,
    onBackClick: () -> Unit,
    onCriteriaSelected: (RuleCriteria) -> Unit,
    onSelectHolidayCalendarsClick: () -> Unit // This is kept for the ShiftWork criteria
) {
    var currentCriteriaType by remember { mutableStateOf(initialCriteria) }

    // State for IfCalendarEventExists
    var calendarEventKeywordsText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.keywords.joinToString(", ") else "") }
    var calendarEventTimeRangeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.timeRangeMinutes.toString() else "0") }
    var isAllDayEvent by remember { mutableStateOf(if (initialCriteria is RuleCriteria.IfCalendarEventExists) initialCriteria.allDay else false) }

    // State for BasedOnTime
    var startTimeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) else "00:00") }
    var endTimeText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.BasedOnTime) initialCriteria.endTime.format(DateTimeFormatter.ofPattern("HH:mm")) else "23:59") }
    var timeInputError by remember { mutableStateOf<String?>(null) }

    // State for ShiftWork
    var shiftWorkCycleDaysText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.cycleDays.toString() else "4") }
    var shiftWorkShiftsPerCycleText by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.shiftsPerCycle.toString() else "2") }
    var shiftWorkStartDate by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) Instant.ofEpochMilli(initialCriteria.startDate).atZone(ZoneId.systemDefault()).toLocalDate() else LocalDate.now()) }
    var shiftWorkHolidayHandling by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.holidayHandling else HolidayHandlingStrategy.NORMAL_SCHEDULE) }
    var shiftWorkExceptions by remember { mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.exceptions else emptyMap()) }
    val holidayCalendarIds by remember(initialCriteria) {
        mutableStateOf(if (initialCriteria is RuleCriteria.ShiftWork) initialCriteria.holidayCalendarIds else emptySet())
    }

    // State for FreeShift
    var freeShiftWorkDays by remember { mutableStateOf(if (initialCriteria is RuleCriteria.FreeShift) initialCriteria.workDays else emptySet()) }
    var freeShiftExceptions by remember { mutableStateOf(if (initialCriteria is RuleCriteria.FreeShift) initialCriteria.exceptions else emptyMap()) }

    // Common state for multi-selection UI
    var selectedEpochDays by remember { mutableStateOf(emptySet<Long>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val criteriaTypesWithDescription = remember {
        listOf(
            CriteriaTypeInfo(RuleCriteria.AlwaysTrue, "始终启用", "规则将始终处于启用状态"),
            CriteriaTypeInfo(RuleCriteria.IfCalendarEventExists(emptyList(), 0, false), "基于日历事件", "当选定日历中存在匹配事件时触发"),
            CriteriaTypeInfo(RuleCriteria.BasedOnTime(LocalTime.MIDNIGHT, LocalTime.MIDNIGHT), "基于时间段", "在特定时间段内触发"),
            CriteriaTypeInfo(RuleCriteria.ShiftWork(4, 2, System.currentTimeMillis(), 0), "轮班制", "根据轮班周期触发"),
            CriteriaTypeInfo(RuleCriteria.FreeShift(emptySet()), "自由排班", "手动选择未来的工作日")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定义规则条件") },
                navigationIcon = { IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                actions = {
                    IconButton(onClick = {
                        val criteriaToSave = when (val criteria = currentCriteriaType) {
                            is RuleCriteria.IfCalendarEventExists -> RuleCriteria.IfCalendarEventExists(calendarEventKeywordsText.split(",").map { it.trim() }.filter { it.isNotBlank() }, calendarEventTimeRangeText.toIntOrNull() ?: 0, isAllDayEvent)
                            is RuleCriteria.BasedOnTime -> {
                                try {
                                    timeInputError = null
                                    RuleCriteria.BasedOnTime(LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm")), LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("HH:mm")))
                                } catch (e: DateTimeParseException) {
                                    timeInputError = "时间格式错误，请使用 HH:mm 格式"
                                    null
                                }
                            }
                            is RuleCriteria.ShiftWork -> {
                                val cycleDays = shiftWorkCycleDaysText.toIntOrNull() ?: 0
                                val shiftsPerCycle = shiftWorkShiftsPerCycleText.toIntOrNull() ?: 0
                                if (cycleDays > 0 && shiftsPerCycle > 0) {
                                    criteria.copy(
                                        cycleDays = cycleDays,
                                        shiftsPerCycle = shiftsPerCycle,
                                        startDate = shiftWorkStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                                        holidayHandling = shiftWorkHolidayHandling,
                                        exceptions = shiftWorkExceptions,
                                        holidayCalendarIds = holidayCalendarIds
                                    )
                                } else { null }
                            }
                            is RuleCriteria.FreeShift -> criteria.copy(workDays = freeShiftWorkDays, exceptions = freeShiftExceptions)
                            is RuleCriteria.AlwaysTrue -> RuleCriteria.AlwaysTrue
                        }
                        criteriaToSave?.let { onCriteriaSelected(it) }
                    }) { Icon(Icons.Filled.Save, contentDescription = "保存条件") }
                }
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxHeight()) {
            Column(Modifier.padding(16.dp)) {
                // Dropdown to select criteria type
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = criteriaTypesWithDescription.find { it.criteria::class == currentCriteriaType::class }?.name ?: "选择条件类型",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("条件类型") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        criteriaTypesWithDescription.forEach { selection ->
                            DropdownMenuItem(
                                text = { Text(selection.name) },
                                onClick = {
                                    currentCriteriaType = selection.criteria
                                    expanded = false
                                    selectedEpochDays = emptySet() // Reset selection when type changes
                                    if (selection.criteria is RuleCriteria.AlwaysTrue) onCriteriaSelected(selection.criteria)
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))
            }

            // Main content based on selected criteria
            when (currentCriteriaType) {
                is RuleCriteria.IfCalendarEventExists -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(value = calendarEventKeywordsText, onValueChange = { calendarEventKeywordsText = it }, label = { Text("关键词 (用逗号分隔)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value = calendarEventTimeRangeText, onValueChange = { calendarEventTimeRangeText = it }, label = { Text("事件时间范围 (分钟)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isAllDayEvent, onCheckedChange = { isAllDayEvent = it })
                            Text("全天事件")
                        }
                    }
                }
                is RuleCriteria.BasedOnTime -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        OutlinedTextField(value = startTimeText, onValueChange = { startTimeText = it }, label = { Text("开始时间 (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value = endTimeText, onValueChange = { endTimeText = it }, label = { Text("结束时间 (HH:mm)") }, modifier = Modifier.fillMaxWidth())
                        if (timeInputError != null) {
                            Text(timeInputError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                is RuleCriteria.ShiftWork -> {
                    Column(modifier = Modifier.weight(1f)) {
                         Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            // Inputs for periodic shift
                            OutlinedTextField(value = shiftWorkCycleDaysText, onValueChange = { shiftWorkCycleDaysText = it }, label = { Text("轮班周期 (天)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = shiftWorkShiftsPerCycleText, onValueChange = { shiftWorkShiftsPerCycleText = it }, label = { Text("周期内班次数") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { showDatePicker = true }) { Text("选择开始日期: ${shiftWorkStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}") }
                         }
                        // Schedule Editor
                        ShiftScheduleEditor(
                            modifier = Modifier.weight(1f),
                            scheduleProvider = { days ->
                                val cycle = shiftWorkCycleDaysText.toIntOrNull() ?: 0
                                val shifts = shiftWorkShiftsPerCycleText.toIntOrNull() ?: 0
                                List(days) { i ->
                                    val date = LocalDate.now().plusDays(i.toLong())
                                    val daysBetween = ChronoUnit.DAYS.between(shiftWorkStartDate, date)
                                    val isWorkDay = if (cycle > 0 && daysBetween >= 0) (daysBetween % cycle) < shifts else false
                                    DateInfo(date, isWorkDay, shiftWorkExceptions[date.toEpochDay()])
                                }
                            },
                            selectedEpochDays = selectedEpochDays,
                            onDateClick = { day ->
                                selectedEpochDays = if (selectedEpochDays.contains(day)) selectedEpochDays - day else selectedEpochDays + day
                            }
                        )
                    }

                    // Action buttons
                    Column(Modifier.padding(16.dp)) {
                        ShiftActionButtons(
                            enabled = selectedEpochDays.isNotEmpty(),
                            onMarkAsOffDay = {
                                val newExceptions = shiftWorkExceptions.toMutableMap()
                                selectedEpochDays.forEach { newExceptions[it] = RuleException.OffDay }
                                shiftWorkExceptions = newExceptions
                                selectedEpochDays = emptySet()
                            },
                            onChangeTime = { showTimePicker = true }
                        )
                    }
                }
                is RuleCriteria.FreeShift -> {
                    // Schedule Editor
                    ShiftScheduleEditor(
                        modifier = Modifier.weight(1f),
                        scheduleProvider = { days ->
                            // Generate schedule based on free shift selection
                            List(days) { i ->
                                val date = LocalDate.now().plusDays(i.toLong())
                                val isWorkDay = freeShiftWorkDays.contains(date.toEpochDay())
                                DateInfo(date, isWorkDay, freeShiftExceptions[date.toEpochDay()])
                            }
                        },
                        isWorkDayToggleable = true,
                        onWorkDayToggle = { day, isWork ->
                            freeShiftWorkDays = if (isWork) freeShiftWorkDays + day else freeShiftWorkDays - day
                        },
                        selectedEpochDays = selectedEpochDays,
                        onDateClick = { day ->
                            selectedEpochDays = if (selectedEpochDays.contains(day)) selectedEpochDays - day else selectedEpochDays + day
                        }
                    )

                    // Action buttons
                    Column(Modifier.padding(16.dp)) {
                        ShiftActionButtons(
                            enabled = selectedEpochDays.isNotEmpty(),
                            onMarkAsOffDay = {
                                val newExceptions = freeShiftExceptions.toMutableMap()
                                selectedEpochDays.forEach { newExceptions[it] = RuleException.OffDay }
                                freeShiftExceptions = newExceptions
                                selectedEpochDays = emptySet()
                            },
                            onChangeTime = { showTimePicker = true }
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("选择一个规则条件类型")
                    }
                }
            }
        }

        // --- Dialogs ---
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = shiftWorkStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { shiftWorkStartDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                        showDatePicker = false
                    }) { Text("确认") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
            ) { DatePicker(state = datePickerState) }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState()
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            when (currentCriteriaType) {
                                is RuleCriteria.ShiftWork -> {
                                    val newExceptions = shiftWorkExceptions.toMutableMap()
                                    selectedEpochDays.forEach { newExceptions[it] = RuleException.CustomTime(newTime) }
                                    shiftWorkExceptions = newExceptions
                                }
                                is RuleCriteria.FreeShift -> {
                                    val newExceptions = freeShiftExceptions.toMutableMap()
                                    selectedEpochDays.forEach { newExceptions[it] = RuleException.CustomTime(newTime) }
                                    freeShiftExceptions = newExceptions
                                }
                                else -> {}
                            }
                            selectedEpochDays = emptySet()
                            showTimePicker = false
                        }
                    ) { Text("确认") }
                },
                dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("取消") } }
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}

data class DateInfo(val date: LocalDate, val isWorkDay: Boolean, val exception: RuleException?)

@Composable
fun ShiftScheduleEditor(
    modifier: Modifier = Modifier,
    scheduleProvider: (Int) -> List<DateInfo>,
    selectedEpochDays: Set<Long>,
    onDateClick: (Long) -> Unit,
    isWorkDayToggleable: Boolean = false,
    onWorkDayToggle: (Long, Boolean) -> Unit = { _, _ -> }
) {
    val schedule = remember(scheduleProvider) { scheduleProvider(90) } // Display 90 days

    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        items(schedule) { dateInfo ->
            DateRow(
                dateInfo = dateInfo,
                isSelected = selectedEpochDays.contains(dateInfo.date.toEpochDay()),
                onDateClick = { onDateClick(dateInfo.date.toEpochDay()) },
                isWorkDayToggleable = isWorkDayToggleable,
                onWorkDayToggle = { onWorkDayToggle(dateInfo.date.toEpochDay(), it) }
            )
            HorizontalDivider()
        }
    }
}

@Composable
fun DateRow(
    dateInfo: DateInfo,
    isSelected: Boolean,
    onDateClick: () -> Unit,
    isWorkDayToggleable: Boolean,
    onWorkDayToggle: (Boolean) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val isActuallyWorkDay = dateInfo.isWorkDay && dateInfo.exception == null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .clickable(onClick = onDateClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateInfo.date.format(DateTimeFormatter.ofPattern("M月d日, EEE", Locale.getDefault())),
            modifier = Modifier.weight(1f),
            fontWeight = if (isActuallyWorkDay) FontWeight.Bold else FontWeight.Normal,
            color = if (isActuallyWorkDay) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (val exception = dateInfo.exception) {
            is RuleException.OffDay -> Text("休息", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            is RuleException.CustomTime -> Text(exception.time.format(DateTimeFormatter.ofPattern("HH:mm")), color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            null -> {
                if (isWorkDayToggleable) {
                    Switch(checked = dateInfo.isWorkDay, onCheckedChange = onWorkDayToggle)
                } else if (dateInfo.isWorkDay) {
                    Text("工作日", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ShiftActionButtons(
    enabled: Boolean,
    onMarkAsOffDay: () -> Unit,
    onChangeTime: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onMarkAsOffDay, enabled = enabled, modifier = Modifier.weight(1f)) {
            Text("标记为休息")
        }
        Button(onClick = onChangeTime, enabled = enabled, modifier = Modifier.weight(1f)) {
            Text("修改时间")
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String = "选择时间",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
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
            initialCriteria = RuleCriteria.FreeShift(
                workDays = setOf(LocalDate.now().toEpochDay(), LocalDate.now().plusDays(2).toEpochDay()),
                exceptions = mapOf(
                    LocalDate.now().plusDays(1).toEpochDay() to RuleException.OffDay,
                    LocalDate.now().plusDays(3).toEpochDay() to RuleException.CustomTime(LocalTime.of(9, 30))
                )
            ),
            onBackClick = {},
            onCriteriaSelected = {},
            onSelectHolidayCalendarsClick = {}
        )
    }
}