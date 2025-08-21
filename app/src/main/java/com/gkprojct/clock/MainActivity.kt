package com.gkprojct.clock

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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gkprojct.clock.ui.theme.ClockTheme
import com.gkprojct.clock.vm.AlarmViewModel
import com.gkprojct.clock.vm.AlarmViewModelFactory
import com.gkprojct.clock.vm.AppDatabase
import java.time.DayOfWeek
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClockTheme {
                AppContent()
            }
        }
    }
}

enum class SettingsScreenType {
    Main, CalendarSelection, RuleManagement, AddEditRule, RuleCalendarSelection, RuleAlarmSelection, RuleCriteriaDefinition, ShiftWorkHolidayCalendarSelection
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val ruleDao = remember { database.ruleDao() }
    val alarmDao = remember { database.alarmDao() }
    val ruleViewModel: RuleViewModel = viewModel(factory = RuleViewModelFactory(ruleDao))
    val alarmViewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(alarmDao))
    val prefs = remember { context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE) }
    var isBedtimeSetupComplete by remember { mutableStateOf(prefs.getBoolean("isBedtimeSetupComplete", false)) }
    var currentScreenIndex by remember { mutableIntStateOf(0) }
    var bedtimeSetupStep by remember { mutableIntStateOf(0) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var currentSettingsScreen by remember { mutableStateOf(SettingsScreenType.Main) }
    var ruleToEdit by remember { mutableStateOf<Rule?>(null) }
    var currentRuleSelectedCalendarIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var currentRuleSelectedAlarmIds by remember { mutableStateOf<Set<UUID>>(emptySet()) }
    var currentRuleCriteria by remember { mutableStateOf<RuleCriteria>(RuleCriteria.AlwaysTrue) }
    var currentShiftWorkHolidayIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    val sharedPreferences = remember { context.getSharedPreferences("CalendarPrefs", Context.MODE_PRIVATE) }
    val saveSelectedCalendarIds: (List<Long>) -> Unit = { selectedIds ->
        sharedPreferences.edit().putStringSet("selectedCalendarIds", selectedIds.map { it.toString() }.toSet()).apply()
    }
    val loadSelectedCalendarIds: () -> Set<Long> = {
        sharedPreferences.getStringSet("selectedCalendarIds", emptySet())?.map { it.toLong() }?.toSet() ?: emptySet()
    }
    val initiallySelectedCalendarIds = remember(showSettingsScreen) {
        if (showSettingsScreen) loadSelectedCalendarIds() else emptySet()
    }

    LaunchedEffect(ruleToEdit) {
        ruleToEdit?.let {
            currentRuleSelectedCalendarIds = it.calendarIds
            currentRuleSelectedAlarmIds = it.targetAlarmIds
            currentRuleCriteria = it.criteria
            if (it.criteria is RuleCriteria.ShiftWork) {
                currentShiftWorkHolidayIds = (it.criteria as RuleCriteria.ShiftWork).holidayCalendarIds
            }
        } ?: run {
            currentRuleSelectedCalendarIds = emptySet()
            currentRuleSelectedAlarmIds = emptySet()
            currentRuleCriteria = RuleCriteria.AlwaysTrue
            currentShiftWorkHolidayIds = emptySet()
        }
    }

    val alarms by alarmViewModel.allAlarms.collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            if (!showSettingsScreen && isBedtimeSetupComplete) {
                NavigationBar {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            selected = currentScreenIndex == index,
                            onClick = { currentScreenIndex = index },
                            icon = { Icon(item.icon, contentDescription = item.name) },
                            label = { Text(item.name) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (showSettingsScreen) {
                when (currentSettingsScreen) {
                    SettingsScreenType.Main -> SettingsScreen(
                        onBackClick = { showSettingsScreen = false },
                        onSelectCalendarClick = { currentSettingsScreen = SettingsScreenType.CalendarSelection },
                        onManageRulesClick = { currentSettingsScreen = SettingsScreenType.RuleManagement },
                        onCalendarsSelectionDone = saveSelectedCalendarIds,
                        initialSelectedCalendarIds = initiallySelectedCalendarIds
                    )
                    SettingsScreenType.CalendarSelection, SettingsScreenType.RuleCalendarSelection, SettingsScreenType.ShiftWorkHolidayCalendarSelection -> CalendarSelectionScreen(
                        onBackClick = {
                            currentSettingsScreen = when (currentSettingsScreen) {
                                SettingsScreenType.RuleCalendarSelection -> SettingsScreenType.AddEditRule
                                SettingsScreenType.ShiftWorkHolidayCalendarSelection -> SettingsScreenType.RuleCriteriaDefinition
                                else -> SettingsScreenType.Main
                            }
                        },
                        onCalendarsSelected = { selectedIds ->
                            when (currentSettingsScreen) {
                                SettingsScreenType.RuleCalendarSelection -> {
                                    currentRuleSelectedCalendarIds = selectedIds.toSet()
                                    currentSettingsScreen = SettingsScreenType.AddEditRule
                                }
                                SettingsScreenType.ShiftWorkHolidayCalendarSelection -> {
                                    currentShiftWorkHolidayIds = selectedIds.toSet()
                                    if (currentRuleCriteria is RuleCriteria.ShiftWork) {
                                        currentRuleCriteria = (currentRuleCriteria as RuleCriteria.ShiftWork).copy(holidayCalendarIds = selectedIds.toSet())
                                    }
                                    currentSettingsScreen = SettingsScreenType.RuleCriteriaDefinition
                                }
                                else -> {
                                    saveSelectedCalendarIds(selectedIds)
                                    currentSettingsScreen = SettingsScreenType.Main
                                }
                            }
                        },
                        initialSelectedCalendarIds = when (currentSettingsScreen) {
                            SettingsScreenType.RuleCalendarSelection -> currentRuleSelectedCalendarIds
                            SettingsScreenType.ShiftWorkHolidayCalendarSelection -> currentShiftWorkHolidayIds
                            else -> initiallySelectedCalendarIds
                        }
                    )
                    SettingsScreenType.RuleManagement -> RuleManagementScreen(
                        onBackClick = { currentSettingsScreen = SettingsScreenType.Main },
                        onAddRuleClick = {
                            ruleToEdit = null
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        },
                        onRuleClick = { rule ->
                            ruleToEdit = rule
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        },
                        ruleViewModel = ruleViewModel
                    )
                    SettingsScreenType.AddEditRule -> AddEditRuleScreen(
                        initialRule = ruleToEdit,
                        onSaveRule = { savedRule ->
                            ruleViewModel.saveRule(savedRule)
                            currentSettingsScreen = SettingsScreenType.RuleManagement
                        },
                        onCancel = { currentSettingsScreen = SettingsScreenType.RuleManagement },
                        ruleViewModel = ruleViewModel,
                        onSelectCalendarsClick = { currentIds ->
                            currentRuleSelectedCalendarIds = currentIds
                            currentSettingsScreen = SettingsScreenType.RuleCalendarSelection
                        },
                        onSelectAlarmsClick = { currentIds ->
                            currentRuleSelectedAlarmIds = currentIds
                            currentSettingsScreen = SettingsScreenType.RuleAlarmSelection
                        },
                        onDefineCriteriaClick = { criteria ->
                            currentRuleCriteria = criteria
                            if (criteria is RuleCriteria.ShiftWork) {
                                currentShiftWorkHolidayIds = criteria.holidayCalendarIds
                            }
                            currentSettingsScreen = SettingsScreenType.RuleCriteriaDefinition
                        }
                    )
                    SettingsScreenType.RuleAlarmSelection -> RuleAlarmSelectionScreen(
                        availableAlarms = alarms,
                        initialSelectedAlarmIds = currentRuleSelectedAlarmIds,
                        onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                        onAlarmsSelected = { selectedIds ->
                            currentRuleSelectedAlarmIds = selectedIds
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        }
                    )
                    SettingsScreenType.RuleCriteriaDefinition -> RuleCriteriaDefinitionScreen(
                        initialCriteria = currentRuleCriteria,
                        onBackClick = { currentSettingsScreen = SettingsScreenType.AddEditRule },
                        onCriteriaSelected = { selectedCriteria ->
                            currentRuleCriteria = selectedCriteria
                            currentSettingsScreen = SettingsScreenType.AddEditRule
                        },
                        onSelectHolidayCalendarsClick = {
                            currentSettingsScreen = SettingsScreenType.ShiftWorkHolidayCalendarSelection
                        }
                    )
                }
            } else if (!isBedtimeSetupComplete) {
                Box(modifier = Modifier.fillMaxSize()) { Text("Bedtime setup is not complete (Screens Missing)") }
            } else {
                when (currentScreenIndex) {
                    0 -> AlarmScreen(onSettingsClick = { showSettingsScreen = true }, ruleViewModel = ruleViewModel)
                    1 -> ClockScreen(onSettingsClick = { showSettingsScreen = true })
                    2 -> TimerScreen(timerViewModel = viewModel())
                    3 -> StopwatchScreen(stopwatchViewModel = viewModel())
                    4 -> BedtimeScreen()
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