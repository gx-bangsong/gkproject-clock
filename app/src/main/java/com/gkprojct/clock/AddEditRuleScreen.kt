package com.gkprojct.clock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import com.gkprojct.clock.RuleAction
import com.gkprojct.clock.vm.RuleDao
import com.gkprojct.clock.vm.RuleEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    initialRule: Rule? = null,
    onSaveRule: (Rule) -> Unit,
    onCancel: () -> Unit,
    ruleViewModel: RuleViewModel,
    onSelectCalendarsClick: (Set<Long>) -> Unit,
    onSelectAlarmsClick: (Set<UUID>) -> Unit,
    onDefineCriteriaClick: (RuleCriteria) -> Unit
) {
    var ruleName by remember { mutableStateOf(initialRule?.name ?: "") }
    var ruleDescription by remember { mutableStateOf(initialRule?.description ?: "") }
    var ruleEnabled by remember { mutableStateOf(initialRule?.enabled ?: true) }
    var selectedCalendarIds by remember { mutableStateOf(initialRule?.calendarIds ?: emptySet()) }
    var selectedAlarmIds by remember { mutableStateOf(initialRule?.targetAlarmIds ?: emptySet()) }
    var ruleCriteria by remember { mutableStateOf<RuleCriteria>(initialRule?.criteria ?: RuleCriteria.AlwaysTrue) }
    var ruleAction by remember { mutableStateOf<RuleAction>(initialRule?.action ?: RuleAction.SkipNextAlarm) }

    val isEditing = initialRule != null
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialRule) {
        if (isEditing && initialRule != null) {
            ruleViewModel.getRuleById(initialRule.id)?.let {
                ruleName = it.name
                ruleDescription = it.description
                ruleEnabled = it.enabled
                selectedCalendarIds = it.calendarIds ?: emptySet()
                selectedAlarmIds = it.targetAlarmIds ?: emptySet()
                ruleCriteria = it.criteria
                ruleAction = it.action
            }
        }
    }

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
                    IconButton(onClick = {
                        val ruleToSave = initialRule?.copy(
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            calendarIds = selectedCalendarIds,
                            targetAlarmIds = selectedAlarmIds,
                            criteria = ruleCriteria,
                            action = ruleAction
                        ) ?: Rule(
                            id = UUID.randomUUID(),
                            name = ruleName,
                            description = ruleDescription,
                            enabled = ruleEnabled,
                            targetAlarmIds = selectedAlarmIds,
                            calendarIds = selectedCalendarIds,
                            criteria = ruleCriteria,
                            action = ruleAction
                        )
                        ruleViewModel.saveRule(ruleToSave)
                        onSaveRule(ruleToSave)
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存规则")
                    }
                    if (isEditing && initialRule != null) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                ruleViewModel.deleteRule(initialRule)
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
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Criteria, Calendars, Alarms sections...
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectCalendarsClick(selectedCalendarIds) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("选择日历")
                    Text("已选择 ${selectedCalendarIds.size} 个日历", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "选择日历")
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAlarmsClick(selectedAlarmIds) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("选择应用闹钟")
                    Text("已选择 ${selectedAlarmIds.size} 个闹钟", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "选择应用闹钟")
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDefineCriteriaClick(ruleCriteria) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("规则条件")
                    Text(ruleCriteria.toSummaryString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "定义规则条件")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // --- Action Selection ---
            Text("执行操作", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            var showTimePicker by remember { mutableStateOf(false) }

            Row(Modifier.fillMaxWidth().clickable { ruleAction = RuleAction.SkipNextAlarm }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = ruleAction is RuleAction.SkipNextAlarm,
                    onClick = { ruleAction = RuleAction.SkipNextAlarm }
                )
                Spacer(Modifier.width(8.dp))
                Text("跳过当天闹钟")
            }
            Row(Modifier.fillMaxWidth().clickable {
                if (ruleAction !is RuleAction.AdjustAlarmTime) {
                    ruleAction = RuleAction.AdjustAlarmTime(LocalTime.now())
                }
                showTimePicker = true
            }, verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = ruleAction is RuleAction.AdjustAlarmTime,
                    onClick = {
                        if (ruleAction !is RuleAction.AdjustAlarmTime) {
                            ruleAction = RuleAction.AdjustAlarmTime(LocalTime.now())
                        }
                        showTimePicker = true
                    }
                )
                Spacer(Modifier.width(8.dp))
                val actionText = if (ruleAction is RuleAction.AdjustAlarmTime) {
                    ": " + (ruleAction as RuleAction.AdjustAlarmTime).newTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                } else {
                    ""
                }
                Text("调整闹钟时间$actionText")
            }

            if (showTimePicker) {
                val currentTime = if (ruleAction is RuleAction.AdjustAlarmTime) (ruleAction as RuleAction.AdjustAlarmTime).newTime else LocalTime.now()
                val timePickerState = rememberTimePickerState(initialHour = currentTime.hour, initialMinute = currentTime.minute, is24Hour = true)

                TimePickerDialog(
                    onDismissRequest = { showTimePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            ruleAction = RuleAction.AdjustAlarmTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                            showTimePicker = false
                        }) { Text("确认") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("取消") }
                    }
                ) {
                    TimePicker(state = timePickerState)
                }
            }
        }
    }
}

@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun AddEditRuleScreenPreviewAdd() {
    MaterialTheme {
        val sampleViewModel = RuleViewModel(object : RuleDao {
            override fun getAllRules(): kotlinx.coroutines.flow.Flow<List<RuleEntity>> = MutableStateFlow(emptyList())
            override suspend fun getRuleById(ruleId: UUID): RuleEntity? = null
            override suspend fun insertRule(rule: RuleEntity) {}
            override suspend fun updateRule(rule: RuleEntity) {}
            override suspend fun deleteRule(rule: RuleEntity) {}
            override suspend fun deleteRuleById(ruleId: UUID) {}
        })

        AddEditRuleScreen(
            initialRule = null,
            onSaveRule = {},
            onCancel = {},
            ruleViewModel = sampleViewModel,
            onSelectCalendarsClick = {},
            onSelectAlarmsClick = {},
            onDefineCriteriaClick = {}
        )
    }
}
