package com.gkprojct.clock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas // Add import for Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add // Add import for Add
import androidx.compose.material.icons.filled.Close // Add import for Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop // Add import for Stop
import androidx.compose.material.icons.filled.MoreVert // Add import for MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset // Add import for Offset
import androidx.compose.ui.geometry.Size // Add import for Size
import androidx.compose.ui.graphics.StrokeCap // Add import for StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke // Add import for Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gkprojct.clock.ui.theme.ClockTheme
import com.gkprojct.clock.vm.TimerPreset
import com.gkprojct.clock.vm.TimerState
import com.gkprojct.clock.vm.TimerViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class) // Add OptIn for Scaffold
@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val timeInput by timerViewModel.timeInput.collectAsState()
    val timerState by timerViewModel.timerState.collectAsState()
    val remainingTime by timerViewModel.remainingTime.collectAsState()
    val initialDuration by timerViewModel.initialDuration.collectAsState() // Get initial duration
    val presets = timerViewModel.presets
    val showStartButton = timeInput.isNotEmpty() && timeInput != "000000" && timerState == TimerState.STOPPED

    // Use Scaffold to easily place the FloatingActionButton
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("计时器") },
                actions = {
                    IconButton(onClick = { /* TODO: Implement settings menu */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "更多选项")
                    }
                }
            )
        },
        floatingActionButton = {
            // Show FAB only when a timer is running or paused
            if (timerState != TimerState.STOPPED) {
                FloatingActionButton(
                    onClick = { /* TODO: Implement adding a new timer */ },
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "添加计时器")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center // Center the FAB
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold
                .padding(horizontal = 16.dp) // Keep horizontal padding
                .padding(top = 8.dp), // Adjust padding as needed
            horizontalAlignment = Alignment.CenterHorizontally
            // Remove SpaceBetween, arrangement will be handled by Crossfade content
            // verticalArrangement = Arrangement.SpaceBetween 
            // Add top padding to account for the TopAppBar
        ) {
            // Crossfade between Input UI and Running/Paused UI
            Crossfade(targetState = timerState == TimerState.STOPPED, label = "TimerStateCrossfade") {
                isStopped ->
                if (isStopped) {
                    // --- Input Mode UI --- (Relatively unchanged, adjust spacing)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxHeight() // Allow Column to take height
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        TimerInputDisplay(timeInput = timeInput)
                        Spacer(modifier = Modifier.height(16.dp))
                        PresetTimersRow(
                            presets = presets,
                            onPresetClick = { durationMillis ->
                                timerViewModel.setInputTime(durationMillis)
                                // If a timer was running/paused, reset it before setting new input
                                if (timerState != TimerState.STOPPED) {
                                    timerViewModel.resetTimer()
                                }
                                timerViewModel.setInputTime(durationMillis) // Set input after potential reset
                            },
                            enabled = timerState == TimerState.STOPPED
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NumberPad { digit ->
                            when (digit) {
                                "backspace" -> timerViewModel.deleteDigit()
                                else -> timerViewModel.appendDigit(digit)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f)) // Push button to bottom
                        // Start Button Area
                        if (showStartButton) {
                            Button(
                                onClick = { timerViewModel.startTimerFromInput() },
                                modifier = Modifier
                                    .size(72.dp)
                                    .padding(bottom = 32.dp), // Adjust bottom padding
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "开始",
                                    modifier = Modifier.size(ButtonDefaults.IconSize * 1.5f)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(72.dp + 32.dp)) // Placeholder space
                        }
                    }
                } else {
                    // --- Running/Paused/Overtime Mode UI --- (Updated Implementation)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Bar: Timer Title and Cancel Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatDurationToTimerTitle(initialDuration), // Display formatted initial duration
                                style = MaterialTheme.typography.titleMedium
                            )
                            IconButton(onClick = { timerViewModel.resetTimer() }) {
                                Icon(Icons.Filled.Close, contentDescription = "取消计时器")
                            }
                        }

                        Spacer(modifier = Modifier.weight(0.5f)) // Add space above circle

                        // Center Area: Circular Progress and Time
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.75f) // Control the size of the circle area
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularTimer( // Extracted Composable for circle
                                remainingTime = remainingTime,
                                initialDuration = initialDuration
                            )
                            // Updated TimerDisplay to handle negative time
                            TimerDisplay(timeMillis = remainingTime, isOvertime = remainingTime < 0)
                        }

                        Spacer(modifier = Modifier.weight(0.5f)) // Add space below circle

                        // Bottom Controls: +1:00 and Stop Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 80.dp), // Add padding to avoid FAB overlap
                            horizontalArrangement = Arrangement.SpaceEvenly, // Use SpaceEvenly for better spacing
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // +1:00 Button (Rounded)
                            Button(
                                onClick = { timerViewModel.addMinute() },
                                shape = RoundedCornerShape(24.dp), // More rounded rectangle
                                modifier = Modifier.height(60.dp).width(120.dp), // Adjust size
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                Text("+1:00", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 16.sp)
                            }

                            // Stop Button (Square-ish)
                            Button(
                                onClick = { timerViewModel.resetTimer() }, // Stop button resets the timer
                                shape = RoundedCornerShape(16.dp), // Square-ish shape
                                modifier = Modifier.size(80.dp), // Keep size
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stop, // Use Stop icon
                                    contentDescription = "停止",
                                    modifier = Modifier.size(ButtonDefaults.IconSize * 1.5f),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to format duration (e.g., 10000ms -> "10s Timer")
fun formatDurationToTimerTitle(durationMillis: Long): String {
    if (durationMillis <= 0) return "计时器" // Default title
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format("%dh %dm %ds 计时器", hours, minutes, seconds)
        minutes > 0 -> String.format("%dm %ds 计时器", minutes, seconds)
        else -> String.format("%ds 计时器", seconds)
    }
}

@Composable
fun CircularTimer(remainingTime: Long, initialDuration: Long) {
    val progress = if (initialDuration > 0) remainingTime.toFloat() / initialDuration.toFloat() else 0f
    val circleColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val strokeWidth = 12.dp

    Canvas(modifier = Modifier.fillMaxSize()) {
        val diameter = size.minDimension
        val radius = diameter / 2f
        val topLeft = Offset(0f, 0f)
        val strokePx = strokeWidth.toPx()

        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius - strokePx / 2f,
            center = center,
            style = Stroke(width = strokePx)
        )

        // Foreground arc
        drawArc(
            color = circleColor,
            startAngle = -90f, // Start from the top
            sweepAngle = progress * 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )
    }
}

// Timer display when running/paused (Style adjustments)
@Composable
fun TimerDisplay(timeMillis: Long, isOvertime: Boolean) {
    val formattedTime = formatTimeHMS(timeMillis)
    Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 56.sp, // Slightly larger font inside circle
            fontWeight = FontWeight.Normal // Normal weight might look better
        ),
        color = MaterialTheme.colorScheme.onSurface // Ensure text color is appropriate
    )
}

@Composable
fun TimerInputDisplay(timeInput: String) {
    val hours = timeInput.substring(0, 2)
    val minutes = timeInput.substring(2, 4)
    val seconds = timeInput.substring(4, 6)

    Row(verticalAlignment = Alignment.CenterVertically) {
        TimePart(time = hours, unit = "h")
        Spacer(modifier = Modifier.width(8.dp))
        TimePart(time = minutes, unit = "m")
        Spacer(modifier = Modifier.width(8.dp))
        TimePart(time = seconds, unit = "s")
    }
}

@Composable
fun TimePart(time: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = time,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp, // Smaller font size
                fontWeight = FontWeight.Light // Lighter font weight
            )
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp) // Adjust padding
        )
    }
}

@Composable
fun NumberPad(onKeyPress: (String) -> Unit) {
    val buttons = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "backspace")
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly, // Use SpaceEvenly for better spacing
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    Button(
                        onClick = { onKeyPress(key) },
                        modifier = Modifier
                            .size(72.dp) // Make buttons square for circular shape
                            .padding(4.dp), // Add padding between buttons
                        shape = CircleShape, // Make buttons circular
                        colors = ButtonDefaults.filledTonalButtonColors(), // Use tonal buttons
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        if (key == "backspace") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "删除")
                        } else {
                            Text(text = key, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetTimersRow(
    presets: List<TimerPreset>,
    onPresetClick: (Long) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presets.forEach { preset ->
            Button(
                onClick = { onPresetClick(preset.durationMillis) },
                enabled = enabled,
                shape = RoundedCornerShape(16.dp), // Rounded corners
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text(preset.name)
            }
        }
    }
}

// Format time to HH:MM:SS or MM:SS or SS
fun formatTimeHMS(timeMillis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds)
        else -> String.format("%d", seconds)
    }
}

// Format time from HHMMSS input string for display
fun formatTimeHmsInput(input: String): String {
    if (input.length != 6) return "0:00"
    val hours = input.substring(0, 2).toIntOrNull() ?: 0
    val minutes = input.substring(2, 4).toIntOrNull() ?: 0
    val seconds = input.substring(4, 6).toIntOrNull() ?: 0

    return when {
        hours > 0 -> String.format("%d:%02d:%02d", hours, minutes, seconds)
        minutes > 0 -> String.format("%d:%02d", minutes, seconds)
        else -> String.format("%d", seconds)
    }
}

// --- Previews --- (Update if necessary)
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Timer Screen - Stopped")
@Composable
fun TimerScreenPreviewStopped() {
    ClockTheme {
        // Provide a mock ViewModel or state for preview
        val mockViewModel = TimerViewModel()
        TimerScreen(timerViewModel = mockViewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Timer Screen - Running")
@Composable
fun TimerScreenPreviewRunning() {
    ClockTheme {
        val mockViewModel = TimerViewModel()
        // Simulate running state for preview
        LaunchedEffect(Unit) {
            mockViewModel.setInputTime(10000) // 10 seconds
            mockViewModel.startTimerFromInput()
            // Let it run for a bit
            kotlinx.coroutines.delay(2000)
        }
        TimerScreen(timerViewModel = mockViewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Timer Screen - Paused")
@Composable
fun TimerScreenPreviewPaused() {
    ClockTheme {
        val mockViewModel = TimerViewModel()
        // Simulate paused state for preview
        LaunchedEffect(Unit) {
            mockViewModel.setInputTime(30000) // 30 seconds
            mockViewModel.startTimerFromInput()
            kotlinx.coroutines.delay(5000)
            mockViewModel.pauseTimer()
        }
        TimerScreen(timerViewModel = mockViewModel)
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
fun DefaultPreview() {
    ClockTheme {
        TimerScreen()
    }
}