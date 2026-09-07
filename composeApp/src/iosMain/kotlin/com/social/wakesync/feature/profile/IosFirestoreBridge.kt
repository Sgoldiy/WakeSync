package com.social.wakesync.feature.profile

/**
 * Bridge interface that Swift implements to provide Firestore operations to Kotlin.
 * This follows the same bridging pattern used for Google Sign-In.
 */
interface IosFirestoreBridge {
    fun getCurrentUserUid(): String?
    fun getCurrentUserDisplayName(): String?
    fun saveProfile(
        username: String,
        avatar: String,
        goal: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun checkUsername(
        username: String,
        onResult: (Boolean) -> Unit,
        onError: (String) -> Unit
    )
    fun getUserDocument(
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    )
    fun setUserDocument(
        data: Map<String, Any?>,
        merge: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun queryUsers(
        field: String,
        value: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )

    // ── Alarm CRUD ──────────────────────────────────────────────────
    fun getAlarms(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun addAlarm(
        data: Map<String, Any?>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun updateAlarm(
        alarmId: String,
        data: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun deleteAlarm(
        alarmId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Habit CRUD ──────────────────────────────────────────────────
    fun getHabits(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun addHabit(
        data: Map<String, Any?>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun updateHabit(
        habitId: String,
        data: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )
    fun deleteHabit(
        habitId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Leaderboard ─────────────────────────────────────────────────
    fun getLeaderboard(
        sortField: String,
        limit: Int,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )

    // ── Social Feed ─────────────────────────────────────────────────
    fun getFeedPosts(
        limit: Int,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun addFeedPost(
        data: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Notifications ───────────────────────────────────────────────
    fun getNotifications(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun markNotificationRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Duo Alarm ───────────────────────────────────────────────────
    fun listenToDuoAlarm(
        alarmId: String,
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    )
    fun setDuoAlarmWinner(
        alarmId: String,
        winnerUid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Punishment ──────────────────────────────────────────────────
    fun writePunishment(
        punishmentId: String,
        data: Map<String, Any?>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    // ── Push Notifications ──────────────────────────────────────────
    fun registerDeviceToken(
        token: String,
        platform: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun sendPushNotification(
        toUid: String,
        title: String,
        body: String,
        type: String,
        data: Map<String, String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    )

    fun getFCMToken(
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    )
}

object IosFirestoreBridgeHolder {
    var bridge: IosFirestoreBridge? = null
}
