package com.social.wakesync.feature.home

import platform.UserNotifications.*
import platform.Foundation.*
import kotlinx.datetime.*

class IosAlarmScheduler : AlarmScheduler {
    override fun schedule(alarm: AlarmData) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        val content = UNMutableNotificationContent().apply {
            setTitle(if (alarm.mode == "Solo") "⏰ Solo Wake Up!" else alarm.label)
            setBody("It's ${alarm.time}! Time to wake up and sync.")
            
            // For Solo mode, we try to use a specific sound file.
            // On iOS, the sound file must be in the main bundle (e.g. solo_alarm.wav)
            val notificationSound = if (alarm.mode == "Solo") {
                UNNotificationSound.soundNamed("solo_alarm.wav")
            } else {
                UNNotificationSound.defaultCriticalSound()
            }
            setSound(notificationSound)
            
            setUserInfo(mapOf(
                "ALARM_ID" to alarm.id,
                "ALARM_MODE" to alarm.mode
            ))
            setCategoryIdentifier("ALARM_CATEGORY")
        }

        val date = NSDate.dateWithTimeIntervalSince1970(alarm.timestamp / 1000.0)
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or 
            NSCalendarUnitHour or NSCalendarUnitMinute, 
            fromDate = date
        )

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(components, repeats = false)
        val request = UNNotificationRequest.requestWithIdentifier(alarm.id, content, trigger)

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                println("Error scheduling iOS notification: ${error.localizedDescription}")
            }
        }
    }

    override fun cancel(alarmId: String) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        center.removePendingNotificationRequestsWithIdentifiers(listOf(alarmId))
    }
}

actual fun getAlarmScheduler(): AlarmScheduler = IosAlarmScheduler()
