package com.social.wakesync.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AlarmState {
    var isRinging by mutableStateOf(false)
    var activeAlarmId by mutableStateOf<String?>(null)
    var activeAlarmMode by mutableStateOf("Solo")
    // Comma-separated list of the user's selected wake-up games (user picks exactly 3).
    var activeAlarmChallenge by mutableStateOf("Memory Chain,Color Clash,Speed Tap")
    var activeAlarmPartnerUsername by mutableStateOf<String?>(null)
    var previousStreak by mutableStateOf(0)
    var showStreakSave by mutableStateOf(false)
    var showStreakBroken by mutableStateOf(false)
    var activePunishment by mutableStateOf<Punishment?>(null)

    /** Parsed game list — resilient to legacy single-game values. */
    val activeAlarmGames: List<String>
        get() = activeAlarmChallenge.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}