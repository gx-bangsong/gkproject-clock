package com.gkprojct.clock

import android.Manifest // Import Manifest for permission name
import android.annotation.SuppressLint // Import for SuppressLint
import android.content.Context // Import Context
import android.database.Cursor // Import Cursor
import android.net.Uri // Import Uri
import android.provider.CalendarContract // Import CalendarContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement // Import Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues // Import PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.width // <-- Can remove if unused elsewhere
import androidx.compose.foundation.lazy.LazyColumn // Import LazyColumn for potentially long lists
import androidx.compose.foundation.lazy.items // Import items for LazyColumn
import androidx.compose.material.icons.Icons
// Import AutoMirrored filled icons for RTL support
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Import AutoMirrored ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
// No longer need mutableStateSetOf explicitly
// import androidx.compose.runtime.mutableStateSetOf // <-- REMOVED
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CalendarSelectionScreen(
    onBackClick: () -> Unit,
    onCalendarsSelected: (List<Long>) -> Unit,
    initialSelectedCalendarIds: Set<Long> = emptySet()
) {
    val context = LocalContext.current
    val calendarPermissionState = rememberPermissionState(Manifest.permission.READ_CALENDAR)

    var calendars by remember { mutableStateOf<List<CalendarInfo>>(emptyList()) }
    var isLoadingCalendars by remember { mutableStateOf(false) }

    // *** Use mutableStateOf<Set<Long>> instead of mutableStateSetOf ***
    var selectedCalendarIds by remember { mutableStateOf<Set<Long>>(emptySet()) }


    LaunchedEffect(calendarPermissionState.status, initialSelectedCalendarIds) { // Added initialSelectedCalendarIds as key
        if (calendarPermissionState.status.isGranted) {
            isLoadingCalendars = true
            val fetchedCalendars = loadCalendars(context)
            val fetchedCalendarIds = fetchedCalendars.map { it.id }.toSet()

            // Initialize selectedCalendarIds state by assigning the correct initial set
            selectedCalendarIds = initialSelectedCalendarIds.intersect(fetchedCalendarIds)

            calendars = fetchedCalendars
            isLoadingCalendars = false
        } else {
            calendars = emptyList()
            // Clear selections if permission revoked by assigning an empty set
            selectedCalendarIds = emptySet()
            isLoadingCalendars = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择日历") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Save button logic remains the same, but uses the current value of selectedCalendarIds
                    if (calendarPermissionState.status.isGranted && calendars.isNotEmpty() && !isLoadingCalendars) {
                        IconButton(onClick = {
                            onCalendarsSelected(selectedCalendarIds.toList()) // Pass the current set as a List
                            onBackClick() // Save after returning
                        }) {
                            Icon(Icons.Filled.Save, contentDescription = "保存选中的日历")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (calendarPermissionState.status.isGranted) {
                Text("以下是您可以选择的日历：")
                Spacer(Modifier.height(8.dp))

                if (isLoadingCalendars) {
                    CircularProgressIndicator() // Use CircularProgressIndicator from material3
                    Text("正在加载日历...")
                } else if (calendars.isEmpty()) {
                    Text("没有找到可用的日历。请确保您的设备已添加日历账户。")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(calendars, key = { it.id }) { calendarInfo ->
                            val currentId = calendarInfo.id
                            val isChecked = selectedCalendarIds.contains(currentId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // *** Update state by creating a new Set ***
                                        if (isChecked) {
                                            // Remove: Create new set without the ID
                                            selectedCalendarIds = selectedCalendarIds - currentId
                                        } else {
                                            // Add: Create new set with the ID included
                                            selectedCalendarIds = selectedCalendarIds + currentId
                                        }
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(calendarInfo.name)
                                    Text(
                                        text = calendarInfo.accountName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        // *** Update state by creating a new Set ***
                                        if (checked) {
                                            // Add: Create new set with the ID included
                                            selectedCalendarIds = selectedCalendarIds + currentId
                                        } else {
                                            // Remove: Create new set without the ID
                                            selectedCalendarIds = selectedCalendarIds - currentId
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

            } else { // Permission not granted
                Text("需要读取日历权限才能查看和选择日历。")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { calendarPermissionState.launchPermissionRequest() }) {
                    Text("授予日历权限")
                }
                Spacer(Modifier.height(8.dp))
                if (calendarPermissionState.status.shouldShowRationale) {
                    Text("我们需要日历权限来根据您的日程安排暂停闹钟。")
                }
            }
        }
    }
}

// --- Data Class to hold Calendar Information (Keep this) ---
data class CalendarInfo(
    val id: Long,
    val name: String,
    val accountName: String,
    val accountType: String
)

// --- Function to load calendars (Keep this) ---
@SuppressLint("MissingPermission")
suspend fun loadCalendars(context: Context): List<CalendarInfo> = withContext(Dispatchers.IO) {
    val calendarsList = mutableListOf<CalendarInfo>()
    // Projection remains the same
    val projection = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.ACCOUNT_NAME,
        CalendarContract.Calendars.ACCOUNT_TYPE
    )

    val uri: Uri = CalendarContract.Calendars.CONTENT_URI
    var cursor: Cursor? = null

    try {
        // Query logic remains the same
        cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            val idColumn = it.getColumnIndex(CalendarContract.Calendars._ID)
            val nameColumn = it.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountNameColumn = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
            val accountTypeColumn = it.getColumnIndex(CalendarContract.Calendars.ACCOUNT_TYPE)

            while (it.moveToNext()) {
                val id = if (idColumn != -1) it.getLong(idColumn) else -1L
                val name = if (nameColumn != -1) it.getString(nameColumn) else "Unnamed Calendar"
                val accountName = if (accountNameColumn != -1) it.getString(accountNameColumn) else "Unknown Account"
                val accountType = if (accountTypeColumn != -1) it.getString(accountTypeColumn) else "Unknown Type"

                if (id != -1L) {
                    calendarsList.add(CalendarInfo(id, name, accountName, accountType))
                }
            }
        }
    } catch (e: Exception) {
        // Basic error handling, consider logging more robustly
        e.printStackTrace()
    } finally {
        cursor?.close() // Ensure cursor is closed
    }
    calendarsList
}


// --- Preview (Keep this) ---
@Preview(showBackground = true)
@Composable
fun CalendarSelectionScreenPreview() {
    MaterialTheme { // Use MaterialTheme for preview consistency
        CalendarSelectionScreen(
            onBackClick = {},
            onCalendarsSelected = { selectedIds -> println("Preview Selected IDs: $selectedIds") }, // Added print for preview
            initialSelectedCalendarIds = setOf(1L, 3L) // Example initial selection
        )
    }
}