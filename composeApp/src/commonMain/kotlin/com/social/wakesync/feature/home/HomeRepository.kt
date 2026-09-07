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
    suspend fun assignPunishment(
        alarmId: String,
        loserUid: String,
        loserUsername: String,
        challengerUid: String,
        challengerUsername: String,
        mode: String,
        challenge: String
    ): Result<Punishment>
    suspend fun getActivePunishment(): Punishment?
    suspend fun submitProof(punishmentId: String, proofUrl: String): Result<Unit>
    suspend fun completePunishment(punishmentId: String): Result<Unit>
    fun getLeaderboard(mode: String, isGlobal: Boolean): Flow<List<LeaderboardUser>>
    fun getGroupLeaderboard(groupId: String = "Morning Crew"): Flow<List<GroupMember>>
    fun getSocialFeed(): Flow<List<FeedPost>>
    suspend fun postToFeed(content: String, badge: String): Result<Unit>
    fun getNotifications(): Flow<List<FeedNotification>>
    fun getStories(): Flow<List<StoryItem>>
    suspend fun postStory(caption: String, badgeText: String, badgeColorHex: String, bgStartHex: String, bgEndHex: String): Result<Unit>
    suspend fun markNotificationRead(notificationId: String): Result<Unit>
    suspend fun getNotificationPreferences(): NotificationPreferences
    suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Result<Unit>
    suspend fun registerDeviceToken(token: String, platform: String): Result<Unit>
    suspend fun sendPushNotification(
        toUid: String,
        title: String,
        body: String,
        type: String,
        data: Map<String, String> = emptyMap()
    ): Result<Unit>
}

data class SoundMetadata(
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val category: String = "Solo" // e.g., "Solo", "Battle", "Chill"
)

data class Punishment(
    val id: String = "",
    val loserUid: String = "",
    val loserUsername: String = "",
    val challengerUid: String = "",
    val challengerUsername: String = "",
    val alarmId: String = "",
    val mode: String = "Solo", // Solo, Duo, Group
    val challenge: String = "",
    val punishmentType: String = "",
    val punishmentEmoji: String = "",
    val punishmentDetail: String = "",
    val status: String = "Assigned", // Assigned, ProofSubmitted, Completed, Escalated, Expired
    val createdAt: Long = 0L,
    val dueAt: Long = 0L,
    val proofUrl: String? = null,
    val proofSubmittedAt: Long? = null,
    val completedAt: Long? = null
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

data class FeedPost(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val avatar: String = "",
    val content: String = "",
    val badgeText: String = "",
    val badgeColorHex: String = "#22C55E",
    val streak: Int = 0,
    val createdAt: Long = 0L,
    val reactions: Map<String, Int> = emptyMap()
)

data class FeedNotification(
    val id: String = "",
    val type: String = "",
    val message: String = "",
    val fromUsername: String = "",
    val fromUid: String = "",
    val punishmentId: String? = null,
    val createdAt: Long = 0L,
    val read: Boolean = false
)

data class NotificationPreferences(
    val alarmSoundsEnabled: Boolean = true,
    val punishmentSoundsEnabled: Boolean = true,
    val socialSoundsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val quietHoursStart: Int = -1,
    val quietHoursEnd: Int = -1
)

expect fun getHomeRepository(): HomeRepository
