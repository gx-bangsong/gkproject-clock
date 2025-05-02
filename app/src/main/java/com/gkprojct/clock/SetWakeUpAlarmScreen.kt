package com.gkprojct.clock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime // Use java.time for time
import java.time.format.DateTimeFormatter // Use java.time for time formatting
import java.time.DayOfWeek // Use java.time.DayOfWeek
import java.util.Locale // For localization


// Import shared definitions from SharedDefinitions.kt
import com.gkprojct.clock.bottomNavItems // Assuming bottomNavItems is not needed here
import com.gkprojct.clock.dayOfWeekToShortName // Assuming you added this map to SharedDefinitions
import com.gkprojct.clock.shortDayNamesOrder // Assuming you added this list to SharedDefinitions
import com.gkprojct.clock.DayButton // Assuming you added this composable to SharedDefinitions
import com.gkprojct.clock.AlarmOptionItem // Assuming you added this composable to SharedDefinitions


@Composable
fun SetWakeUpAlarmScreen(
    onSkip: () -> Unit, // Callback for the Skip button
    onNext: () -> Unit // Callback for the Next button
) {
    // State for the time picker
    var wakeUpTime by remember { mutableStateOf(LocalTime.of(7, 0)) }

    // State for selected days (Use Set<DayOfWeek> consistently)
    // Initialize with example days (e.g., Weekdays)
    var selectedDays by remember {
        mutableStateOf(setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        ))
    }

    // State for settings toggles/values
    var isSunriseAlarmEnabled by remember { mutableStateOf(false) }
    var selectedSound by remember { mutableStateOf("Default (Cesium)") } // TODO: Replace with actual sound state/object
    var isVibrateEnabled by remember { mutableStateOf(true) } // Screenshot looks enabled


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Top Section (Icon and Title) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Space from top
            Icon(
                imageVector = Icons.Default.Alarm, // Alarm icon
                contentDescription = "Alarm icon",
                modifier = Modifier.size(48.dp), // Adjust size
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Muted color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set a regular wake-up alarm",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Center text
            )
            Spacer(modifier = Modifier.height(32.dp)) // Space before time picker
        }


        // --- Time Picker ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minus button
            IconButton(onClick = {
                wakeUpTime = wakeUpTime.minusMinutes(1) // Decrement time
            }) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease time")
            }

            // Time display (Hour:Minute AM/PM)
            Row(verticalAlignment = Alignment.Bottom) { // Align AM/PM with the bottom of the hour/minute
                val formatter = remember { DateTimeFormatter.ofPattern("h:mm") }
                val ampmFormatter = remember { DateTimeFormatter.ofPattern("a", Locale.getDefault()) }

                Text(
                    text = wakeUpTime.format(formatter), // Format as "7:00"
                    fontSize = 72.sp, // Large font
                    fontWeight = FontWeight.Light // Light weight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = wakeUpTime.format(ampmFormatter).uppercase(Locale.getDefault()), // Format as "AM" or "PM", ensure uppercase
                    fontSize = 24.sp, // Smaller font for AM/PM
                    // color = MaterialTheme.colorScheme.onSurfaceVariant, // Muted color
                    modifier = Modifier.padding(bottom = 8.dp) // Align with bottom of large text
                )
            }


            // Plus button
            IconButton(onClick = {
                wakeUpTime = wakeUpTime.plusMinutes(1) // Increment time
            }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase time")
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Space after time picker

        // --- Day Selector ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly // Evenly space buttons
        ) {
            // Iterate through the ordered list of DayOfWeek enums from SharedDefinitions
            shortDayNamesOrder.forEach { dayOfWeek ->
                DayButton(
                    // Get the short name for the UI button from the map in SharedDefinitions
                    text = dayOfWeekToShortName[dayOfWeek] ?: "?", // Use map, provide fallback
                    isSelected = selectedDays.contains(dayOfWeek), // Check if the DayOfWeek enum is selected
                    onClick = {
                        // Toggle the selection state for this specific DayOfWeek enum
                        selectedDays = if (selectedDays.contains(dayOfWeek)) {
                            selectedDays - dayOfWeek // Remove the enum from the set
                        } else {
                            selectedDays + dayOfWeek // Add the enum to the set
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp)) // Space before settings list

        // --- Settings List ---
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Sunrise Alarm
            AlarmOptionItem(
                icon = Icons.Default.WbSunny, // Using WbSunny for sun/sunrise
                text = "Sunrise Alarm",
                description = "Slowly brighten screen before alarm", // Added description param
                content = {
                    Switch(
                        checked = isSunriseAlarmEnabled,
                        onCheckedChange = { isSunriseAlarmEnabled = it }
                    )
                },
                // Make row clickable to toggle the switch
                onClick = { isSunriseAlarmEnabled = !isSunriseAlarmEnabled }
            )
            Divider() // Separator

            // Sound
            AlarmOptionItem(
                icon = Icons.Default.MusicNote, // Music note icon for sound
                text = "Sound",
                description = selectedSound, // Show selected sound as description
                hasMoreAction = true, // Shows arrow for navigation
                onClick = { /* TODO: Navigate to sound selection */ }
            )
            Divider() // Separator

            // Vibrate
            AlarmOptionItem(
                icon = Icons.Default.Vibration, // Vibration icon
                text = "Vibrate",
                content = {
                    Checkbox(
                        checked = isVibrateEnabled,
                        onCheckedChange = { isVibrateEnabled = it }
                    )
                },
                // Make row clickable to toggle the checkbox
                onClick = { isVibrateEnabled = !isVibrateEnabled }
            )
            Divider() // Separator

            // Google Assistant Routine
            AlarmOptionItem(
                icon = Icons.Default.MicNone, // Or Icons.Default.Assistant
                text = "Google Assistant Routine",
                hasMoreAction = true, // Shows arrow for navigation
                onClick = { /* TODO: Navigate to Assistant routine setup */ }
            )
            // No divider after the last item in the screenshot
        }

        // --- Space to push buttons to the bottom ---
        Spacer(modifier = Modifier.weight(1f))

        // --- Bottom Buttons (Skip and Next) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Padding above and below buttons
            horizontalArrangement = Arrangement.SpaceBetween // Space between Skip and Next
        ) {
            // Skip button
            OutlinedButton(
                onClick = onSkip // Use the provided callback
            ) {
                Text("Skip")
            }

            // Next button
            Button(
                onClick = onNext // Use the provided callback
            ) {
                Text("Next")
            }
        }
    }
}


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun SetWakeUpAlarmScreenPreview() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        SetWakeUpAlarmScreen(onSkip = {}, onNext = {}) // Pass empty lambdas for preview
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SetWakeUpAlarmScreenPreviewDark() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        SetWakeUpAlarmScreen(onSkip = {}, onNext = {}) // Pass empty lambdas for preview
    }
}