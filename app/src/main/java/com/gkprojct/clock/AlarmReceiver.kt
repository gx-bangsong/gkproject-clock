package com.gkprojct.clock

import android.Manifest
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        // 触发时执行的动作：例如，显示一个通知或做其他事情
        val notification = createNotification(context)
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(AlarmService.NOTIFICATION_ID, notification)

        // 你可以在这里做更多操作，例如显示 Toast
        Toast.makeText(context, "闹钟响了！", Toast.LENGTH_SHORT).show()
    }

    // 创建通知
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
