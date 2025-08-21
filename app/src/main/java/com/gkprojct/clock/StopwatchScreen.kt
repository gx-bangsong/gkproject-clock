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
import androidx.compose.material.icons.filled.MoreVert // Add import for MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api // Add import for ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton // Add import for IconButton
import androidx.compose.material3.Scaffold // Add import for Scaffold
import androidx.compose.material3.TopAppBar // Add import for TopAppBar
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
@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for Scaffold
@Composable
fun StopwatchScreen(stopwatchViewModel: StopwatchViewModel = viewModel()) {
    val elapsedTime by stopwatchViewModel.elapsedTime.collectAsState()
    val laps by stopwatchViewModel.laps.collectAsState()
    val stopwatchState by stopwatchViewModel.stopwatchState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("秒表") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement settings menu */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多选项")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues) // Apply padding from Scaffold
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
            .padding(vertical = 24.dp, horizontal = 16.dp), // Add horizontal padding
        horizontalArrangement = Arrangement.SpaceBetween, // Use SpaceBetween for edge alignment
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Button: Reset / Lap
        val isResetEnabled = stopwatchState == StopwatchState.PAUSED // Enable Reset only when paused
        val isLapEnabled = stopwatchState == StopwatchState.RUNNING // Enable Lap only when running
        Button(
            onClick = if (isLapEnabled) onLapClick else onResetClick,
            enabled = isLapEnabled || isResetEnabled, // Enabled if running (Lap) or paused (Reset)
            shape = CircleShape,
            modifier = Modifier.size(72.dp), // Consistent size with reference
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f), // Muted disabled state
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        ) {
            Icon(
                imageVector = if (isLapEnabled) Icons.Filled.Flag else Icons.Filled.Refresh,
                contentDescription = if (isLapEnabled) "计次" else "重置"
            )
        }

        // Center Button: Start / Pause (Conditional Shape & Size)
        val isRunning = stopwatchState == StopwatchState.RUNNING
        val isIdle = stopwatchState == StopwatchState.IDLE
        Button(
            onClick = if (isRunning) onPauseClick else onStartClick,
            // Shape: Rounded Rect when running/paused, Circle when idle
            shape = if (isIdle) CircleShape else RoundedCornerShape(24.dp),
            // Size: Larger Circle when idle, Rectangular when running/paused
            modifier = if (isIdle) Modifier.size(88.dp) else Modifier.size(width = 140.dp, height = 72.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                // Color: Primary when idle (Start), SurfaceVariant when running (Pause)
                containerColor = if (isIdle) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isIdle) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isRunning) "暂停" else "开始",
                modifier = Modifier.size(ButtonDefaults.IconSize * 1.5f) // Larger icon
            )
        }

        // Right Button: Placeholder (Invisible, maintains spacing)
        // Matches the size of the left button for symmetry
        Box(modifier = Modifier.size(72.dp)) // Placeholder to maintain spacing
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