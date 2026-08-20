package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getHabits(): Flow<List<Habit>>
    fun getStats(): Flow<HomeStats>
    fun getFriends(): Flow<List<Friend>>
    fun getAlarms(): Flow<List<AlarmData>>
    fun getNextAlarm(): Flow<AlarmData?>
    suspend fun toggleHabit(habitId: String, isDone: Boolean): Result<Unit>
    suspend fun addAlarm(alarm: AlarmData): Result<Unit>
    suspend fun updateAlarm(alarm: AlarmData): Result<Unit>
    suspend fun toggleAlarm(alarmId: String, isEnabled: Boolean): Result<Unit>
    suspend fun deleteAlarm(alarmId: String): Result<Unit>
    fun getSoundCatalog(): Flow<List<SoundMetadata>>
    suspend fun seedSoundCatalog(): Result<Unit>
    suspend fun searchUsersByUsername(query: String): Result<List<Friend>>
    fun getCurrentUserUid(): String?
    fun listenToDuoAlarm(alarmId: String): Flow<String?>
    suspend fun setDuoAlarmWinner(alarmId: String, winnerUid: String): Result<Unit>
    suspend fun resetDuoAlarmWinner(alarmId: String): Result<Unit>
    suspend fun addHabit(habit: Habit): Result<Unit>
    suspend fun deleteHabit(habitId: String): Result<Unit>
    suspend fun updateHabit(habit: Habit): Result<Unit>
    suspend fun recordAlarmResult(alarmId: String, mode: String, isWin: Boolean): Result<Unit>
}

data class SoundMetadata(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val category: String = "Solo" // e.g., "Solo", "Battle", "Chill"
)

data class AlarmData(
    val id: String = "",
    val time: String, // e.g., "06:30"
    val label: String = "Alarm",
    val days: List<Int> = emptyList(),
    val isEnabled: Boolean = true,
    val mode: String = "Solo",
    val challenge: String = "Math",
    val isGroup: Boolean = false,
    val timestamp: Long = 0, // Next occurrence timestamp
    val soundUrl: String? = null,
    val soundName: String = "Default",
    val soundId: String? = null,
    val partnerUid: String? = null, // For Duo/Group shared alarm synchronization
    val partnerUsername: String? = null, // Username of tagged partner (e.g. maya.rises)
    val mathDifficulty: String = "Medium", // "Easy" (1 q), "Medium" (2 q), "Hard" (3 q)
    val bondName: String? = null
)

data class HomeStats(
    val streak: Int,
    val wins: Int,
    val losses: Int,
    val rank: String,
    val soloStreak: Int = 0,
    val soloWins: Int = 0,
    val soloLosses: Int = 0,
    val duoStreak: Int = 0,
    val duoWins: Int = 0,
    val duoLosses: Int = 0,
    val groupStreak: Int = 0,
    val groupWins: Int = 0,
    val groupLosses: Int = 0
)

expect fun getHomeRepository(): HomeRepository
