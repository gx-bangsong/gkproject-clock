package com.gkprojct.clock

// Import shared definitions from SharedDefinitions.kt
// Ensure these imports are correct based on your SharedDefinitions.kt file

// Import Shared Composable and constants from SharedDefinitions.kt
// REMOVE any duplicate definitions or placeholders if they exist in this file
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.UUID


// --- Data Class to represent an Alarm ---
// This is the single, correct definition for the Alarm data class.
data class Alarm(
    val id: UUID = UUID.randomUUID(),
    val time: Calendar, // <-- Correctly using Calendar
    val label: String? = null,
    val isEnabled: Boolean,
    val repeatingDays: Set<DayOfWeek> = emptySet(), // Using java.time.DayOfWeek
    val sound: String = "Default (Cesium)", // Example sound name
    val vibrate: Boolean = true,
    var isExpanded: Boolean = false, // Keep track of expanded state
    val appliedRules: List<String> = emptyList() // Keep track of applied rules
) {
    // Use SimpleDateFormat to format the time from the Calendar object
    val formattedTime: String
        get() = SimpleDateFormat("h:mm", Locale.getDefault()).format(time.time)
    val amPm: String
        get() = SimpleDateFormat("a", Locale.getDefault()).format(time.time).uppercase(Locale.getDefault())

    // Summary for repeating days
    val repeatingDaysShortSummary: String
        get() {
            if (repeatingDays.isEmpty()) return "Never"
            if (repeatingDays.size == 7) return "Every day"

            val weekendDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
            if (repeatingDays == weekendDays) return "Weekends"
            val weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
            if (repeatingDays == weekdays) return "Weekdays"

            // Sort by DayOfWeek enum order (MON to SUN)
            return repeatingDays.sorted().joinToString(", ") {
                it.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }
        }
}


// --- Main Alarm Screen Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(onSettingsClick: () -> Unit) { // <-- onSettingsClick 参数从这里接收
    // Sample alarm data (replace with your actual data source/ViewModel)
    val alarms = remember {
        mutableStateListOf(
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 30); set(Calendar.AM_PM, Calendar.AM) }, // <-- Pass Calendar object directly
                isEnabled = true,
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                // isExpanded = true // Keep collapsed initially for cleaner look
                appliedRules = listOf("课程日历无课时暂停") // Example: apply a rule by default
            ),
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.PM) }, // <-- Pass Calendar object directly
                isEnabled = false,
                repeatingDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                appliedRules = listOf("假日暂停") // Example: apply another rule by default
            ),
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.AM) }, // <-- Pass Calendar object directly
                isEnabled = false,
                label = "Wake up"
            )
        )
    }

    // State to control the display of the Rule Selection Dialog - MOVED OUTSIDE LAZYCOLUMN
    var showRuleSelectionDialog by remember { mutableStateOf(false) }
    // State to hold the alarm for which the dialog is shown - MOVED OUTSIDE LAZYCOLUMN
    var selectedAlarmForRules by remember { mutableStateOf<Alarm?>(null) }
//    var showMenu by remember { mutableStateOf(false) } // 这个状态已经移到 MoreOptionsSettingsAction 内部了，这里不需要了


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm") },
                actions = {
                    MoreOptionsSettingsAction(onSettingsClick = onSettingsClick) // <-- Directly pass the parameter
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Handle Add Alarm (Navigate to Add/Edit screen) */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
        // bottomBar is now handled by the root AppContent in MainActivity
    )
    { paddingValues ->
        // The content of the Alarm screen goes here, applying the padding
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold (includes top bar and implicit bottom bar space)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), // Padding around the list items
            verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between alarm items
        ) {
            items(alarms, key = { it.id }) { alarm ->
                // Find the index to update the original list (mutableStateListOf makes this observable)
                val index = alarms.indexOfFirst { it.id == alarm.id }
                if (index != -1) { // Ensure index is valid
                    // Remember the state for the individual alarm item within the list
                    var currentAlarmState by remember { mutableStateOf(alarm) }

                    // Sync state changes back to the main list
                    // This ensures UI updates trigger recomposition of the list item
                    LaunchedEffect(currentAlarmState) {
                        alarms[index] = currentAlarmState // Update the item in the main list
                    }

                    // --- Corrected and complete call to AlarmItem ---
                    AlarmItem(
                        alarm = currentAlarmState, // Pass the alarm data object
                        onToggle = { isEnabled: Boolean -> // Explicitly type for clarity
                            // When the switch is toggled, update the isEnabled state of this alarm item
                            currentAlarmState = currentAlarmState.copy(isEnabled = isEnabled)
                        },
                        onExpandedChange = { isExpanded: Boolean -> // Explicitly type for clarity
                            // When the item is clicked, update its expanded state
                            currentAlarmState = currentAlarmState.copy(isExpanded = isExpanded)
                        },
                        onDayToggle = { day: DayOfWeek -> // Explicitly type for clarity
                            // When a day button is toggled, update the repeatingDays set
                            val newDays = currentAlarmState.repeatingDays.toMutableSet()
                            if (newDays.contains(day)) {
                                newDays.remove(day)
                            } else {
                                newDays.add(day)
                            }
                            currentAlarmState =
                                currentAlarmState.copy(repeatingDays = newDays.toSet()) // Update the set, convert back to immutable Set
                        },
                        onLabelClick = {
                            /* TODO: Handle label click (e.g., show dialog to edit label) */
                            println("Label clicked for alarm ${alarm.id}") // Placeholder action
                        },
                        onSoundClick = {
                            /* TODO: Handle sound click (e.g., navigate to sound picker screen) */
                            println("Sound clicked for alarm ${alarm.id}") // Placeholder action
                        },
                        onVibrateToggle = { vibrate: Boolean -> // Explicitly type for clarity
                            // When vibrate checkbox is toggled, update the vibrate state
                            currentAlarmState = currentAlarmState.copy(vibrate = vibrate)
                        },
                        onDeleteClick = {
                            /* TODO: Show confirmation dialog before deleting */
                            println("Delete clicked for alarm ${alarm.id}") // Placeholder action
                            // Temporarily delete directly for testing
                            alarms.removeAt(index) // This modifies the main list, triggering recomposition
                        },
                        onRulesClick =  {
                            // 当点击规则选项时，设置屏幕级别的状态来显示对话框，并记住是哪个闹钟
                            selectedAlarmForRules = currentAlarmState // Remember which alarm was clicked
                            showRuleSelectionDialog = true // Show the dialog
                        }
                    )
                }
            }
        }

        // --- Rule Selection Dialog (Keep this part here) ---
        // 根据屏幕级别的状态显示规则选择对话框
        if (showRuleSelectionDialog && selectedAlarmForRules != null) {
            // Define a list of available rules (for now, hardcoded examples)
            // In a real app, this list would come from your settings/data source
            val availableRules = remember {
                listOf("假日暂停", "周末暂停", "课程日历无课时暂停", "国定假日暂停") // Example rules
            }
            RuleSelectionDialog(
                alarm = selectedAlarmForRules!!, // Pass the selected alarm
                availableRules = availableRules, // <-- Pass the list of available rules
                onDismiss = {
                    showRuleSelectionDialog = false // Hide the dialog on dismiss
                    selectedAlarmForRules = null // Clear the selected alarm
                },
                onRulesUpdated = { updatedAlarm -> // <-- Modified callback name and signature
                    // When rules are updated in the dialog, update the corresponding alarm in the main list
                    val updatedIndex = alarms.indexOfFirst { it.id == updatedAlarm.id }
                    if (updatedIndex != -1) {
                        alarms[updatedIndex] = updatedAlarm // Update the alarm in the main list
                    }
                    // Note: The dialog doesn't automatically close after selecting,
                    // user needs to click "确定" or outside the dialog.
                    // If you want it to close after selection, call onDismiss here.
                    // onDismiss() // Uncomment this line if you want the dialog to close on selection
                }
            )
        }
    }
}

// --- Composable for a Single Alarm Item ---
@OptIn(ExperimentalMaterial3Api::class) // Surface with onClick needs this
@Composable
fun AlarmItem(
    alarm: Alarm, // <-- Receive the Alarm data object
    onToggle: (Boolean) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onDayToggle: (DayOfWeek) -> Unit,
    onLabelClick: () -> Unit,
    onSoundClick: () -> Unit,
    onVibrateToggle: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onRulesClick: () -> Unit, // <-- New callback for Rules click
    modifier: Modifier = Modifier
) {
    // Use Surface for background color and elevation/border if desired
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Rounded corners
        tonalElevation = 2.dp, // Slight elevation like a card
        // border = BorderStroke(1.dp, Color.Gray) // Optional border

        // Make the Surface clickable to expand/collapse, but exclude the Switch inside
        // We handle the click explicitly on the Column below
        // onClick = { onExpandedChange(!alarm.isExpanded) } // Don't use onClick here directly on Surface if Column is clickable
    ) {
        Column(
            // Make the column clickable to expand/collapse
            modifier = Modifier
                .clickable(onClick = { onExpandedChange(!alarm.isExpanded) }) // Handle expand/collapse click here
                .padding(horizontal = 16.dp, vertical = 12.dp) // Inner padding
        ) {
            // Top Row: Time, Label/Days, Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time and AM/PM
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.formattedTime,
                        fontSize = 36.sp, // Large time
                        fontWeight = FontWeight.Light // Lighter weight often used for clocks
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = alarm.amPm,
                        fontSize = 16.sp, // Smaller AM/PM
                        modifier = Modifier.padding(bottom = 4.dp) // Align with bottom of time
                    )
                }

                // This Spacer pushes the switch to the end
                Spacer(Modifier.weight(1f))

                // Switch for enabling/disabling the alarm
                // Handle the click explicitly on the Switch itself, preventing ripple on Column
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle, // Use the provided callback
                    modifier = Modifier.clickable(
                        onClick = { onToggle(!alarm.isEnabled) }, // Explicitly call onToggle here
                        indication = null, // Prevent ripple effect on the switch click itself
                        interactionSource = remember { MutableInteractionSource() } // Needed for clickable without default indication
                    )
                )
            }

            Spacer(Modifier.height(4.dp))

            // Label and/or Repeating Days Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alarm.label ?: alarm.repeatingDaysShortSummary, // Show label or days summary
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Slightly muted color
                )
                Spacer(Modifier.weight(1f))
                // Icon to indicate expand/collapse state
                Icon(
                    imageVector = if (alarm.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (alarm.isExpanded) "Collapse alarm details" else "Expand alarm details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // --- Expanded Content ---
            if (alarm.isExpanded) {
                Spacer(Modifier.height(16.dp))
                Divider() // Separator
                Spacer(Modifier.height(16.dp))

                // Day Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Distribute days evenly
                ) {
                    // Use the ordered list of DayOfWeek enums from SharedDefinitions
                    // Ensure shortDayNamesOrder and dayOfWeekToShortName are imported
                    // Ensure DayButton is imported
                    shortDayNamesOrder.forEach { dayOfWeek ->
                        DayButton( // Use the shared DayButton composable
                            text = dayOfWeekToShortName[dayOfWeek] ?: "?", // Get short name from map
                            isSelected = alarm.repeatingDays.contains(dayOfWeek), // Check if this day is in the repeatingDays set
                            onClick = { onDayToggle(dayOfWeek) } // Call the onDayToggle callback with the specific DayOfWeek
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Options List
                // Use the shared AlarmOptionItem composable for each setting
                // Ensure AlarmOptionItem is imported
                AlarmOptionItem(
                    icon = Icons.Default.Label,
                    text = alarm.label ?: "Add label",
                    onClick = onLabelClick // Call the onLabelClick callback
                )
                // Sound option
                AlarmOptionItem(
                    icon = Icons.Default.MusicNote,
                    text = alarm.sound, // Show the current sound
                    hasMoreAction = true, // Indicates navigation
                    onClick = onSoundClick // Call the onSoundClick callback
                )
                // Vibrate option
                AlarmOptionItem(
                    icon = Icons.Default.Vibration,
                    text = "Vibrate",
                    content = { // Use the content slot for the Checkbox
                        Checkbox(
                            checked = alarm.vibrate, // Use the alarm's vibrate state
                            onCheckedChange = onVibrateToggle // Call the onVibrateToggle callback
                        )
                    },
                    // Make the row clickable to toggle the checkbox
                    onClick = { onVibrateToggle(!alarm.vibrate) }
                )
                // Add other options similarly if needed (e.g., Google Assistant Routine)
                AlarmOptionItem(
                    icon = Icons.Default.Policy, // 使用 Policy 或其他合适的图标
                    text = "规则", // 显示文本 "规则"
                    description = if (alarm.appliedRules.isEmpty()) "未应用规则" else "${alarm.appliedRules.size} 条规则", // 显示已应用的规则数量或状态
                    hasMoreAction = true, // 指示点击可以进入下一界面
                    onClick = onRulesClick // <-- 调用新的回调参数
                )
                // Delete option
                AlarmOptionItem(
                    icon = Icons.Default.Delete,
                    text = "Delete",
                    onClick = onDeleteClick // Call the onDeleteClick callback
                )

            }
        }
    }
}

// --- Composable for Rule Selection Dialog/Screen (Placeholder) ---
// This can remain in AlarmScreen.kt for now, or be moved to a separate file later
@SuppressLint("MutableCollectionMutableState")
@Composable
fun RuleSelectionDialog(
    alarm: Alarm, // 接收要应用规则的闹钟
    availableRules: List<String>, // <-- Add parameter: list of all available rules
    onDismiss: () -> Unit, // Callback to dismiss the dialog
    onRulesUpdated: (Alarm) -> Unit // <-- Modified callback: returns the updated Alarm object
) {
    // In the dialog, manage the currently selected rules (based on the incoming alarm.appliedRules)
    var currentAppliedRules by remember(alarm.appliedRules) { // Re-initialize state when alarm.appliedRules changes
        mutableStateOf(alarm.appliedRules.toMutableSet())
    }

    AlertDialog( // Use AlertDialog as an example, you could also use a full screen
        onDismissRequest = onDismiss,
        title = { Text("应用规则到闹钟") },
        text = {
            Column {
                Text("闹钟时间: ${alarm.formattedTime} ${alarm.amPm}")
                Spacer(Modifier.height(8.dp))
                Text("已应用的规则:")
                if (currentAppliedRules.isEmpty()) { // Use currentAppliedRules state
                    Text("无")
                } else {
                    currentAppliedRules.forEach { rule -> // Use currentAppliedRules state
                        Text("- $rule")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("可用规则 (示例):")
                // TODO: List available rules here (e.g., fetched from settings/data source)
                val availableRulesList = remember { listOf("假日暂停", "周末暂停", "课程日历无课时暂停") } // Example list

                // Basic example of listing available rules and a dummy apply button
                availableRulesList.forEach { ruleId -> // Use availableRulesList
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("- $ruleId")
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = {
                                // Example: Add the rule to the alarm's appliedRules
                                // Update the local state and call the parent callback
                                if (!currentAppliedRules.contains(ruleId)) {
                                    val newRules = currentAppliedRules.plus(ruleId).toMutableSet() // Update local state first
                                    currentAppliedRules = newRules
                                    val updatedAlarm = alarm.copy(appliedRules = newRules.toList())
                                    onRulesUpdated(updatedAlarm) // Call the callback to update the alarm in the main list
                                } else {
                                    println("Rule $ruleId already applied") // Log if already applied
                                }
                            },
                            enabled = !currentAppliedRules.contains(ruleId) // Disable if already applied
                        ) {
                            Text(if (currentAppliedRules.contains(ruleId)) "已应用" else "应用") // Use currentAppliedRules state
                        }
                    }
                }


                // TODO: Add UI to remove rules
                if (currentAppliedRules.isNotEmpty()) { // Use currentAppliedRules state
                    Spacer(Modifier.height(16.dp))
                    Text("移除规则 (示例):")
                    currentAppliedRules.forEach { ruleId -> // Use currentAppliedRules state
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("- $ruleId")
                            Spacer(Modifier.weight(1f))
                            Button(onClick = {
                                // Example: Remove the rule from the alarm's appliedRules
                                // Update the local state and call the parent callback
                                val newRules = currentAppliedRules.minus(ruleId).toMutableSet() // Update local state first
                                currentAppliedRules = newRules
                                val updatedAlarm = alarm.copy(appliedRules = newRules.toList())
                                onRulesUpdated(updatedAlarm) // Call the callback to update the alarm in the main list
                            }) {
                                Text("移除")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { // Click confirm button to dismiss the dialog
                Text("确定")
            }
        }
        // Optional: dismissButton if you want a dedicated "Cancel" button
    )
}


// --- Preview ---
// Keep your Preview functions here
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Alarm Screen Dark") // Dark mode preview
@Composable
fun AlarmScreenPreview() {
    // Wrap preview in a theme (e.g., ClockTheme)
    // If Theme is not available here, wrap in MaterialTheme
    MaterialTheme {
        // Provide an empty lambda for onSettingsClick in preview
        AlarmScreen(onSettingsClick = { /* Preview doesn't navigate */ }) // Corrected lambda
    }
}

@Preview(showBackground = true, name = "Alarm Screen Light") // Light mode preview
@Composable
fun AlarmScreenPreviewLight() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        // Provide an empty lambda for onSettingsClick in preview
        AlarmScreen(onSettingsClick = { /* Preview doesn't navigate */ }) // Corrected lambda
    }
}

// --- Preview for Rule Selection Dialog ---
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Rule Dialog Dark")
@Composable
fun RuleSelectionDialogPreviewDark() {
    MaterialTheme { // Or your app's theme
        val sampleAlarm = remember {
            mutableStateOf(
                Alarm(
                    time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 30); set(Calendar.AM_PM, Calendar.AM) }, // <-- Pass Calendar object directly
                    isEnabled = true,
                    appliedRules = listOf("假日暂停", "周末暂停") // Example applied rules
                ))
        }
        RuleSelectionDialog(
            alarm = sampleAlarm.value,
            availableRules = listOf("假日暂停", "周末暂停", "课程日历无课时暂停", "国定假日暂停"), // Pass example rules for preview
            onDismiss = {},
            onRulesUpdated = { updatedAlarm -> // Corrected callback name and signature
                sampleAlarm.value = updatedAlarm.copy() // Update the sample alarm state in preview
                println("Preview: Applied/Removed rule. Updated rules: ${updatedAlarm.appliedRules}")
            }
        )
    }
}

@Preview(showBackground = true, name = "Rule Dialog Light")
@Composable
fun RuleSelectionDialogPreviewLight() {
    MaterialTheme { // Or your app's theme
        val sampleAlarm = remember {
            mutableStateOf(
                Alarm(
                    time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 30); set(Calendar.AM_PM, Calendar.AM) }, // <-- Pass Calendar object directly
                    isEnabled = true,
                    appliedRules = emptyList() // Example no applied rules
                ))
        }
        RuleSelectionDialog(
            alarm = sampleAlarm.value,
            availableRules = listOf("假日暂停", "周末暂停", "课程日历无课时暂停", "国定假日暂停"), // Pass example rules for preview
            onDismiss = {},
            onRulesUpdated = { updatedAlarm -> // Corrected callback name and signature
                sampleAlarm.value = updatedAlarm.copy() // Update the sample alarm state in preview
                println("Preview: Applied/Removed rule. Updated rules: ${updatedAlarm.appliedRules}")
            }
        )
    }
}
