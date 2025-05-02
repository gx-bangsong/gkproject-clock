package com.gkprojct.clock // <-- Ensure package path is correct

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import androidx.compose.runtime.mutableStateOf // Import mutableStateOf
import java.time.DayOfWeek // Import DayOfWeek for Preview
import kotlin.collections.mutableSetOf // Import mutableSetOf explicitly for clarity
import java.util.Calendar // Import Calendar for Preview sample data

// --- Import Alarm from the correct file ---
// Assuming Alarm is defined in AlarmScreen.kt
import com.gkprojct.clock.Alarm

// --- Import necessary components from SharedDefinitions.kt if used ---
// Assuming these are defined in SharedDefinitions.kt and imported correctly
// import com.gkprojct.clock.DayButton
// import com.gkprojct.clock.AlarmOptionItem
// import com.gkprojct.clock.MoreOptionsSettingsAction
// import com.gkprojct.clock.shortDayNamesOrder
// import com.gkprojct.clock.dayOfWeekToShortName


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleAlarmSelectionScreen(
    availableAlarms: List<Alarm>, // <-- Parameter type is List<Alarm>
    initialSelectedAlarmIds: Set<UUID>,
    onBackClick: () -> Unit,
    onAlarmsSelected: (Set<UUID>) -> Unit
) {
    // 状态：当前已选择的闹钟 ID 集合
    // 使用 mutableStateOf 包装一个标准的 mutableSetOf，并用 initialSelectedAlarmIds 初始化
    var selectedAlarmIds by remember { mutableStateOf(mutableSetOf<UUID>().apply { addAll(initialSelectedAlarmIds) }) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择应用闹钟") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onAlarmsSelected(selectedAlarmIds) // 传递选中的 ID 集合
                        onBackClick() // 保存后返回
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存选中的闹钟")
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
            Text(
                text = "选择规则将影响的闹钟:",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Divider()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- 使用传入的 availableAlarms 列表 ---
                items(availableAlarms, key = { it.id }) { alarm -> // <-- 这里的 alarm 是 Alarm 对象 from AlarmScreen.kt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 在点击整个 Row 时切换选中状态
                                selectedAlarmIds = selectedAlarmIds.toMutableSet().apply {
                                    if (contains(alarm.id)) {
                                        remove(alarm.id)
                                    } else {
                                        add(alarm.id)
                                    }
                                }
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            // --- **正确访问 Alarm 对象的属性 from AlarmScreen.kt** ---
                            // Use label if available, otherwise show a placeholder or time
                            Text(alarm.label ?: "Alarm", fontSize = 16.sp)
                            Text(
                                // Access properties from the Alarm data class defined in AlarmScreen.kt
                                text = "时间: ${alarm.formattedTime} | ${alarm.repeatingDaysShortSummary} | ${if (alarm.isEnabled) "已启用" else "已禁用"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // ------------------------------------
                        }
                        Checkbox(
                            checked = selectedAlarmIds.contains(alarm.id),
                            onCheckedChange = { isChecked ->
                                // 在 Checkbox 状态变化时切换选中状态
                                selectedAlarmIds = selectedAlarmIds.toMutableSet().apply {
                                    if (isChecked) {
                                        add(alarm.id)
                                    } else {
                                        remove(alarm.id)
                                    }
                                }
                            }
                        )
                    }
                    Divider()
                }
                // ------------------------------------
            }
        }
    }
}


// --- Preview (Updated to use Alarm data class from AlarmScreen.kt) ---
@Preview(showBackground = true)
@Composable
fun RuleAlarmSelectionScreenPreview() {
    MaterialTheme {
        // Create sample Alarm data list using the structure from AlarmScreen.kt
        val sampleAlarms = listOf(
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.AM) },
                label = "起床闹钟",
                isEnabled = true,
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 30); set(Calendar.AM_PM, Calendar.PM) },
                label = "午休闹钟",
                isEnabled = false,
                repeatingDays = emptySet()
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.PM) },
                label = "会议提醒",
                isEnabled = true,
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.PM) },
                label = "健身闹钟",
                isEnabled = true,
                repeatingDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            ),
        )
        // Example initial selected ID
        val initialSelected = setOf(sampleAlarms[0].id, sampleAlarms[2].id)

        RuleAlarmSelectionScreen(
            availableAlarms = sampleAlarms, // <-- Pass the sample Alarm list
            initialSelectedAlarmIds = initialSelected,
            onBackClick = { println("Preview: Back Clicked") },
            onAlarmsSelected = { selectedIds -> println("Preview: Alarms Selected: $selectedIds") }
        )
    }
}
