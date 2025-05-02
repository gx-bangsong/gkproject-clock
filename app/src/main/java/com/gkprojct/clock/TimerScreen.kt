package com.gkprojct.clock

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun TimerScreen(timerViewModel: TimerViewModel = viewModel()) {
    val timeInput by timerViewModel.timeInput.collectAsState()
    val timerState by timerViewModel.timerState.collectAsState()
    val remainingTime by timerViewModel.remainingTime.collectAsState()
    val presets = timerViewModel.presets
    val showStartButton = timeInput.isNotEmpty() && timeInput != "000000" && timerState == TimerState.STOPPED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Presets
        PresetTimersRow(
            presets = presets,
            onPresetClick = { durationMillis ->
                timerViewModel.setInputTime(durationMillis)
                // Reset state to stopped if a preset is clicked while running/paused
                if (timerState != TimerState.STOPPED) {
                    timerViewModel.resetTimer() // Reset also clears input
                    timerViewModel.setInputTime(durationMillis) // Set input again after reset
                }
            },
            enabled = timerState == TimerState.STOPPED // Disable presets when timer is active
        )

        // Middle section: Timer Display or Input Pad
        Crossfade(targetState = timerState == TimerState.STOPPED) {
            isStopped ->
            if (isStopped) {
                // Input Mode
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(32.dp))
                    TimerInputDisplay(timeInput = timeInput)
                    Spacer(modifier = Modifier.height(32.dp))
                    NumberPad {
                        when (it) {
                            "backspace" -> timerViewModel.deleteDigit()
                            else -> timerViewModel.appendDigit(it)
                        }
                    }
                }
            } else {
                // Running/Paused Mode
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f) // Take available space
                ) {
                    TimerDisplay(timeMillis = remainingTime)
                    Spacer(modifier = Modifier.height(48.dp))
                    TimerControls(
                        timerState = timerState,
                        onStartPauseResume = {
                            when (timerState) {
                                TimerState.RUNNING -> timerViewModel.pauseTimer()
                                TimerState.PAUSED -> timerViewModel.resumeTimer()
                                else -> {} // Should not happen here
                            }
                        },
                        onReset = { timerViewModel.resetTimer() }
                    )
                }
            }
        }

        // Bottom section: Start Button or Spacer
        if (timerState == TimerState.STOPPED) {
            if (showStartButton) {
                Button(
                    onClick = { timerViewModel.startTimerFromInput() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text("开始")
                }
            } else {
                // Keep space for the button even when hidden
                Spacer(modifier = Modifier.height(ButtonDefaults.MinHeight + 32.dp))
            }
        } else {
            // Add a spacer at the bottom when controls are shown
            Spacer(modifier = Modifier.height(ButtonDefaults.MinHeight + 32.dp))
        }
    }
}

// Timer display when running/paused
@Composable
fun TimerDisplay(timeMillis: Long) {
    val formattedTime = formatTimeHMS(timeMillis)
    Text(
        text = formattedTime,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 48.sp, // Consistent smaller font size
            fontWeight = FontWeight.Light // Consistent lighter font weight
        )
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

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        buttons.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    Button(
                        onClick = { onKeyPress(key) },
                        modifier = Modifier.size(72.dp), // Adjust size as needed
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp), // Remove default padding
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Match dark theme
                    ) {
                        if (key == "backspace") {
                            Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete")
                        } else {
                            Text(text = key, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}


// --- Updated PresetTimersRow --- 
@Composable
fun PresetTimersRow(
    presets: List<TimerPreset>,
    onPresetClick: (Long) -> Unit,
    enabled: Boolean // Add enabled state
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        presets.forEach { preset ->
            Button(
                onClick = { onPresetClick(preset.durationMillis) },
                enabled = enabled, // Control enabled state
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) // Dim when disabled
                )
            ) {
                Text(preset.name)
            }
        }
    }
}
// --- End of PresetTimersRow ---

// --- Updated TimerControls --- 
@Composable
fun TimerControls(
    timerState: TimerState,
    onStartPauseResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reset Button (often on the left)
        Button(onClick = onReset) {
            Text("重置") // Changed to Chinese
        }
        // Pause/Resume Button (often on the right)
        Button(onClick = onStartPauseResume) {
            Text(if (timerState == TimerState.RUNNING) "暂停" else "继续") // Changed to Chinese
        }
    }
}
// --- End of TimerControls ---

// --- formatTimeHMS (Used by TimerDisplay) ---
fun formatTimeHMS(timeMillis: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    // Format as HH:MM:SS for the running display
    return String.format(java.util.Locale.US, "%02d:%02d:%02d",
        (hours % 100).coerceAtLeast(0), // Show hours up to 99, handle potential negative
        minutes.coerceAtLeast(0),
        seconds.coerceAtLeast(0)
    )
}
// --- End of formatTimeHMS ---


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
fun DefaultPreview() {
    ClockTheme {
        TimerScreen()
    }
}