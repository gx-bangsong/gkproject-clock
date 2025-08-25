package com.gkprojct.clock

// ... 其他导入 ...
import android.content.res.Configuration
import androidx.compose.foundation.layout.Column // <-- Import Column
import androidx.compose.foundation.layout.fillMaxSize // <-- Import fillMaxSize
import androidx.compose.foundation.layout.padding // <-- Import padding modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier // <-- Modifier is already imported
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp // <-- dp is already imported

// 确保这些导入存在


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onSelectCalendarClick: () -> Unit, // 导航到日历选择界面
    onManageRulesClick: () -> Unit, // 导航到规则管理界面
    onCalendarsSelectionDone: (List<Long>) -> Unit, // 日历选择界面完成时调用保存回调
    // --- 新增参数：接收初始选中的日历 ID 集合 ---
    initialSelectedCalendarIds: Set<Long> = emptySet()
    // ------------------------------------------
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column( // <-- Column should now be recognized
            modifier = Modifier
                .padding(paddingValues) // <-- padding should now be recognized
                .fillMaxSize() // <-- fillMaxSize should now be recognized
                .padding(16.dp) // Inner padding
        ) {
            Text( // <-- Text should be recognized (from material3 or ui)
                text = "规则闹钟",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp) // <-- padding should now be recognized
            )
            HorizontalDivider()

            // Option to select calendars to read
            AlarmOptionItem( // <-- AlarmOptionItem should be recognized (from your shared file)
                icon = Icons.Default.CalendarMonth,
                text = "选择日历",
                description = "选择用于规则判断的系统日历",
                hasMoreAction = true,
                onClick = onSelectCalendarClick
            )
            HorizontalDivider()

            // Option to manage defined rules
            AlarmOptionItem( // <-- AlarmOptionItem should be recognized
                icon = Icons.Default.Rule,
                text = "管理规则",
                description = "查看和编辑规则",
                hasMoreAction = true,
                onClick = onManageRulesClick
            )
            HorizontalDivider()

            // TODO: Add other settings options here
        }
    }
}

// --- Preview ---
// 更新 Preview 函数，传递新的参数
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreviewDark() {
    MaterialTheme {
        SettingsScreen(
            onBackClick = {},
            onSelectCalendarClick = {},
            onManageRulesClick = {},
            onCalendarsSelectionDone = { selectedIds -> /* ... */ },
            initialSelectedCalendarIds = setOf(1L, 5L)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreviewLight() {
    MaterialTheme {
        SettingsScreen(
            onBackClick = {},
            onSelectCalendarClick = {},
            onManageRulesClick = {},
            onCalendarsSelectionDone = { selectedIds -> /* ... */ },
            initialSelectedCalendarIds = setOf(1L, 5L)
        )
    }
}
