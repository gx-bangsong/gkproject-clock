package com.gkprojct.clock // <-- 确保包路径正确


// Import shared definitions (ensure you import what's needed)
// 确保这些导入存在
// Import screen composable (IDE usually handles this, uncomment if needed)
// 如果这些文件在同一个包下且被直接使用，请取消注释


// --- 导入 Room 相关类 (从 com.gkprojct.clock.vm 包导入) ---
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gkprojct.clock.ui.theme.ClockTheme
import com.gkprojct.clock.vm.AppDatabase
import java.util.UUID

// ------------------------------------------------------


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClockTheme {
                AppContent()
            }
        }
    }
}

// 定义一个枚举类来表示 Settings 内部的屏幕类型 (Keep this)
enum class SettingsScreenType {
    Main, CalendarSelection, RuleManagement, AddEditRule, RuleCalendarSelection, RuleAlarmSelection, RuleCriteriaDefinition
}

@Composable
fun AppContent() {
    val context = LocalContext.current

    // 获取数据库实例和 RuleDao (从 vm 包导入)
    val database = remember { AppDatabase.getDatabase(context) }
    val ruleDao = remember { database.ruleDao() }

    // 创建 RuleViewModel 实例 (从 vm 包导入)
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

    // --- 状态：存储当前正在编辑的规则对象 --- (Keep this)
    var ruleToEdit by remember { mutableStateOf<Rule?>(null) }
    // ----------------------------------------

    // --- 状态：存储当前规则已选中的日历 ID 集合 (用于在导航时传递) --- (Keep this)
    // 确保 emptySet() 在这里能正确推断类型为 Set<Long>
    var currentRuleSelectedCalendarIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    // ---------------------------------------------------------------------------------------

    // --- 状态：存储当前规则已选择的应用闹钟 ID 集合 (用于在导航时传递) --- (Keep this)
    // 确保 emptySet() 在这里能正确推断类型为 Set<UUID>
    var currentRuleSelectedAlarmIds by remember { mutableStateOf<Set<UUID>>(emptySet()) }
    // -----------------------------------------------------------------------

    // --- 状态：存储当前规则条件 (用于在导航时传递) --- (Keep this)
    var currentRuleCriteria by remember { mutableStateOf<RuleCriteria>(RuleCriteria.AlwaysTrue) } // 初始化为默认条件
    // ---------------------------------------------------


    val sharedPreferences = remember {
        context.getSharedPreferences("CalendarPrefs", Context.MODE_PRIVATE)
    }

    // 函数：保存选中的日历 ID 列表 (Keep this)
    val saveSelectedCalendarIds: (List<Long>) -> Unit = { selectedIds ->
        val idSet = selectedIds.map { it.toString() }.toSet()
        sharedPreferences.edit { putStringSet("selectedCalendarIds", idSet) }
        // TODO: Add confirmation message (e.g., Toast)
        Log.d("CalendarPrefs", "Saved selected calendar IDs: $selectedIds")
    }

    // 函数：加载之前保存的日历 ID 列表 (Keep this)
    val loadSelectedCalendarIds: () -> Set<Long> = {
        // 确保 emptySet() 在这里能正确推断类型为 Set<Long>
        sharedPreferences.getStringSet("selectedCalendarIds", emptySet())?.map { it.toLong() }?.toSet() ?: emptySet()
    }
    // --- 在需要显示 SettingsScreen 的时候加载选中的日历 ID --- (Keep this)
    val initiallySelectedCalendarIds = remember(showSettingsScreen) {
        if (showSettingsScreen) { // 只有在进入 Settings 区域时才加载
            loadSelectedCalendarIds()
        } else {
            // 确保 emptySet() 在这里能正确推断类型为 Set<Long>
            emptySet()
        }
    }
    // -------------------------------------------------------
    // --- 监听 showSettingsScreen 状态变化并打印日志 --- (Keep this)
    LaunchedEffect(showSettingsScreen) {
        Log.d("NavigationDebug", "showSettingsScreen changed to: $showSettingsScreen")
    }
    LaunchedEffect(ruleToEdit) {
        // 当 ruleToEdit 变化时，更新 currentRuleSelectedCalendarIds, currentRuleSelectedAlarmIds, currentRuleCriteria
        // 这确保在进入编辑模式时，UI 状态与要编辑的规则同步
        ruleToEdit?.let {
            currentRuleSelectedCalendarIds = it.calendarIds
            currentRuleSelectedAlarmIds = it.targetAlarmIds
            currentRuleCriteria = it.criteria
        } ?: run {
            // 如果 ruleToEdit 为 null (添加模式)，重置状态
            currentRuleSelectedCalendarIds = emptySet() // 确保 emptySet() 在这里能正确推断类型为 Set<Long>
            currentRuleSelectedAlarmIds = emptySet() // 确保 emptySet() 在这里能正确推断类型为 Set<UUID>
            currentRuleCriteria = RuleCriteria.AlwaysTrue
        }
    }


    // --- **移除硬编码的 sampleAlarms 列表** ---
    // val sampleAlarms = remember { ... } // REMOVE THIS BLOCK
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
                                bedtimeSetupStep = if (index == 4) { // No need to check !isBedtimeSetupComplete here due to bottom bar visibility
                                    3 // Go directly to main BedtimeScreen if bottom bar is visible
                                } else {
                                    // For other tabs, ensure the step is not stuck on setup steps
                                    3 // Or a value indicating main screen
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
                        onBackClick = { showSettingsScreen = false }, // 返回主界面
                        onSelectCalendarClick = { currentSettingsScreen = SettingsScreenType.CalendarSelection }, // 导航到日历选择
                        onManageRulesClick = { currentSettingsScreen = SettingsScreenType.RuleManagement }, // 导航到规则管理
                        onCalendarsSelectionDone = saveSelectedCalendarIds, // <-- 将保存函数传递下去
                        initialSelectedCalendarIds = initiallySelectedCalendarIds // <-- **将加载的 ID 传递给 SettingsScreen**

                    )
                    // CalendarSelection 的 Case (Keep this)
                    SettingsScreenType.CalendarSelection -> CalendarSelectionScreen(
                        onBackClick = {
                            currentSettingsScreen = if (currentSettingsScreen == SettingsScreenType.RuleCalendarSelection) {
                                SettingsScreenType.AddEditRule
                            } else {
                                SettingsScreenType.Main
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
                        initialSelectedCalendarIds = if (currentSettingsScreen == SettingsScreenType.RuleCalendarSelection) {
                            currentRuleSelectedCalendarIds
                        } else {
                            initiallySelectedCalendarIds
                        }
                    )
                    SettingsScreenType.RuleManagement -> RuleManagementScreen(
                        onBackClick = { currentSettingsScreen = SettingsScreenType.Main }, // 返回主设置界面
                        onAddRuleClick = {
                            // 添加规则：设置 ruleToEdit 为 null，导航到 AddEditRuleScreen
                            ruleToEdit = null
                            // 在 LaunchedEffect 中处理状态重置
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                            Log.d("NavigationDebug", "MainActivity: Add Rule Clicked")
                        },
                        onRuleClick = { rule ->
                            // 编辑规则：设置 ruleToEdit 为点击的规则，导航到 AddEditRuleScreen
                            ruleToEdit = rule // 保存要编辑的规则
                            // 在 LaunchedEffect 中处理状态加载
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                            Log.d("NavigationDebug", "MainActivity: Rule Clicked: ${rule.name}, ID: ${rule.id}")
                        },
                        ruleViewModel = ruleViewModel // 传递 ViewModel
                    )
                    // AddEditRule 的 Case (Keep this)
                    SettingsScreenType.AddEditRule -> {
                        AddEditRuleScreen(
                            initialRule = ruleToEdit, // 传递要编辑的规则 (添加模式时为 null)
                            onSaveRule = { savedRule ->
                                // 在保存规则前，将最新的选择和条件更新到规则对象中
                                val ruleWithUpdatedConfig = savedRule.copy(
                                    calendarIds = currentRuleSelectedCalendarIds,
                                    targetAlarmIds = currentRuleSelectedAlarmIds,
                                    criteria = currentRuleCriteria
                                )
                                ruleViewModel.saveRule(ruleWithUpdatedConfig) // 调用 ViewModel 保存规则
                                Log.d("NavigationDebug", "MainActivity: Rule Saved: ${ruleWithUpdatedConfig.name}, ID: ${ruleWithUpdatedConfig.id}, Calendars: ${ruleWithUpdatedConfig.calendarIds}, Alarms: ${ruleWithUpdatedConfig.targetAlarmIds}, Criteria: ${ruleWithUpdatedConfig.criteria}")
                                currentSettingsScreen = SettingsScreenType.RuleManagement // 保存后返回规则管理界面
                                ruleToEdit = null // 清空编辑状态，LaunchedEffect 会重置其他状态
                            },
                            onCancel = {
                                // 取消后返回规则管理界面
                                currentSettingsScreen = SettingsScreenType.RuleManagement
                                ruleToEdit = null // 清空编辑状态，LaunchedEffect 会重置其他状态
                            },
                            ruleViewModel = ruleViewModel, // 传递 ViewModel
                            onSelectCalendarsClick = { currentSelectedIds ->
                                currentRuleSelectedCalendarIds = currentSelectedIds
                                currentSettingsScreen = SettingsScreenType.RuleCalendarSelection
                                Log.d("NavigationDebug", "MainActivity: Navigating to Rule Calendar Selection with IDs: $currentSelectedIds")
                            },
                            // --- 处理选择应用闹钟回调 ---
                            onSelectAlarmsClick = { currentSelectedIds ->
                                currentRuleSelectedAlarmIds = currentSelectedIds // 保存当前已选中的闹钟 ID
                                currentSettingsScreen = SettingsScreenType.RuleAlarmSelection // <-- **导航到规则闹钟选择界面**
                                Log.d("NavigationDebug", "MainActivity: Navigating to Rule Alarm Selection with IDs: $currentSelectedIds")
                            },
                            // --- 处理定义规则条件回调 ---
                            onDefineCriteriaClick = { currentCriteria ->
                                currentRuleCriteria = currentCriteria
                                currentSettingsScreen = SettingsScreenType.RuleCriteriaDefinition
                                Log.d("NavigationDebug", "MainActivity: Navigating to Rule Criteria Definition with criteria: $currentCriteria")
                            }
                        )
                    }
                    // --- RuleAlarmSelection 的 Case ---
                    SettingsScreenType.RuleAlarmSelection -> {
                        RuleAlarmSelectionScreen(
                            availableAlarms = emptyList(), // <-- **暂时传递空列表，待实现真实闹钟加载**
                            initialSelectedAlarmIds = currentRuleSelectedAlarmIds,
                            onBackClick = {
                                // 返回 AddEditRule 界面
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            },
                            onAlarmsSelected = { selectedIds ->
                                // 从闹钟选择界面返回时，更新 currentRuleSelectedAlarmIds 状态
                                currentRuleSelectedAlarmIds = selectedIds.toSet() // <-- **更新闹钟选择状态**
                                Log.d("NavigationDebug", "MainActivity: Rule Alarm Selection Done. Selected IDs: $selectedIds")
                                // 返回 AddEditRule 界面
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            }
                        )
                    }
                    // -------------------------------------
                    // RuleCalendarSelection 的 Case (Keep this)
                    SettingsScreenType.RuleCalendarSelection -> {
                        CalendarSelectionScreen(
                            onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                            onCalendarsSelected = { selectedIds ->
                                currentRuleSelectedCalendarIds = selectedIds.toSet()
                                Log.d("NavigationDebug", "MainActivity: Rule Calendar Selection Done. Selected IDs: $selectedIds")
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            },
                            initialSelectedCalendarIds = currentRuleSelectedCalendarIds
                        )
                    }
                    // RuleCriteriaDefinition 的 Case (Keep this)
                    SettingsScreenType.RuleCriteriaDefinition -> {
                        RuleCriteriaDefinitionScreen(
                            initialCriteria = currentRuleCriteria,
                            onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                            onCriteriaSelected = { selectedCriteria ->
                                currentRuleCriteria = selectedCriteria
                                Log.d("NavigationDebug", "MainActivity: Rule Criteria Selected: $selectedCriteria")
                                currentSettingsScreen = SettingsScreenType.AddEditRule
                            }
                        )
                    }
                }
            } else {
                // 否则，根据选中的底部导航标签和 Bedtime 设置状态决定显示哪个屏幕 (Keep this)
                when (currentScreenIndex) {
                    0 -> AlarmScreen(
                        onSettingsClick = {
                            Log.d("NavigationDebug", "onSettingsClick lambda invoked for AlarmScreen")
                            showSettingsScreen = true
                        },
                    )
                    1 -> ClockScreen(
                        onSettingsClick = {
                            Log.d("NavigationDebug", "onSettingsClick lambda invoked for ClockScreen")
                            showSettingsScreen = true
                        },
                    )
                        2 -> TimerScreen() // Use the updated TimerScreen
                    3 -> StopwatchScreen() // <-- Corrected this line
                    4 -> {
                        // Handle Bedtime screen logic based on setup completion
                        if (!isBedtimeSetupComplete) {
                            // 如果 Bedtime 设置未完成，显示设置流程
                            when (bedtimeSetupStep) {
                                0 -> BedtimeIntroScreen(
                                    onGetStartedClick = { bedtimeSetupStep = 1 }
                                )
                                1 -> SetWakeUpAlarmScreen(
                                    onSkip = {
                                        isBedtimeSetupComplete = true
                                        prefs.edit { putBoolean("isBedtimeSetupComplete", true) }
                                        bedtimeSetupStep = 3
                                    },
                                    onNext = { bedtimeSetupStep = 2 }
                                )
                                2 -> SetBedtimeScreen(
                                    onSkip = {
                                        isBedtimeSetupComplete = true
                                        prefs.edit { putBoolean("isBedtimeSetupComplete", true) }
                                        bedtimeSetupStep = 3
                                    },
                                    onDone = {
                                        isBedtimeSetupComplete = true
                                        prefs.edit { putBoolean("isBedtimeSetupComplete", true) }
                                        bedtimeSetupStep = 3
                                        // TODO: Save the actual settings
                                    }
                                )
                                else -> BedtimeIntroScreen(onGetStartedClick = { bedtimeSetupStep = 1 })
                            }
                        } else {
                            // 如果 Bedtime 设置已完成，显示主 BedtimeScreen
                            BedtimeScreen(onSettingsClick = {
                                Log.d("NavigationDebug", "onSettingsClick lambda invoked for BedtimeScreen")
                                showSettingsScreen = true
                            })
                        }
                    }
                    else -> AlarmScreen(onSettingsClick = {
                        Log.d("NavigationDebug", "onSettingsClick lambda invoked for Default/AlarmScreen")
                        showSettingsScreen = true
                    })
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
