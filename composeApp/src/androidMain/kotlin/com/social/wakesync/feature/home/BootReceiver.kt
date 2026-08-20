package com.social.wakesync.feature.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            Log.d("BootReceiver", "Received ${intent.action}, rescheduling alarms...")

            // goAsync() keeps the receiver alive beyond the default 10-second ANR timeout,
            // allowing the coroutine to complete its work before the process is killed.
            val pendingResult = goAsync()

            initializeAlarmScheduler(context)
            val scheduler = getAlarmScheduler()
            val repository = getHomeRepository()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Timeout after 30 seconds to prevent infinite blocking
                    withTimeout(30_000L) {
                        val alarms = repository.getAlarms().first()
                        val now = System.currentTimeMillis()
                        var scheduledCount = 0

                        alarms.forEach { alarm ->
                            if (alarm.isEnabled && alarm.timestamp > now) {
                                scheduler.schedule(alarm)
                                scheduledCount++
                            }
                        }
                        Log.d("BootReceiver", "Rescheduled $scheduledCount alarms after ${intent.action}")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Failed to reschedule alarms", e)
                } finally {
                    // Signal that async work is done — prevents OS from killing the process early
                    pendingResult.finish()
                }
            }
        }
    }
}
