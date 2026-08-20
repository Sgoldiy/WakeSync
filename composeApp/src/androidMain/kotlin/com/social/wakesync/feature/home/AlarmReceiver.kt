package com.social.wakesync.feature.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID")
        val alarmTime = intent.getStringExtra("ALARM_TIME")
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "WakeSync Alarm"
        val alarmMode = intent.getStringExtra("ALARM_MODE") ?: "Solo"
        val alarmChallenge = intent.getStringExtra("ALARM_CHALLENGE") ?: "Math"
        val alarmPartners = intent.getStringExtra("ALARM_PARTNERS")
        val soundId = intent.getStringExtra("SOUND_ID")

        Log.d("AlarmReceiver", "Alarm fired! ID: $alarmId, Time: $alarmTime, Mode: $alarmMode, Challenge: $alarmChallenge, Partners: $alarmPartners")

        // Acquire a temporary WakeLock to keep CPU alive while we start the service.
        // The service will acquire its own WakeLock.
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WakeSync::AlarmReceiverWakeLock"
        )
        wakeLock.acquire(10_000L) // 10 seconds max — service will take over

        try {
            // Start the AlarmService as a foreground service
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_TIME", alarmTime)
                putExtra("ALARM_LABEL", alarmLabel)
                putExtra("ALARM_MODE", alarmMode)
                putExtra("ALARM_CHALLENGE", alarmChallenge)
                putExtra("ALARM_PARTNERS", alarmPartners)
                putExtra("SOUND_ID", soundId)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            Log.d("AlarmReceiver", "AlarmService started for alarm: $alarmId")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to start AlarmService", e)
        } finally {
            // Release if still held — service has its own WakeLock
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
