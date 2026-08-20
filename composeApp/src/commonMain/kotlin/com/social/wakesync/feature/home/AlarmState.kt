package com.social.wakesync.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AlarmState {
    var isRinging by mutableStateOf(false)
    var activeAlarmId by mutableStateOf<String?>(null)
    var activeAlarmMode by mutableStateOf("Solo")
    var activeAlarmChallenge by mutableStateOf("Math")
    var activeAlarmPartnerUsername by mutableStateOf<String?>(null)
    var showStreakSave by mutableStateOf(false)
    var showStreakBroken by mutableStateOf(false)
}