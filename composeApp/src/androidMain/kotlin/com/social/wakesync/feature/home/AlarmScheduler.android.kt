package com.social.wakesync.feature.home

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class AndroidAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: AlarmData) {
        if (!alarm.isEnabled) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_TIME", alarm.time)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_MODE", alarm.mode)
            putExtra("ALARM_CHALLENGE", alarm.challenge)
            putExtra("ALARM_PARTNERS", alarm.partnerUsername)
            putExtra("SOUND_URL", alarm.soundUrl)
            putExtra("SOUND_ID", alarm.soundId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = alarm.timestamp

        // Use setAlarmClock for guaranteed exact delivery — this is what professional
        // alarm apps use. It bypasses Doze mode completely and shows the alarm icon
        // on the status bar and lock screen.
        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id.hashCode(),
            Intent(context, com.social.wakesync.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, showIntent),
                    pendingIntent
                )
            } else {
                // Fallback if user hasn't granted exact alarm permission
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, showIntent),
                pendingIntent
            )
        }
        Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} at $triggerTime using setAlarmClock")
    }

    override fun cancel(alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Cancelled alarm $alarmId")
    }
}

private var androidAlarmScheduler: AlarmScheduler? = null

fun initializeAlarmScheduler(context: Context) {
    androidAlarmScheduler = AndroidAlarmScheduler(context)
}

actual fun getAlarmScheduler(): AlarmScheduler {
    return androidAlarmScheduler ?: throw IllegalStateException("AlarmScheduler not initialized")
}
