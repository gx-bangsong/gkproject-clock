package com.gkprojct.clock

// Import shared definitions from SharedDefinitions.kt
// Ensure these imports are correct based on your SharedDefinitions.kt file
import com.gkprojct.clock.DayButton // Import DayButton
import com.gkprojct.clock.AlarmOptionItem // Import AlarmOptionItem
import com.gkprojct.clock.MoreOptionsSettingsAction // Import MoreOptionsSettingsAction
import com.gkprojct.clock.dayOfWeekToShortName // Import dayOfWeekToShortName if needed elsewhere
import com.gkprojct.clock.shortDayNamesOrder // Import shortDayNamesOrder if needed elsewhere

// Import Shared Composable and constants from SharedDefinitions.kt
// REMOVE any duplicate definitions or placeholders if they exist in this file
import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton // Add this for Dialog buttons
import androidx.compose.material3.TimePicker // Add this
import androidx.compose.material3.TimePickerLayoutType // Add this
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState // Add this
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Add missing import
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.map // Add missing import
import com.gkprojct.clock.vm.RuleCriteria
import com.gkprojct.clock.vm.RuleEntity // Import RuleEntity if needed for RuleSelectionDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalTime // Add potential missing import
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
    val appliedRules: List<String> = emptyList() // Keep track of applied rules (rule names)
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
fun AlarmScreen(
    onSettingsClick: () -> Unit,
    ruleViewModel: RuleViewModel // Assuming RuleViewModel is needed for rules
) { // <-- Added ruleViewModel parameter
    // Sample alarm data (replace with your actual data source/ViewModel)
    val alarms = remember {
        mutableStateListOf(
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 8); set(Calendar.MINUTE, 30); set(Calendar.AM_PM, Calendar.AM) },
                isEnabled = true,
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                appliedRules = listOf("课程日历无课时暂停")
            ),
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.PM) },
                isEnabled = false,
                repeatingDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
                appliedRules = listOf("假日暂停")
            ),
            Alarm(
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 6); set(Calendar.MINUTE, 0); set(Calendar.AM_PM, Calendar.AM) },
                isEnabled = false,
                label = "Wake up"
            )
        )
    }

    // State to control the display of the Rule Selection Dialog
    var showRuleSelectionDialog by remember { mutableStateOf(false) }
    // State to hold the alarm for which the dialog is shown
    var selectedAlarmForRules by remember { mutableStateOf<Alarm?>(null) }

    // --- State for Time Picker Dialog ---
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.getInstance().get(Calendar.MINUTE),
        is24Hour = false // Or use system setting
    )
    // -----------------------------------

    // --- Collect available rules from ViewModel --- (Corrected)
    val availableRules: List<Rule> by ruleViewModel.allRulesAsUiModel
        .collectAsState(initial = emptyList<Rule>()) // Specify type for initial value
    // --------------------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alarm") },
                actions = {
                    // Use the imported MoreOptionsSettingsAction
                    MoreOptionsSettingsAction(onSettingsClick = onSettingsClick)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Alarm")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                val index = alarms.indexOfFirst { it.id == alarm.id }
                if (index != -1) {
                    var currentAlarmState by remember { mutableStateOf(alarm) }

                    LaunchedEffect(currentAlarmState) {
                        if (alarms[index] != currentAlarmState) { // Only update if changed
                            alarms[index] = currentAlarmState
                        }
                    }

                    // --- Corrected and complete call to AlarmItem ---
                    AlarmItem(
                        alarm = currentAlarmState,
                        onToggleExpand = { currentAlarmState = currentAlarmState.copy(isExpanded = !currentAlarmState.isExpanded) },
                        onToggleEnable = { enabled -> currentAlarmState = currentAlarmState.copy(isEnabled = enabled) },
                        onDelete = { alarms.removeAt(index) }, // Simple delete for now
                        onLabelChange = { newLabel -> currentAlarmState = currentAlarmState.copy(label = newLabel.ifBlank { null }) },
                        onRepeatingDaysChange = { newDays -> currentAlarmState = currentAlarmState.copy(repeatingDays = newDays) },
                        onSoundChange = { /* TODO: Implement sound selection */ },
                        onVibrateChange = { vibrate -> currentAlarmState = currentAlarmState.copy(vibrate = vibrate) },
                        onRuleClick = { // Show rule selection dialog when rule section is clicked
                            selectedAlarmForRules = currentAlarmState
                            showRuleSelectionDialog = true
                        }
                    )
                    // -------------------------------------------------
                }
            }
        }

        // --- Time Picker Dialog --- (Placed outside LazyColumn)
        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedCalendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        // Add the new alarm to the list
                        alarms.add(
                            Alarm(
                                time = selectedCalendar,
                                isEnabled = true // Default to enabled
                                // Add other default properties as needed
                            )
                        )
                        showTimePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                TimePicker(
                    state = timePickerState,
                    layoutType = TimePickerLayoutType.Vertical // Or Horizontal
                )
            }
        }
        // ------------------------

        // --- Rule Selection Dialog --- (Placed outside LazyColumn)
        if (showRuleSelectionDialog && selectedAlarmForRules != null) {
            val currentAlarm = selectedAlarmForRules!! // Safe call due to check
            // Assuming appliedRules stores rule names (Strings)
            val initialSelectedRuleNames = currentAlarm.appliedRules.toSet()

            // Find the corresponding Rule objects for the initial names
            val initialSelectedRules = availableRules.filter { it.name in initialSelectedRuleNames }.map { it.id }.toSet()

            RuleSelectionDialog(
                showDialog = showRuleSelectionDialog,
                alarm = currentAlarm,
                availableRules = availableRules, // Pass the list of Rule objects
                initialSelectedRuleIds = initialSelectedRules, // Pass the Set<UUID> of initially selected rules
                onDismiss = { showRuleSelectionDialog = false },
                onRulesSelected = { selectedRuleIds ->
                    // Find the names of the selected rules
                    val selectedRuleNames = availableRules
                        .filter { it.id in selectedRuleIds }
                        .map { it.name }

                    // Update the alarm's appliedRules list
                    val index = alarms.indexOfFirst { it.id == currentAlarm.id }
                    if (index != -1) {
                        alarms[index] = alarms[index].copy(appliedRules = selectedRuleNames)
                    }
                    selectedAlarmForRules = null // Clear selection
                    showRuleSelectionDialog = false
                }
            )
        }
        // ---------------------------
    }
}

// --- Alarm Item Composable (Structure assumed, ensure it matches your definition) ---
@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggleExpand: () -> Unit,
    onToggleEnable: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onLabelChange: (String) -> Unit,
    onRepeatingDaysChange: (Set<DayOfWeek>) -> Unit,
    onSoundChange: (String) -> Unit, // Placeholder
    onVibrateChange: (Boolean) -> Unit,
    onRuleClick: () -> Unit // Callback when the rule section is clicked
) {
    // Use Surface for background color and elevation/border if desired
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Rounded corners
        tonalElevation = 2.dp, // Slight elevation like a card
    ) {
        Column(
            // Make the column clickable to expand/collapse
            modifier = Modifier
                .clickable(onClick = onToggleExpand) // Handle expand/collapse click here
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
                    onCheckedChange = onToggleEnable, // Use the provided callback
                    modifier = Modifier.clickable(
                        onClick = { onToggleEnable(!alarm.isEnabled) }, // Explicitly call onToggleEnable here
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


            // --- Expanded Section ---
            if (alarm.isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Repeating Days Buttons (Assuming DayButton exists)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Use a standard loop or Composable loop like FlowRow if needed
                    // Qualify shortDayNamesOrder if ambiguous
                    for (dayOfWeek in shortDayNamesOrder) { // Example qualification
                        DayButton(
                            text = dayOfWeekToShortName[dayOfWeek]!!,
                            isSelected = alarm.repeatingDays.contains(dayOfWeek),
                            onClick = {
                                val newDays = alarm.repeatingDays.toMutableSet()
                                if (newDays.contains(dayOfWeek)) {
                                    newDays.remove(dayOfWeek)
                                } else {
                                    newDays.add(dayOfWeek)
                                }
                                onRepeatingDaysChange(newDays)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Other Options (Assuming AlarmOptionItem exists)
                // Use the correct AlarmOptionItem definition (assuming one from preview or shared components)
                // Example using the one with 'checked'
                AlarmOptionItem(icon = Icons.Default.Label, text = alarm.label ?: "Add Label", onClick = { /* TODO: Show Label Dialog */ onLabelChange(alarm.label ?: "") })
                AlarmOptionItem(icon = Icons.Default.MusicNote, text = alarm.sound, onClick = { onSoundChange(alarm.sound) /* TODO: Show Sound Picker */ })
                AlarmOptionItem(icon = Icons.Default.Vibration, text = "Vibrate") {
                    Switch(checked = alarm.vibrate, onCheckedChange = { onVibrateChange(!alarm.vibrate) })
                }

                // --- Rules Section --- (Clickable to open dialog)
                AlarmOptionItem(
                    icon = Icons.Default.Policy,
                    text = if (alarm.appliedRules.isEmpty()) "Rules" else "Rules: ${alarm.appliedRules.joinToString()}",
                    onClick = onRuleClick // Trigger dialog on click
                )
                // ---------------------

                Spacer(Modifier.height(16.dp))

                // Delete Button
                Button(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Alarm")
                    Spacer(Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
}

// --- Rule Selection Dialog Composable (Structure assumed) ---
@Composable
fun RuleSelectionDialog(
    showDialog: Boolean,
    alarm: Alarm,
    availableRules: List<Rule>, // Now expects List<Rule>
    initialSelectedRuleIds: Set<UUID>, // Expects Set<UUID>
    onDismiss: () -> Unit,
    onRulesSelected: (Set<UUID>) -> Unit // Returns Set<UUID>
) {
    if (showDialog) {
        var currentSelectedIds by remember { mutableStateOf(initialSelectedRuleIds) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Rules for '${alarm.label ?: alarm.formattedTime}'") },
            text = {
                LazyColumn {
                    items(availableRules, key = { it.id }) { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSelectedIds = currentSelectedIds.toMutableSet().apply {
                                        if (contains(rule.id)) remove(rule.id)
                                        else add(rule.id)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = currentSelectedIds.contains(rule.id),
                                onCheckedChange = { isChecked ->
                                    currentSelectedIds = currentSelectedIds.toMutableSet().apply {
                                        if (isChecked) add(rule.id)
                                        else remove(rule.id)
                                    }
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(rule.name)
                                Text(
                                    rule.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onRulesSelected(currentSelectedIds) }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- Time Picker Dialog Wrapper --- (Helper Composable)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable (() -> Unit),
    dismissButton: @Composable (() -> Unit)? = null,
    containerColor: Color = colorScheme.surface, // Use Compose Color
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = containerColor // Use Compose Color
                ),
            color = containerColor // Use Compose Color
        ) {
            // Column needs to be inside the Surface's content lambda
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                content()
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AlarmScreenPreview() {
    // Create a dummy RuleDao and RuleViewModel for the preview
    val sampleRuleDao = object : com.gkprojct.clock.vm.RuleDao {
        private val sampleRulesFlow = MutableStateFlow(listOf(
            // Ensure RuleCriteria is imported or qualified
            com.gkprojct.clock.vm.RuleEntity(UUID.randomUUID(), "Holiday Pause", "Pause on holidays", true, emptySet(), setOf(1L), com.gkprojct.clock.RuleCriteria.IfCalendarEventExists(listOf("holiday"), 60), RuleAction.SkipNextAlarm), // Use correct RuleCriteria path
            com.gkprojct.clock.vm.RuleEntity(UUID.randomUUID(), "No Class Pause", "Pause if no class", false, emptySet(), setOf(2L), com.gkprojct.clock.RuleCriteria.BasedOnTime(LocalTime.of(9,0), LocalTime.of(17,0)), RuleAction.SkipNextAlarm) // Use correct RuleCriteria path
        ))
        override fun getAllRules(): Flow<List<com.gkprojct.clock.vm.RuleEntity>> = sampleRulesFlow
        override suspend fun getRuleById(ruleId: UUID): com.gkprojct.clock.vm.RuleEntity? = sampleRulesFlow.value.find { it.id == ruleId }
        override suspend fun insertRule(rule: com.gkprojct.clock.vm.RuleEntity) {}
        override suspend fun updateRule(rule: com.gkprojct.clock.vm.RuleEntity) {}
        override suspend fun deleteRule(rule: com.gkprojct.clock.vm.RuleEntity) {}
        override suspend fun deleteRuleById(ruleId: UUID) {}
    }
    val sampleViewModel = com.gkprojct.clock.RuleViewModel(sampleRuleDao)

    MaterialTheme {
        AlarmScreen(onSettingsClick = {}, ruleViewModel = sampleViewModel) // Pass dummy ViewModel
    }
}











// ... rest of the file ...
