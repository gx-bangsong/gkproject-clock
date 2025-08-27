package com.gkprojct.clock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import java.time.LocalTime // Use java.time for time
import java.time.format.DateTimeFormatter // Use java.time for time formatting
import java.time.DayOfWeek // Use java.time.DayOfWeek
import java.util.Locale // For localization
import java.time.temporal.ChronoUnit // For calculating duration


// Import shared definitions from SharedDefinitions.kt
// Adjust import if you named your shared file differently


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeScreen(onSettingsClick: () -> Unit) { // Main Bedtime screen after setup

    // Note: Bottom navigation state and composable are now in MainActivity/AppContent

    // TODO: Fetch actual bedtime and wake-up time from saved settings
    // For now, using placeholder values
    val bedtime = LocalTime.of(23, 0) // Example 11:00 PM
    val wakeUpTime = LocalTime.of(7, 0) // Example 7:00 AM

    // TODO: Calculate duration accurately
    val durationHours = ChronoUnit.HOURS.between(bedtime, wakeUpTime).let {
        if (it < 0) it + 24 else it
    }
    val durationMinutes = ChronoUnit.MINUTES.between(bedtime.plusHours(durationHours.toLong()), wakeUpTime).let {
        if (it < 0) it + 60
        else it
    }
    val durationText = if (durationMinutes == 0L) "$durationHours hours" else "$durationHours hours $durationMinutes minutes"


    // TODO: Fetch next alarm day based on setup settings
    val nextAlarmDay = DayOfWeek.SUNDAY // Example


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bedtime") },
                actions = {
                    MoreOptionsSettingsAction(onSettingsClick = onSettingsClick)
                }
            )
        },
        // No FAB on the main Bedtime screen after setup
        // bottomBar is now handled by the root AppContent in MainActivity
    ) { paddingValues ->
        // Main content area - a scrollable column of cards/sections
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp), // Padding around the content
            verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between cards/sections
        ) {
            item {
                // --- Schedule Card ---
                ScheduleCard(
                    bedtime = bedtime, // Use placeholder/fetched data
                    wakeUpTime = wakeUpTime, // Use placeholder/fetched data
                    durationText = durationText, // Use calculated duration text
                    nextAlarmDay = nextAlarmDay, // Use placeholder/fetched data
                    onClick = { /* TODO: Handle Schedule card click (likely navigate to edit schedule) */ }
                )
            }

            item {
                // --- Recent Bedtime Activity Card ---
                BedtimeActivityCard(
                    onNoThanksClick = { /* TODO */ },
                    onResumeClick = { /* TODO */ } // Changed from Continue
                )
            }

            item {
                // --- Listen to Sleep Sounds Card ---
                SleepSoundsCard(
                    onChooseSoundClick = { /* TODO: Navigate to sound picker */ }
                )
            }

            item {
                // --- See your upcoming events Card ---
                UpcomingEventsCard(
                    onClick = { /* TODO: Navigate to events */ }
                )
            }

            // Add more items/cards here if there are other sections
        }
    }
}


// --- Composable for Schedule Card (Used in BedtimeScreen) ---
@OptIn(ExperimentalMaterial3Api::class) // Surface with onClick needs this
@Composable
fun ScheduleCard(
    bedtime: LocalTime,
    wakeUpTime: LocalTime,
    durationText: String, // Accept formatted duration text
    nextAlarmDay: DayOfWeek,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium, // Rounded corners
        tonalElevation = 2.dp, // Slight elevation
        onClick = onClick // Make the whole card clickable
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule, // Clock/Schedule icon
                    contentDescription = "Schedule",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Schedule", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))

            // BEDTIME vs WAKE-UP labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround // Space out labels
            ) {
                Text(text = "BEDTIME", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "WAKE-UP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))

            // Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround, // Space out times
                verticalAlignment = Alignment.Bottom // Align AM/PM at the bottom
            ) {
                val formatter = remember { DateTimeFormatter.ofPattern("h:mm") }
                val ampmFormatter = remember { DateTimeFormatter.ofPattern("a", Locale.getDefault()) }

                // Bedtime (11:00 PM)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = bedtime.format(formatter),
                        fontSize = 48.sp, // Large time font
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = bedtime.format(ampmFormatter).uppercase(Locale.getDefault()), // Ensure uppercase
                        fontSize = 18.sp, // Smaller AM/PM
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Wake-up Time (7:00 AM)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = wakeUpTime.format(formatter),
                        fontSize = 48.sp, // Large time font
                        fontWeight = FontWeight.Light
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = wakeUpTime.format(ampmFormatter).uppercase(Locale.getDefault()), // Ensure uppercase
                        fontSize = 18.sp, // Smaller AM/PM
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Duration and Next Alarm Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center, // Center the text row
                verticalAlignment = Alignment.CenterVertically // Align items vertically
            ) {
                Text(
                    text = durationText, // Use the formatted duration text
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) // Match font size
                Text(
                    text = "Next alarm on ${nextAlarmDay.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())}", // Get full localized name
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- Composable for Recent Bedtime Activity Card (Used in BedtimeScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BedtimeActivityCard(
    onNoThanksClick: () -> Unit,
    onResumeClick: () -> Unit // Changed from onContinueClick
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BarChart, // Bar chart icon for activity
                    contentDescription = "Bedtime activity",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "See recent bedtime activity", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = "Keep track of your screen time and see estimates of time spent in bed. This is based on when your device remained motionless in a dark room.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Align buttons to the end
            ) {
                TextButton(onClick = onNoThanksClick) {
                    Text("No thanks")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onResumeClick) {
                    Text("Continue")
                }
            }
        }
    }
}

// --- Composable for Listen to Sleep Sounds Card (Used in BedtimeScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepSoundsCard(
    onChooseSoundClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote, // Music icon
                    contentDescription = "Sleep sounds",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Listen to sleep sounds", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = "You can play soothing music to help you fall asleep. Sleep sounds don't play automatically.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End // Align button to the end
            ) {
                Button(onClick = onChooseSoundClick) {
                    Text("Choose a sound")
                }
            }
        }
    }
}


// --- Composable for See your upcoming events Card (Used in BedtimeScreen) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingEventsCard(
    onClick: () -> Unit // Make the card clickable to view events
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        onClick = onClick // Make the whole card clickable
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Icon and Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event, // Calendar/Event icon
                    contentDescription = "Upcoming events",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "See your upcoming events", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = "To make sure your alarm is set before any events,", // Text ends with comma in screenshot
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Note: Screenshot description is incomplete, it ends with a comma.
            // In a real app, this text would likely continue or be different.
        }
    }
}


// --- Preview ---
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Bedtime Screen (Setup Done) Dark")
@Composable
fun BedtimeScreenPreviewAfterSetup() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        BedtimeScreen { var showSettingsScreen = true } // Preview the screen composable
    }
}

@Preview(showBackground = true, name = "Bedtime Screen (Setup Done) Light")
@Composable
fun BedtimeScreenPreviewAfterSetupLight() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        BedtimeScreen { var showSettingsScreen = true } // Preview the screen composable
    }
}