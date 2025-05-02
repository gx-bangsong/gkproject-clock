package com.gkprojct.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gkprojct.clock.vm.Lap
import com.gkprojct.clock.vm.StopwatchState
import com.gkprojct.clock.vm.StopwatchViewModel

// Removed onSettingsClick parameter as it's not used in this version
@Composable
fun StopwatchScreen(stopwatchViewModel: StopwatchViewModel = viewModel()) {
    val elapsedTime by stopwatchViewModel.elapsedTime.collectAsState()
    val laps by stopwatchViewModel.laps.collectAsState()
    val stopwatchState by stopwatchViewModel.stopwatchState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Area: Timer Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Make it square
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            StopwatchCircle(elapsedTime)
            // Main Time Display
            TimeDisplay(elapsedTime, stopwatchViewModel::formatTime)
        }

        // Middle Area: Laps List (Scrollable)
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Takes remaining space
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Display laps only if not empty
            if (laps.isNotEmpty()) {
                itemsIndexed(laps) { index, lap ->
                    LapRow(lap = lap, index = laps.size - index, stopwatchViewModel::formatTime)
                    if (index < laps.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
            } else if (stopwatchState != StopwatchState.IDLE) {
                // Optional: Show a placeholder when running/paused but no laps yet
                item { 
                    Text(
                        text = "", // No laps recorded yet
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bottom Area: Control Buttons
        StopwatchControls(
            stopwatchState = stopwatchState,
            onStartClick = { stopwatchViewModel.startStopwatch() },
            onPauseClick = { stopwatchViewModel.pauseStopwatch() },
            onLapClick = { stopwatchViewModel.lapStopwatch() },
            onResetClick = { stopwatchViewModel.resetStopwatch() }
        )
    }
}

@Composable
fun StopwatchCircle(elapsedTime: Long) {
    val circleColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val strokeWidth = 8.dp

    Canvas(modifier = Modifier.fillMaxSize()) {
        val diameter = size.minDimension * 0.8f // Make circle slightly smaller than the box
        val radius = diameter / 2f
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val strokePx = strokeWidth.toPx()

        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius - strokePx / 2f,
            center = center,
            style = Stroke(width = strokePx)
        )

        // Foreground arc (representing seconds hand)
        // Use milliseconds for smoother animation if needed, here using seconds
        val secondsFraction = (elapsedTime % 60000) / 60000f // Fraction of a minute
        val sweepAngle = secondsFraction * 360f

        drawArc(
            color = circleColor,
            startAngle = -90f, // Start from the top
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(diameter, diameter),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun TimeDisplay(elapsedTime: Long, formatTime: (Long) -> String) {
    // Use the ViewModel's formatting logic directly for consistency
    val timeString = formatTime(elapsedTime)
    // Split to style parts differently if needed, or display as one string
    val mainTime = timeString.substringBeforeLast('.') // MM:SS
    val hundredths = timeString.substringAfterLast('.', "00") // ms

    Row(
        verticalAlignment = Alignment.CenterVertically, // Align main time and hundredths
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(bottom = 16.dp) // Add some padding below time
    ) {
        Text(
            text = mainTime, // Display MM:SS
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = ".${hundredths}",
            fontSize = 32.sp, // Smaller font for hundredths
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Slightly muted color
            modifier = Modifier
                .align(Alignment.Bottom) // Align to bottom of the main time
                .padding(start = 4.dp, bottom = 8.dp) // Adjust padding
        )
    }
}

@Composable
fun LapRow(lap: Lap, index: Int, formatTime: (Long) -> String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp), // Add horizontal padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "# ${index}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = formatTime(lap.lapTime), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = formatTime(lap.totalTime), fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StopwatchControls(
    stopwatchState: StopwatchState,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onLapClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Button: Lap or Reset
        Button(
            onClick = if (stopwatchState == StopwatchState.IDLE) onResetClick else onLapClick,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            // Enable Lap only when RUNNING, Enable Reset only when PAUSED or IDLE (but not 0 time)
            enabled = stopwatchState == StopwatchState.RUNNING || (stopwatchState != StopwatchState.IDLE) // Enable Lap when running, Reset otherwise unless truly idle
        ) {
            Icon(
                imageVector = if (stopwatchState == StopwatchState.RUNNING) Icons.Filled.Flag else Icons.Filled.Refresh,
                contentDescription = if (stopwatchState == StopwatchState.RUNNING) "Lap" else "Reset"
            )
        }

        // Center Button: Start/Pause
        Button(
            onClick = {
                when (stopwatchState) {
                    StopwatchState.IDLE, StopwatchState.PAUSED -> onStartClick()
                    StopwatchState.RUNNING -> onPauseClick()
                }
            },
            modifier = Modifier
                .size(80.dp) // Adjust size as needed
                .weight(1.5f), // Give it more weight to be larger
            shape = RoundedCornerShape(24.dp), // Change shape to RoundedCornerShape
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Use primary color
                contentColor = MaterialTheme.colorScheme.onPrimary // Use onPrimary for content
            ),
            contentPadding = PaddingValues(0.dp) // Remove default padding if needed
        ) {
            Icon(
                imageVector = when (stopwatchState) {
                    StopwatchState.IDLE, StopwatchState.PAUSED -> Icons.Filled.PlayArrow
                    StopwatchState.RUNNING -> Icons.Filled.Pause
                },
                contentDescription = when (stopwatchState) {
                    StopwatchState.IDLE, StopwatchState.PAUSED -> "Start"
                    StopwatchState.RUNNING -> "Pause"
                },
                modifier = Modifier.size(40.dp) // Adjust icon size
            )
        }

        // Right Button: Placeholder or Future Functionality (Kept for symmetry)
        // Using a disabled button or Box for spacing
        Box(modifier = Modifier.size(80.dp)) // Placeholder to maintain spacing
        /* Example if adding a third button:
        Button(
            onClick = { /* Action for third button */ },
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            enabled = false, // Or based on state
            colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.Transparent)
        ) {
            // Icon or Text
        }
        */
    }
}

// --- Preview --- Needs a ViewModel instance or mock data
@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    MaterialTheme {
        // Preview with a default ViewModel instance
        StopwatchScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StopwatchScreenPreviewDark() {
    MaterialTheme {
        StopwatchScreen()
    }
}