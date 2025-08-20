package com.gkprojct.clock

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gkprojct.clock.vm.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val ruleDao = db.ruleDao()
                val ruleEngine = RuleEngine(context.contentResolver)
                val rules = ruleDao.getAllRules().first()

                var shouldSkip = false
                var adjustedTime: LocalTime? = null

                val alarmIdStr = intent.getStringExtra("ALARM_ID")
                if (alarmIdStr == null) {
                    // No alarm ID, show notification as normal
                    withContext(Dispatchers.Main) {
                        showNotification(context)
                    }
                    return@launch
                }

                val alarmId = UUID.fromString(alarmIdStr)

                for (ruleEntity in rules) {
                    if (!ruleEntity.enabled) continue

                    if (ruleEntity.targetAlarmIds.contains(alarmId)) {
                        val rule = Rule(
                            id = ruleEntity.id,
                            name = ruleEntity.name,
                            description = ruleEntity.description,
                            enabled = ruleEntity.enabled,
                            targetAlarmIds = ruleEntity.targetAlarmIds,
                            calendarIds = ruleEntity.calendarIds,
                            criteria = ruleEntity.criteria,
                            action = ruleEntity.action
                        )

                        if (ruleEngine.evaluate(rule, Instant.now())) {
                            when (val action = rule.action) {
                                is RuleAction.SkipNextAlarm -> {
                                    shouldSkip = true
                                    break
                                }
                                is RuleAction.AdjustAlarmTime -> {
                                    adjustedTime = action.newTime
                                }
                            }
                        }
                    }
                }

                if (shouldSkip) {
                    Log.d("AlarmReceiver", "Alarm skipped by a rule.")
                } else if (adjustedTime != null) {
                    Log.d("AlarmReceiver", "Alarm adjusted to $adjustedTime by a rule.")
                    // TODO: Reschedule the alarm
                    // For now, we just show the original notification.
                    withContext(Dispatchers.Main) {
                        showNotification(context)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showNotification(context)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        val notification = createNotification(context)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(AlarmService.NOTIFICATION_ID, notification)
        Toast.makeText(context, "闹钟响了！", Toast.LENGTH_SHORT).show()
    }

    private fun createNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, AlarmService.CHANNEL_ID)
            .setContentTitle("闹钟")
            .setContentText("时间到了！")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
    }
}
