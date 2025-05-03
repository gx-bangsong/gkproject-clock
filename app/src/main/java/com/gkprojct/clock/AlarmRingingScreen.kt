package com.gkprojct.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close // Dismiss icon
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily // Placeholder for Google Sans
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Placeholder for Google Sans font - replace with actual font resource if available
val googleSansFontFamily = FontFamily.SansSerif // Use SansSerif as a fallback

@Composable
fun AlarmRingingScreen(
    alarmTime: String, // e.g., "09:30"
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val sliderWidth = 300.dp // Width of the slider track
    val thumbRadius = 30.dp
    val trackHeight = 60.dp // Height of the rounded rectangle track
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val thumbColor = MaterialTheme.colorScheme.primary

    var offsetX by remember { mutableStateOf(0f) }
    val maxOffsetX = with(density) { (sliderWidth / 2 - thumbRadius).toPx() }
    val minOffsetX = -maxOffsetX

    var currentIcon by remember { mutableStateOf(Icons.Filled.Alarm) }

    val draggableState = rememberDraggableState {
        val newOffsetX = (offsetX + it).coerceIn(minOffsetX, maxOffsetX)
        offsetX = newOffsetX

        // Update icon based on drag position
        currentIcon = when {
            offsetX < -maxOffsetX * 0.8 -> Icons.Filled.Snooze
            offsetX > maxOffsetX * 0.8 -> Icons.Filled.Close
            else -> Icons.Filled.Alarm
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround // Distribute space
    ) {
        // Alarm Time Display
        Text(
            text = alarmTime,
            style = TextStyle(
                fontFamily = googleSansFontFamily, // Attempt to use Google Sans
                fontWeight = FontWeight.Normal, // Adjust weight as needed
                fontSize = 101.sp, // Large font size
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(top = 64.dp) // Add some top padding
        )

        // Custom Slider
        Box(
            modifier = Modifier
                .height(trackHeight)
                .width(sliderWidth)
                .padding(bottom = 32.dp), // Padding from bottom
            contentAlignment = Alignment.Center
        ) {
            // Slider Track (Rounded Rectangle)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = trackColor,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                )
            }

            // Snooze and Dismiss Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), // Padding inside the track
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Snooze", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Dismiss", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Draggable Thumb
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .size(thumbRadius * 2)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = draggableState,
                        onDragStopped = {
                            when {
                                offsetX < -maxOffsetX * 0.8 -> onSnooze()
                                offsetX > maxOffsetX * 0.8 -> onDismiss()
                            }
                            // Reset position and icon after drag stops
                            offsetX = 0f
                            currentIcon = Icons.Filled.Alarm
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = thumbColor,
                        radius = size.minDimension / 2
                    )
                }
                Icon(
                    imageVector = currentIcon,
                    contentDescription = when (currentIcon) {
                        Icons.Filled.Snooze -> "Snooze"
                        Icons.Filled.Close -> "Dismiss"
                        else -> "Alarm"
                    },
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(thumbRadius) // Icon size within the thumb
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F) // Dark background for preview
@Composable
fun AlarmRingingScreenPreview() {
    MaterialTheme {
        AlarmRingingScreen(
            alarmTime = "09:30",
            onSnooze = { println("Snooze triggered") },
            onDismiss = { println("Dismiss triggered") }
        )
    }
}