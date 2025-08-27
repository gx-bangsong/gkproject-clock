package com.gkprojct.clock

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val ruleDao = db.ruleDao()
                val alarmDao = db.alarmDao()
                val ruleEngine = RuleEngine(context.contentResolver)

                val alarmIdStr = intent.getStringExtra("ALARM_ID")
                if (alarmIdStr == null) {
                    Log.e("AlarmReceiver", "Received intent with null ALARM_ID.")
                    return@launch
                }
                val alarmId = UUID.fromString(alarmIdStr)
                val originalAlarmEntity = alarmDao.getAlarmById(alarmId)
                if (originalAlarmEntity == null) {
                    Log.e("AlarmReceiver", "Original alarm not found for ID: $alarmIdStr")
                    return@launch
                }

                // Fetch rules relevant to this alarm
                val allRules = ruleDao.getAllRules().first()
                val relevantRules = allRules.filter { it.targetAlarmIds.contains(alarmId) }.map {
                    Rule(
                        id = it.id, name = it.name, description = it.description, enabled = it.enabled,
                        targetAlarmIds = it.targetAlarmIds, calendarIds = it.calendarIds,
                        criteria = it.criteria, action = it.action
                    )
                }

                // Evaluate rules
                val resultAction = ruleEngine.evaluateRules(relevantRules, java.time.LocalDateTime.now())

                when (resultAction) {
                    is RuleAction.SkipNextAlarm -> {
                        Log.d("AlarmReceiver", "Alarm ${originalAlarmEntity.label} skipped by a rule.")
                        // Just don't show the notification
                    }
                    is RuleAction.AdjustAlarmTime -> {
                        val adjustedTime = resultAction.newTime
                        Log.d("AlarmReceiver", "Alarm ${originalAlarmEntity.label} adjusted to $adjustedTime by a rule.")
                        val originalCalendar = Calendar.getInstance().apply { timeInMillis = originalAlarmEntity.timeInMillis }
                        val adjustedCalendar = (originalCalendar.clone() as Calendar).apply {
                            set(Calendar.HOUR_OF_DAY, adjustedTime.hour)
                            set(Calendar.MINUTE, adjustedTime.minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        if (adjustedCalendar.before(Calendar.getInstance())) {
                            adjustedCalendar.add(Calendar.DAY_OF_YEAR, 1)
                        }

                        val adjustedAlarm = Alarm(
                            id = UUID.randomUUID(),
                            time = adjustedCalendar,
                            label = "${originalAlarmEntity.label} (Adjusted)",
                            isEnabled = true,
                            repeatingDays = emptySet()
                        )
                        AlarmScheduler.schedule(context, adjustedAlarm)
                        Log.d("AlarmReceiver", "Scheduled adjusted alarm for ${adjustedAlarm.time.time}")
                    }
                    null -> {
                        // No rules matched, fire original alarm
                        withContext(Dispatchers.Main) {
                            showNotification(context)
                        }
                    }
                }

                // Reschedule repeating alarms
                if (originalAlarmEntity.repeatingDays.isNotEmpty()) {
                        val nextAlarmCalendar = Calendar.getInstance().apply {
                            timeInMillis = originalAlarmEntity.timeInMillis
                        }
                        // Advance to the next occurrence
                        nextAlarmCalendar.add(Calendar.DAY_OF_YEAR, 1)

                        val originalAlarm = Alarm(
                            id = originalAlarmEntity.id,
                            time = nextAlarmCalendar,
                            label = originalAlarmEntity.label,
                            isEnabled = originalAlarmEntity.isEnabled,
                            repeatingDays = originalAlarmEntity.repeatingDays,
                            sound = originalAlarmEntity.sound,
                            vibrate = originalAlarmEntity.vibrate,
                            appliedRules = originalAlarmEntity.appliedRules
                        )
                        AlarmScheduler.schedule(context, originalAlarm)
                        Log.d("AlarmReceiver", "Rescheduled repeating alarm: ${originalAlarm.label} for ${nextAlarmCalendar.time}")
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context) {
        // 在调用 notify() 前检查 POST_NOTIFICATIONS 权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("AlarmReceiver", "Notification permission not granted. Cannot show notification.")
            return
        }

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
