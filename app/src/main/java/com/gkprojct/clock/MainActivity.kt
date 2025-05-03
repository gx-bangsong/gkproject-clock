package com.gkprojct.clock // <-- 确保包路径正确


// Import shared definitions (ensure you import what's needed)
// Import screen composables (IDE usually handles this, uncomment if needed)
// Import RuleCriteria (Needed for state)
// Import RuleAlarmSelectionScreen and RuleCriteriaDefinitionScreen
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gkprojct.clock.ui.theme.ClockTheme
import com.gkprojct.clock.vm.AppDatabase
import java.time.DayOfWeek
import java.util.UUID
import java.util.Calendar // Add Calendar import
import java.time.DayOfWeek // Ensure DayOfWeek is imported
import java.util.UUID // Ensure UUID is imported


// 定义一个枚举类来表示 Settings 内部的屏幕类型 (添加闹钟选择和条件配置)
enum class SettingsScreenType {
    Main, CalendarSelection, RuleManagement, AddEditRule, RuleCalendarSelection, RuleAlarmSelection, RuleCriteriaDefinition // <-- **新增 RuleAlarmSelection 和 RuleCriteriaDefinition**
}
@Composable
fun AppContent() {
    val context = LocalContext.current

    // 获取数据库实例和 RuleDao (Keep this)
    val database = remember { AppDatabase.getDatabase(context) }
    val ruleDao = remember { database.ruleDao() }

    // 创建 RuleViewModel 实例 (Keep this)
    val ruleViewModel: RuleViewModel = viewModel(
        factory = RuleViewModelFactory(ruleDao)
    )

    val prefs = remember {
        context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    var isBedtimeSetupComplete by remember {
        mutableStateOf(prefs.getBoolean("isBedtimeSetupComplete", false))
    }

    var currentScreenIndex by remember { mutableIntStateOf(0) }

    var bedtimeSetupStep by remember { mutableIntStateOf(0) }

    var showSettingsScreen by remember { mutableStateOf(false) }
    var currentSettingsScreen by remember { mutableStateOf(SettingsScreenType.Main) }

    // --- 新增状态：存储当前正在编辑的规则对象 --- (Keep this)
    var ruleToEdit by remember { mutableStateOf<Rule?>(null) }
    // ----------------------------------------

    // --- 新增状态：存储当前规则已选中的日历 ID 集合 (用于在导航到 CalendarSelectionScreen 时传递) --- (Keep this)
    var currentRuleSelectedCalendarIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // ---------------------------------------------------------------------------------------

    // --- 新增状态：存储当前规则已选择的应用闹钟 ID 集合 (用于在导航时传递) ---
    var currentRuleSelectedAlarmIds by remember { mutableStateOf<Set<UUID>>(emptySet()) }
    // -----------------------------------------------------------------------

    // --- 新增状态：存储当前规则条件 (用于在导航时传递) ---
    var currentRuleCriteria by remember { mutableStateOf<RuleCriteria>(RuleCriteria.AlwaysTrue) } // 初始化为默认条件
    // ---------------------------------------------------


    val sharedPreferences = remember {
        context.getSharedPreferences("CalendarPrefs", Context.MODE_PRIVATE)
    }

    // 函数：保存选中的日历 ID 列表 (Keep this)
    val saveSelectedCalendarIds: (List<Long>) -> Unit = { selectedIds ->
        val idSet = selectedIds.map { it.toString() }.toSet()
        sharedPreferences.edit().putStringSet("selectedCalendarIds", idSet).apply()
        // TODO: Add confirmation message (e.g., Toast)
        Log.d("CalendarPrefs", "Saved selected calendar IDs: $selectedIds")
    }

    // 函数：加载之前保存的日历 ID 列表 (Keep this)
    val loadSelectedCalendarIds: () -> Set<Long> = {
        sharedPreferences.getStringSet("selectedCalendarIds", emptySet())?.map { it.toLong() }?.toSet() ?: emptySet()
    }
    // --- 在需要显示 SettingsScreen 的时候加载选中的日历 ID --- (Keep this)
    val initiallySelectedCalendarIds = remember(showSettingsScreen) {
        if (showSettingsScreen) { // 只有在进入 Settings 区域时才加载
            loadSelectedCalendarIds()
        } else {
            emptySet() // 不在 Settings 区域时，无需加载或清空
        }
    }
    // -------------------------------------------------------
    // --- 监听 showSettingsScreen 状态变化并打印日志 --- (Keep this)
    LaunchedEffect(showSettingsScreen) {
        Log.d("NavigationDebug", "showSettingsScreen changed to: $showSettingsScreen")
    }
    // --- 监听 ruleToEdit 变化并更新相关状态 ---
    LaunchedEffect(ruleToEdit) {
        // 当 ruleToEdit 变化时，更新 currentRuleSelectedCalendarIds, currentRuleSelectedAlarmIds, currentRuleCriteria
        // 这确保在进入编辑模式时，UI 状态与要编辑的规则同步
        ruleToEdit?.let {
            currentRuleSelectedCalendarIds = it.calendarIds
            currentRuleSelectedAlarmIds = it.targetAlarmIds
            currentRuleCriteria = it.criteria
        } ?: run {
            // 如果 ruleToEdit 为 null (添加模式)，重置状态
            currentRuleSelectedCalendarIds = emptySet()
            currentRuleSelectedAlarmIds = emptySet()
            currentRuleCriteria = RuleCriteria.AlwaysTrue
        }
    }
    // -------------------------------------------


    // --- 创建示例 Alarm 数据列表 (Corrected to match Alarm data class) ---
    val sampleAlarms = remember {
        listOf(
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0) }, // Keep Calendar
                label = "起床闹钟", // String? is correct
                isEnabled = true, // Boolean is correct
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY) // Set<DayOfWeek> is correct
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 30) }, // Keep Calendar
                label = "午休闹钟", // String? is correct
                isEnabled = false, // Boolean is correct
                repeatingDays = emptySet() // Set<DayOfWeek> is correct
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 15); set(Calendar.MINUTE, 0) }, // Keep Calendar
                label = "会议提醒", // String? is correct
                isEnabled = true, // Boolean is correct
                repeatingDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY) // Set<DayOfWeek> is correct
            ),
            Alarm(
                id = UUID.randomUUID(),
                time = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 18); set(Calendar.MINUTE, 0) }, // Keep Calendar
                label = "健身闹钟", // String? is correct
                isEnabled = true, // Boolean is correct
                repeatingDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) // Set<DayOfWeek> is correct
            ),
        )
    }
    // ------------------------------------------


    Scaffold(
        bottomBar = {
            // Only show the bottom bar if Settings are NOT showing AND Bedtime setup is complete (Keep this)
            if (!showSettingsScreen && isBedtimeSetupComplete) {
                NavigationBar {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = currentScreenIndex == index,
                            onClick = {
                                currentScreenIndex = index
                                // Note: This logic is only relevant if the bottom bar is visible (i.e., setup complete)
                                if (index == 4) { // No need to check !isBedtimeSetupComplete here due to bottom bar visibility
                                    bedtimeSetupStep = 3 // Go directly to main BedtimeScreen if bottom bar is visible
                                } else {
                                    // For other tabs, ensure the step is not stuck on setup steps
                                    bedtimeSetupStep = 3 // Or a value indicating main screen
                                }

                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.name
                                )
                            },
                            label = { Text(item.name) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // --- **唯一的**屏幕显示控制逻辑 ---
            if (showSettingsScreen) {
                when (currentSettingsScreen) {
                    SettingsScreenType.Main -> SettingsScreen(
                        onBackClick = { showSettingsScreen = false },
                        onSelectCalendarClick = { currentSettingsScreen = SettingsScreenType.CalendarSelection },
                        onManageRulesClick = { currentSettingsScreen = SettingsScreenType.RuleManagement },
                        onCalendarsSelectionDone = saveSelectedCalendarIds,
                        initialSelectedCalendarIds = initiallySelectedCalendarIds
                    )
                    // CalendarSelection 的 Case (Keep this)
                    SettingsScreenType.CalendarSelection -> CalendarSelectionScreen(
                        onBackClick = {
                            if (currentSettingsScreen == SettingsScreenType.RuleCalendarSelection) {
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            } else {
                                currentSettingsScreen = SettingsScreenType.Main
                            }
                        },
                        onCalendarsSelected = { selectedIds ->
                            if (currentSettingsScreen == SettingsScreenType.RuleCalendarSelection) {
                                currentRuleSelectedCalendarIds = selectedIds.toSet()
                                Log.d("NavigationDebug", "MainActivity: Rule Calendar Selection Done. Selected IDs: $selectedIds")
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            } else {
                                saveSelectedCalendarIds(selectedIds)
                                currentSettingsScreen = SettingsScreenType.Main
                            }
                        },
                        // 根据当前是主设置还是规则设置来传递不同的初始选中 ID
                        initialSelectedCalendarIds = if (currentSettingsScreen == SettingsScreenType.RuleCalendarSelection) {
                            currentRuleSelectedCalendarIds
                        } else {
                            initiallySelectedCalendarIds
                        }
                    )
                    // RuleManagement 的 Case (Keep this)
                    SettingsScreenType.RuleManagement -> RuleManagementScreen(
                        onBackClick = { currentSettingsScreen = SettingsScreenType.Main },
                        onAddRuleClick = {
                            ruleToEdit = null // 清空编辑状态，表示添加新规则
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        },
                        onRuleClick = { rule ->
                            ruleToEdit = rule // 设置要编辑的规则
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        },
                        ruleViewModel = ruleViewModel // <-- **传递 ViewModel**
                    )
                    // AddEditRule 的 Case (Corrected)
                    SettingsScreenType.AddEditRule -> AddEditRuleScreen(
                        initialRule = ruleToEdit, // Pass the rule to edit or null for add mode
                        onSaveRule = { savedRule ->
                            // ViewModel handles saving, just navigate back
                            currentSettingsScreen = SettingsScreenType.RuleManagement
                        },
                        onCancel = { currentSettingsScreen = SettingsScreenType.RuleManagement }, // Use onCancel instead of onBackClick
                        ruleViewModel = ruleViewModel, // Pass the ViewModel
                        onSelectCalendarsClick = { currentIds -> // Lambda receives current IDs from AddEditRuleScreen state
                            // Update state before navigating if needed, though AddEditRuleScreen manages its own state
                            // currentRuleSelectedCalendarIds = currentIds // Not needed here, AddEditRuleScreen handles its state
                            currentSettingsScreen = SettingsScreenType.RuleCalendarSelection
                        },
                        onSelectAlarmsClick = { currentIds -> // Lambda receives current IDs
                            // currentRuleSelectedAlarmIds = currentIds // Not needed here
                            currentSettingsScreen = SettingsScreenType.RuleAlarmSelection
                        },
                        onDefineCriteriaClick = { currentCriteria -> // Lambda receives current criteria
                            // currentRuleCriteria = currentCriteria // Not needed here
                            currentSettingsScreen = SettingsScreenType.RuleCriteriaDefinition
                        }
                        // Removed incorrect parameters: currentSelectedCalendarIds, currentSelectedAlarmIds, currentCriteria
                    )
                    // RuleCalendarSelection 的 Case (Keep this)
                    SettingsScreenType.RuleCalendarSelection -> CalendarSelectionScreen(
                        onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                        onCalendarsSelected = { selectedIds ->
                            currentRuleSelectedCalendarIds = selectedIds.toSet()
                            Log.d("NavigationDebug", "MainActivity: Rule Calendar Selection Done. Selected IDs: $selectedIds")
                            currentSettingsScreen = SettingsScreenType.AddEditRule // 返回编辑屏幕
                        },
                        initialSelectedCalendarIds = currentRuleSelectedCalendarIds // 传递当前规则已选中的日历 ID
                    )
                    // --- **新增 RuleAlarmSelection 的 Case** ---
                    SettingsScreenType.RuleAlarmSelection -> RuleAlarmSelectionScreen(
                        availableAlarms = sampleAlarms, // <-- **传递可用的闹钟列表**
                        initialSelectedAlarmIds = currentRuleSelectedAlarmIds, // 传递当前规则已选中的闹钟 ID
                        onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                        onAlarmsSelected = { selectedIds ->
                            currentRuleSelectedAlarmIds = selectedIds
                            Log.d("NavigationDebug", "MainActivity: Rule Alarm Selection Done. Selected IDs: $selectedIds")
                            currentSettingsScreen = SettingsScreenType.AddEditRule // 返回编辑屏幕
                        }
                    )
                    // --- **新增 RuleCriteriaDefinition 的 Case** ---
                    SettingsScreenType.RuleCriteriaDefinition -> RuleCriteriaDefinitionScreen(
                        initialCriteria = currentRuleCriteria, // 传递当前规则的条件
                        onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                        onCriteriaSelected = { selectedCriteria ->
                            currentRuleCriteria = selectedCriteria
                            Log.d("NavigationDebug", "MainActivity: Rule Criteria Definition Done. Selected Criteria: $selectedCriteria")
                            currentSettingsScreen = SettingsScreenType.AddEditRule // 返回编辑屏幕
                        }
                    )
                }
            } else if (!isBedtimeSetupComplete) {
                // Bedtime Setup Flow (Commented out as screens are missing)
                /*
                when (bedtimeSetupStep) {
                    0 -> BedtimeWelcomeScreen(onNext = { bedtimeSetupStep = 1 })
                    1 -> BedtimeScheduleScreen(onNext = { bedtimeSetupStep = 2 })
                    2 -> WakeUpTimeScreen(onDone = {
                        prefs.edit().putBoolean("isBedtimeSetupComplete", true).apply()
                        isBedtimeSetupComplete = true
                        bedtimeSetupStep = 3 // Mark setup as complete
                        currentScreenIndex = 4 // Switch to Bedtime tab after setup
                    })
                    else -> BedtimeScreen() // Should not happen during setup, but show main screen as fallback
                }
                */
                // Placeholder for missing Bedtime setup
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bedtime setup is not complete (Screens Missing)")
                }
            } else {
                // Main App Screens (Corrected)
                when (currentScreenIndex) {
                    0 -> AlarmScreen(
                        onSettingsClick = { showSettingsScreen = true }, // Pass onSettingsClick
                        ruleViewModel = ruleViewModel
                    )
                    1 -> ClockScreen() // Assuming ClockScreen doesn't need onSettingsClick
                    2 -> TimerScreen(timerViewModel = viewModel()) // Removed onSettingsClick, pass viewModel if needed
                    3 -> StopwatchScreen(stopwatchViewModel = viewModel()) // Removed onSettingsClick, pass viewModel if needed
                    4 -> BedtimeScreen() // Show main BedtimeScreen after setup
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.R)
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ClockTheme {
        AppContent()
    }
}