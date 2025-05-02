package com.gkprojct.clock // Make sure this matches your package name

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek // Import DayOfWeek from java.time


// --- Shared Data Types and Constants ---

// Day of Week enum (Using java.time.DayOfWeek is recommended)
// If you manually defined a DayOfWeek enum elsewhere, delete those definitions.
// Rely on java.time.DayOfWeek.

// Mapping DayOfWeek enum to short UI names (e.g., "S", "M", "T"...)
val dayOfWeekToShortName = mapOf(
    DayOfWeek.SUNDAY to "S",
    DayOfWeek.MONDAY to "M",
    DayOfWeek.TUESDAY to "T",
    DayOfWeek.WEDNESDAY to "W",
    DayOfWeek.THURSDAY to "T", // Note: Both Tuesday and Thursday often map to "T"
    DayOfWeek.FRIDAY to "F",
    DayOfWeek.SATURDAY to "S" // Note: Both Saturday and Sunday often map to "S"
)

// Order for displaying day buttons (usually starts Sunday or Monday)
val shortDayNamesOrder: List<DayOfWeek> = listOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)

// Bottom Navigation Item Data Class
data class BottomNavItem(
    val name: String,
    val icon: ImageVector
    // If using Jetpack Navigation Component, add a route parameter:
    // val route: String
)

// List of Bottom Navigation Items
val bottomNavItems = listOf(
    BottomNavItem("Alarm", Icons.Default.Alarm),
    BottomNavItem("Clock", Icons.Default.Schedule), // Or Icons.Default.AccessTime, Icons.Default.Clock
    BottomNavItem("Timer", Icons.Default.HourglassEmpty), // Or Icons.Default.HourglassFull
    BottomNavItem("Stopwatch", Icons.Default.Timer), // Or Fastforward
    BottomNavItem("Bedtime", Icons.Default.KingBed) // Or NightsStay
)


// --- Shared Composable Functions ---

// Composable for a single Day Selection Button
@Composable
fun DayButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp) // Adjust size as needed
            .clip(CircleShape) // Clip to circle shape
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape // Apply background with circle shape
            )
            .clickable(onClick = onClick), // Make the box clickable
        contentAlignment = Alignment.Center // Center text inside the box
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Composable for an Alarm/Setting Option Item row (used in alarm details and setup screens)
@Composable
fun AlarmOptionItem(
    icon: ImageVector,
    text: String,
    description: String? = null, // Optional description text below the main text
    hasMoreAction: Boolean = false, // Whether to show a '>' icon on the right (for navigation)
    onClick: () -> Unit = {}, // Default empty click handler for the row
    content: @Composable (() -> Unit)? = null // Optional slot for custom content (like Switch/Checkbox)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // Make the row clickable
            .padding(vertical = 12.dp), // Padding for each option row
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$text icon", // Better accessibility
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) { // Text(s) take available space
            Text(text = text, fontSize = 16.sp)
            if (description != null) {
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        // Spacer(Modifier.width(16.dp)) // Add space if needed before the end content

        if (content != null) {
            content() // Display custom content (like Switch/Checkbox)
        } else if (hasMoreAction) {
            // Using ChevronRight for navigation indication
            Icon(
                imageVector = Icons.Default.ChevronRight, // Use ChevronRight for "more action" navigation
                contentDescription = "More options for $text", // More specific accessibility
                tint = MaterialTheme.colorScheme.onSurfaceVariant // Muted color for navigation indicator
            )
        }
        // If no content and no more action, nothing is displayed on the right
    }
}
@Composable
fun MoreOptionsSettingsAction(
    onSettingsClick:  () -> Unit// Callback to open settings
) {
    // State to control the visibility of the dropdown menu within this composable
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = true }) { // Click button to show menu
        Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }

    // Dropdown Menu positioned relative to the IconButton
    DropdownMenu(
        expanded = showMenu, // Menu visibility is controlled by showMenu state
        onDismissRequest = { showMenu = false } // Hide menu when dismissed
    ) {
        // "Settings" Menu Item
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onSettingsClick() // Call the provided settings callback
                showMenu = false // Hide the menu after clicking

            },
            leadingIcon = { // Optional: Add a settings icon
                Icon(Icons.Default.Settings, contentDescription = "Settings Icon")
            }
        )
        // TODO: Add other common menu items here if needed across screens
    }
}