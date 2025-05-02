package com.gkprojct.clock

// Import shared definitions from SharedDefinitions.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


// --- TimerScreen related data (These were in your original TimerScreen code) ---
// Moving them here for now as per original code structure,
// but ideally TimerScreen would be in its own file.
// Data class for Quick Timer presets (Should be in TimerScreen.kt if it's a separate file)
//data class QuickTimerPreset(val label: String, val durationSeconds: Long)


// --- Clock Screen Composable ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockScreen(onSettingsClick: () -> Unit) {
    // TODO: Implement logic to display current real-time and date
    // For now, using placeholder values
    val currentTime = "3:41"
    val amPm = "PM"
    val currentDate = "Sat, Apr 19"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clock") },
                actions = {
                    MoreOptionsSettingsAction(onSettingsClick = onSettingsClick )
                }
            )
        },
        floatingActionButton = {
            // FAB for Clock screen (Add button)
            FloatingActionButton(onClick = { /* TODO: Handle FAB click (e.g., Add City) */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add City")
            }
        },
        floatingActionButtonPosition = FabPosition.Center // Center the FAB
        // bottomBar is now handled by the root AppContent in MainActivity
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Add horizontal padding
            horizontalAlignment = Alignment.Start // Align content to the start
        ) {
            // Use weight to push content down similar to screenshot
            Spacer(modifier = Modifier.weight(1f))

            // Current Time Display
            Row(
                verticalAlignment = Alignment.Bottom // Align AM/PM to the bottom of the time
            ) {
                Text(
                    text = currentTime,
                    fontSize = 72.sp, // Large font for time
                    fontWeight = FontWeight.Light // Light weight
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = amPm,
                    fontSize = 24.sp, // Smaller font for AM/PM
                    modifier = Modifier.padding(bottom = 8.dp) // Adjust alignment
                )
            }

            // Current Date Display
            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant // Muted color
            )

            // Use weight to push content up and center it roughly
            Spacer(modifier = Modifier.weight(2f))

            // TODO: Add World Clock list below the main time display if needed based on full app design
        }
    }
}


// --- Preview ---
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClockScreenPreviewDark() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        ClockScreen { var showSettingsScreen = true } // Preview the screen composable
    }
}

@Preview(showBackground = true)
@Composable
fun ClockScreenPreviewLight() {
    // Wrap preview in a theme (e.g., ClockTheme)
    MaterialTheme {
        ClockScreen { var showSettingsScreen = true } // Preview the screen composable
    }
}