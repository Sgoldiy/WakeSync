package com.social.wakesync.feature.home

interface AlarmScheduler {
    fun schedule(alarm: AlarmData)
    fun cancel(alarmId: String)
}

expect fun getAlarmScheduler(): AlarmScheduler
