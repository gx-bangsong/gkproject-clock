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
import java.time.temporal.ChronoUnit // For calculating duration

// Import shared definitions from SharedDefinitions.kt
// Adjust import if you named your shared file differently
import com.gkprojct.clock.dayOfWeekToShortName // Assuming you added this map to SharedDefinitions
import com.gkprojct.clock.shortDayNamesOrder // Assuming you added this list to SharedDefinitions
import com.gkprojct.clock.DayButton // Assuming you added this composable to SharedDefinitions
import com.gkprojct.clock.AlarmOptionItem // Assuming you added this composable to SharedDefinitions


@Composable
fun SetBedtimeScreen(
    onSkip: () -> Unit, // Callback for the Skip button
    onDone: () -> Unit, // Callback for the Done button
    // In a real app, you might pass wakeUpTime here to calculate duration
    wakeUpTime: LocalTime = LocalTime.of(7, 0) // Example default wake up time (should come from previous screen)
) {
    // State for the bedtime picker
    var bedtime by remember { mutableStateOf(LocalTime.of(23, 0)) } // 11:00 PM

    // State for selected days (reuse logic from wake up screen)
    // Example: initially select weekdays
    var selectedDays by remember { mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) }

    // State for settings display (simplifying based on screenshot text)
    var reminderSettingText by remember { mutableStateOf("15 minutes before bedtime") } // TODO: Make configurable
    var bedtimeModeStatusText by remember { mutableStateOf("Disabled") } // TODO: Make configurable

    // TODO: Calculate duration between bedtime and wakeUpTime accurately handling date rollovers
    val durationHours = ChronoUnit.HOURS.between(bedtime, wakeUpTime).let {
        if (it < 0) it + 24 else it // Handle bedtime being later than wake up time
    }
    val durationMinutes = ChronoUnit.MINUTES.between(bedtime.plusHours(durationHours.toLong()), wakeUpTime).let {
        if (it < 0) it + 60
        else it // Handle cases where minute calculation is negative (shouldn't happen with hour handling but safety)
//        else it
    }
    val durationText = if (durationMinutes == 0L) "$durationHours hours" else "$durationHours hours $durationMinutes minutes" // Refine duration text based on minutes


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
                imageVector = Icons.Default.NightsStay, // Moon/Sleep icon
                contentDescription = "Bedtime icon",
                modifier = Modifier.size(48.dp), // Adjust size
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Muted color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Set bedtime and silence your device",
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
                bedtime = bedtime.minusMinutes(1) // Decrement time
            }) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease bedtime")
            }

            // Time display (Hour:Minute AM/PM) + Duration
            Column(horizontalAlignment = Alignment.CenterHorizontally) { // Column to stack time and duration
                Row(verticalAlignment = Alignment.Bottom) { // Align AM/PM with the bottom of the hour/minute
                    val formatter = remember { DateTimeFormatter.ofPattern("h:mm") }
                    val ampmFormatter = remember { DateTimeFormatter.ofPattern("a", Locale.getDefault()) }

                    Text(
                        text = bedtime.format(formatter), // Format as "11:00"
                        fontSize = 72.sp, // Large font for time
                        fontWeight = FontWeight.Light // Light weight
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bedtime.format(ampmFormatter).uppercase(Locale.getDefault()), // Format as "AM" or "PM", ensure uppercase
                        fontSize = 24.sp, // Smaller font for AM/PM
                        modifier = Modifier.padding(bottom = 8.dp) // Align with bottom of large text
                    )
                }
                // Duration display
                Text(
                    text = durationText, // Use the formatted duration text
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // Plus button
            IconButton(onClick = {
                bedtime = bedtime.plusMinutes(1) // Increment time
            }) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase bedtime")
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
            // Reminder notification
            AlarmOptionItem(
                icon = Icons.Default.Notifications, // Bell icon for reminder
                text = "Reminder notification",
                description = reminderSettingText, // Show current setting
                hasMoreAction = true, // Shows arrow for navigation
                onClick = { /* TODO: Navigate to reminder settings */ }
            )
            Divider() // Separator

            // Bedtime mode
            AlarmOptionItem(
                icon = Icons.Default.KingBed, // Bed icon for Bedtime mode
                text = "Bedtime mode",
                description = bedtimeModeStatusText, // Show current status (e.g., Disabled)
                hasMoreAction = true, // Shows arrow for navigation
                onClick = { /* TODO: Navigate to Bedtime mode settings */ }
            )
            // No divider after the last item in the screenshot
        }

        // --- Space to push buttons to the bottom ---
        Spacer(modifier = Modifier.weight(1f))

        // --- Bottom Buttons (Skip and Done) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Padding above and below buttons
            horizontalArrangement = Arrangement.SpaceBetween // Space between Skip and Done
        ) {
            // Skip button
            OutlinedButton(
                onClick = onSkip // Use the provided callback
            ) {
                Text("Skip")
            }

            // Done button
            Button(
                onClick = onDone // Use the provided callback
            ) {
                Text("Done")
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun SetBedtimeScreenPreview() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        SetBedtimeScreen(onSkip = {}, onDone = {}) // Pass empty lambdas for preview
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SetBedtimeScreenPreviewDark() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        SetBedtimeScreen(onSkip = {}, onDone = {}) // Pass empty lambdas for preview
    }
}