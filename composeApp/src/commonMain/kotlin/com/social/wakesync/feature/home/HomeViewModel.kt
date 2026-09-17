package com.social.wakesync.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.social.wakesync.feature.profile.getProfileRepository
import com.social.wakesync.getPlatform
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "Jake",
    val avatarEmoji: String = "🤯",
    val dateText: String = "",
    val streak: Int = 23,
    val wins: Int = 18,
    val losses: Int = 5,
    val rank: String = "#4",
    val soloStreak: Int = 0,
    val soloWins: Int = 0,
    val soloLosses: Int = 0,
    val duoStreak: Int = 0,
    val duoWins: Int = 0,
    val duoLosses: Int = 0,
    val groupStreak: Int = 0,
    val groupWins: Int = 0,
    val groupLosses: Int = 0,
    val habitStreak: Int = 0,
    val habitWins: Int = 0,
    val habitLosses: Int = 0,
    val nextAlarmTime: String = "6:30 AM",
    val timeLeftToAlarm: String = "9h 14m",
    val isGroupAlarm: Boolean = true,
    val activeAlarmMode: String = "Solo",
    val hasAlarmToday: Boolean = false,
    val suggestedTime: String = "6:30 AM",
    val alarms: List<AlarmData> = emptyList(),
    val habits: List<Habit> = listOf(
        Habit("1", "Morning Run", HabitIconType.RUN, true),
        Habit("2", "Cold Shower", HabitIconType.SHOWER, true),
        Habit("3", "No Phone 1hr", HabitIconType.NO_PHONE, true),
        Habit("4", "Read 20 mins", HabitIconType.READING, false),
        Habit("5", "Stretch", HabitIconType.STRETCH, false)
    ),
    val friends: List<Friend> = listOf(
        Friend("1", "maya.rises", "🦁", 41, FriendStatus.ACTIVE),
        Friend("2", "5amclub..", "🐺", 89, FriendStatus.INACTIVE),
        Friend("3", "nocturna..", "🦊", 7, FriendStatus.FAILED),
        Friend("4", "grind.rio", "🐻", 15, FriendStatus.NEW)
    ),
    val sounds: List<SoundMetadata> = emptyList(),
    val selectedSound: SoundMetadata? = null,
    val sortOrder: AlarmSortOrder = AlarmSortOrder.TIME,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    val isOnline: Boolean = true,
    val pendingSyncCount: Int = 0,
    val errorMessage: String? = null
)

enum class AlarmSortOrder {
    TIME, RECENT
}

data class Habit(
    val id: String,
    val title: String,
    val iconType: HabitIconType,
    val isDone: Boolean,
    val streak: Int = 0,
    val frequency: String = "Daily",
    val reminderTime: String = "6:15 AM",
    val partnerUsername: String? = null,
    val bondName: String? = null
)

enum class HabitIconType {
    RUN, SHOWER, NO_PHONE, READING, STRETCH
}

data class Friend(
    val id: String,
    val name: String,
    val avatar: String,
    val streak: Int,
    val status: FriendStatus
)

enum class FriendStatus {
    ACTIVE, INACTIVE, FAILED, NEW
}

fun getCurrentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val profileRepository = getProfileRepository()
    private val homeRepository = getHomeRepository()
    private val alarmScheduler = getAlarmScheduler()
    private val soundPlayer = getSoundPlayer()
    private val networkMonitor = getNetworkMonitor()

    private var nextAlarmTimestamp: Long = 0

    init {
        observeFirestoreData()
        startClockUpdates()
        seedDatabase()
        registerPushToken()
        loadNotificationPreferences()
        observeNetworkState()
    }

    private fun seedDatabase() {
        viewModelScope.launch {
            try {
                homeRepository.seedSoundCatalog()
            } catch (e: Exception) {
                // Silently fail or log if already seeded
            }
        }
    }

    private fun observeNetworkState() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }

                // When coming back online, sync pending operations
                if (online && PendingOperations.hasPending()) {
                    syncPendingOperations()
                }
            }
        }

        // Track pending operations count
        viewModelScope.launch {
            PendingOperations.pendingOps.collect { ops ->
                _uiState.update { it.copy(pendingSyncCount = ops.size) }
            }
        }
    }

    private suspend fun syncPendingOperations() {
        val ops = PendingOperations.pendingOps.value.toList()
        for (op in ops) {
            try {
                when (op.type) {
                    "habit_toggle" -> {
                        val habitId = op.payload["habitId"] ?: continue
                        val isDone = op.payload["isDone"]?.toBoolean() ?: false
                        homeRepository.toggleHabit(habitId, isDone)
                    }
                    "alarm_add" -> {
                        // Re-add alarm from stored data
                        val alarmJson = op.payload["alarm"] ?: continue
                        // Alarm will be re-synced from Firestore on next fetch
                    }
                    "habit_add" -> {
                        // Habit will be re-synced from Firestore on next fetch
                    }
                }
                PendingOperations.removeOperation(op.id)
            } catch (e: Exception) {
                // Keep the operation in the queue for next sync attempt
            }
        }
    }

    private fun registerPushToken() {
        viewModelScope.launch {
            try {
                val token = getDeviceToken()
                if (token != null) {
                    homeRepository.registerDeviceToken(token, getPlatform().name)
                }
            } catch (e: Exception) {
                // Push token registration is non-critical
            }
        }
    }

    private fun observeFirestoreData() {
        viewModelScope.launch {
            loadUserProfile()
        }

        homeRepository.getAlarms()
            .onEach { alarms ->
                _uiState.update { state ->
                    state.copy(alarms = sortAlarmsList(alarms, state.sortOrder))
                }

                val activeAlarms = alarms.filter { it.isEnabled }
                val todayAlarms = activeAlarms.filter { alarm ->
                    val parts = alarm.time.split(":")
                    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                    val nextTrigger = calculateNextOccurrence(hour, minute, alarm.days)
                    isTimestampToday(nextTrigger)
                }

                if (todayAlarms.isNotEmpty()) {
                    val nextAlarm = todayAlarms.minByOrNull { alarm ->
                        val parts = alarm.time.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        calculateNextOccurrence(hour, minute, alarm.days)
                    }

                    if (nextAlarm != null) {
                        val parts = nextAlarm.time.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        nextAlarmTimestamp = calculateNextOccurrence(hour, minute, nextAlarm.days)
                        AlarmState.activeAlarmId = nextAlarm.id
                        AlarmState.activeAlarmMode = nextAlarm.mode
                        AlarmState.activeAlarmChallenge = nextAlarm.challenge
                        AlarmState.activeAlarmPartnerUsername = nextAlarm.partnerUsername
                        _uiState.update { it.copy(
                            nextAlarmTime = formatAlarmTime12h(nextAlarm.time),
                            isGroupAlarm = nextAlarm.isGroup,
                            activeAlarmMode = nextAlarm.mode,
                            hasAlarmToday = true
                        ) }
                        updateTimeLeft()
                    } else {
                        nextAlarmTimestamp = 0L
                        _uiState.update { it.copy(hasAlarmToday = false) }
                    }
                } else {
                    nextAlarmTimestamp = 0L
                    _uiState.update { it.copy(hasAlarmToday = false) }
                }

                // Sync system alarms with scheduler
                alarms.forEach { alarm ->
                    if (alarm.isEnabled) {
                        val parts = alarm.time.split(":")
                        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        val nextTrigger = calculateNextOccurrence(hour, minute, alarm.days)
                        alarmScheduler.schedule(alarm.copy(timestamp = nextTrigger))
                    } else {
                        alarmScheduler.cancel(alarm.id)
                    }
                }
            }
            .catch { _ -> }
            .launchIn(viewModelScope)

        homeRepository.getHabits()
            .onEach { habits ->
                if (habits.isNotEmpty()) {
                    _uiState.update { it.copy(habits = habits, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            .catch { _ ->
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)

        homeRepository.getStats()
            .onEach { stats ->
                _uiState.update { it.copy(
                    streak = stats.streak,
                    wins = stats.wins,
                    losses = stats.losses,
                    rank = stats.rank,
                    soloStreak = stats.soloStreak,
                    soloWins = stats.soloWins,
                    soloLosses = stats.soloLosses,
                    duoStreak = stats.duoStreak,
                    duoWins = stats.duoWins,
                    duoLosses = stats.duoLosses,
                    groupStreak = stats.groupStreak,
                    groupWins = stats.groupWins,
                    groupLosses = stats.groupLosses,
                    habitStreak = stats.habitStreak,
                    habitWins = stats.habitWins,
                    habitLosses = stats.habitLosses
                ) }
            }
            .catch { _ -> }
            .launchIn(viewModelScope)

        homeRepository.getFriends()
            .onEach { friends ->
                if (friends.isNotEmpty()) {
                    _uiState.update { it.copy(friends = friends) }
                }
            }
            .catch { _ -> }
            .launchIn(viewModelScope)

        homeRepository.getSoundCatalog()
            .onEach { sounds ->
                _uiState.update { it.copy(sounds = sounds) }
            }
            .catch { _ -> }
            .launchIn(viewModelScope)
    }

    private fun startClockUpdates() {
        viewModelScope.launch {
            while (true) {
                updateDateTimeAndAlarm()
                delay(60000)
            }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            loadUserProfile()
        }
    }

    private fun updateDateTimeAndAlarm() {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val nowDateTime = now.toLocalDateTime(timeZone)

        val dayOfWeek = nowDateTime.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        val month = nowDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val dateText = "$dayOfWeek, $month ${nowDateTime.day}"

        _uiState.update { it.copy(dateText = dateText) }
        updateTimeLeft()
    }

    private fun updateTimeLeft() {
        if (nextAlarmTimestamp <= 0L) return
        
        val now = Clock.System.now().toEpochMilliseconds()
        val diff: Long = nextAlarmTimestamp - now
        
        if (diff > 0L) {
            val hours = diff / 3600000L
            val minutes = (diff / 60000L) % 60L
            _uiState.update { it.copy(timeLeftToAlarm = "${hours}h ${minutes}m") }
        } else {
            _uiState.update { it.copy(timeLeftToAlarm = "Now") }
        }
    }

    // formatAlarmTime is now formatAlarmTime12h() in AlarmUtils.kt

    private suspend fun loadUserProfile() {
        val profileResult = profileRepository.getCurrentProfile()
        profileResult.getOrNull()?.let { profile ->
            _uiState.update { it.copy(
                userName = profile.username.ifBlank { "User" },
                avatarEmoji = profile.avatar.ifBlank { "👤" }
            ) }
        }
    }

    fun toggleHabit(habitId: String) {
        val habit = _uiState.value.habits.find { it.id == habitId } ?: return
        // Once a habit is completed, it CANNOT be undone!
        if (habit.isDone) return

        val newStatus = true
        val updatedHabits = _uiState.value.habits.map {
            if (it.id == habitId) {
                it.copy(isDone = true, streak = it.streak + 1)
            } else it
        }

        val maxHabitStreak = updatedHabits.maxOfOrNull { it.streak } ?: 0
        val habitWinsCount = updatedHabits.count { it.isDone }

        _uiState.update {
            it.copy(
                habits = updatedHabits,
                habitStreak = maxOf(it.habitStreak + 1, maxHabitStreak),
                habitWins = habitWinsCount
            )
        }

        viewModelScope.launch {
            val result = homeRepository.toggleHabit(habitId, newStatus)
            if (result.isFailure) {
                // If offline, queue the operation for later sync
                if (!_uiState.value.isOnline) {
                    PendingOperations.addOperation(
                        PendingOperations.PendingOp(
                            id = "habit_toggle_$habitId",
                            type = "habit_toggle",
                            timestamp = getCurrentTimeMillis(),
                            payload = mapOf(
                                "habitId" to habitId,
                                "isDone" to "true"
                            )
                        )
                    )
                } else {
                    _uiState.update { it.copy(errorMessage = "Failed to update habit") }
                }
            }
        }
    }

    fun updateAvatar(emoji: String) {
        val oldEmoji = _uiState.value.avatarEmoji
        _uiState.update { it.copy(avatarEmoji = emoji) }
        
        viewModelScope.launch {
            val result = profileRepository.updateAvatar(emoji)
            if (result.isFailure) {
                _uiState.update { it.copy(
                    avatarEmoji = oldEmoji,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update avatar"
                ) }
            }
        }
    }

    fun selectSound(sound: SoundMetadata) {
        _uiState.update { it.copy(selectedSound = sound) }
        
        // Play preview
        soundPlayer.playPreview(sound.url)
        
        // Pre-download the sound for offline use
        viewModelScope.launch {
            try {
                getSoundDownloader().downloadSound(sound.url, "${sound.id}.mp3")
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to download sound") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }

    suspend fun searchUsers(query: String): List<Friend> {
        val result = homeRepository.searchUsersByUsername(query)
        return result.getOrDefault(emptyList())
    }

    suspend fun fetchUserProfile(username: String): Friend? {
        val result = homeRepository.searchUsersByUsername(username)
        return result.getOrNull()?.firstOrNull { it.name.equals(username, ignoreCase = true) }
    }

    fun getCurrentUserUid(): String? {
        return homeRepository.getCurrentUserUid()
    }

    fun listenToDuoAlarm(alarmId: String): Flow<String?> {
        return homeRepository.listenToDuoAlarm(alarmId)
    }

    fun setDuoAlarmWinner(alarmId: String, winnerUid: String) {
        viewModelScope.launch {
            homeRepository.setDuoAlarmWinner(alarmId, winnerUid)
        }
    }

    fun resetDuoAlarmWinner(alarmId: String) {
        viewModelScope.launch {
            homeRepository.resetDuoAlarmWinner(alarmId)
        }
    }

    fun recordAlarmWin(alarmId: String, mode: String) {
        _uiState.update { it.copy(streak = it.streak + 1, wins = it.wins + 1) }
        viewModelScope.launch {
            homeRepository.recordAlarmResult(alarmId, mode, true)
        }
    }

    fun recordAlarmLoss(alarmId: String, mode: String) {
        val previousStreak = _uiState.value.streak
        _uiState.update { it.copy(streak = 0, losses = it.losses + 1) }
        viewModelScope.launch {
            homeRepository.recordAlarmResult(alarmId, mode, false)

            val alarmTime = _uiState.value.nextAlarmTime.ifEmpty { "07:00 AM" }
            val challenge = AlarmState.activeAlarmChallenge.ifEmpty { "Speed Math" }
            val punishmentStr = _activePunishment.value?.let { "${it.punishmentEmoji} ${it.punishmentType}" } ?: "20 pushups 💪"

            val username = _uiState.value.userName.ifEmpty { "user" }
            val genZCaptions = listOf(
                "@$username slept through $alarmTime alarm 😴",
                "Caught sleeping in 4K resolution 💀 (Missed $alarmTime)",
                "Defaulted on wake-up duty! Penalty: $punishmentStr 📢",
                "Streak reset to 0. Absolute tragedy 📉",
                "Alarm rang for 10 minutes straight. Down bad 🚨"
            )
            val randomCaption = genZCaptions.random()

            // Auto-post shame receipt to social penalty wall
            homeRepository.postShameReceipt(
                alarmTime = alarmTime,
                challengeName = challenge,
                streakLost = previousStreak,
                punishmentText = punishmentStr,
                customCaption = randomCaption
            )

            // Assign punishment for Duo/Group mode
            if (mode == "Duo" || mode == "Group") {
                val challengerUsername = AlarmState.activeAlarmPartnerUsername ?: "Partner"
                val currentUser = _uiState.value
                // Look up the challenger's UID from the alarm's partnerUid field
                val alarm = _uiState.value.alarms.find { it.id == alarmId }
                val challengerUid = alarm?.partnerUid ?: ""
                homeRepository.assignPunishment(
                    alarmId = alarmId,
                    loserUid = homeRepository.getCurrentUserUid() ?: "",
                    loserUsername = currentUser.userName,
                    challengerUid = challengerUid,
                    challengerUsername = challengerUsername,
                    mode = mode,
                    challenge = AlarmState.activeAlarmChallenge
                ).onSuccess { punishment ->
                    _activePunishment.value = punishment
                }
            }
        }
    }

    fun broadcastShameReceipt(customCaption: String? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val alarmTime = _uiState.value.nextAlarmTime.ifEmpty { "07:00 AM" }
            val challenge = AlarmState.activeAlarmChallenge.ifEmpty { "Speed Math" }
            val punishmentStr = _activePunishment.value?.let { "${it.punishmentEmoji} ${it.punishmentType}" } ?: "20 pushups 💪"

            homeRepository.postShameReceipt(
                alarmTime = alarmTime,
                challengeName = challenge,
                streakLost = AlarmState.previousStreak,
                punishmentText = punishmentStr,
                customCaption = customCaption
            ).onSuccess {
                onComplete()
            }
        }
    }

    private val _activePunishment = MutableStateFlow<Punishment?>(null)
    val activePunishment: StateFlow<Punishment?> = _activePunishment.asStateFlow()

    fun submitProof(punishmentId: String, proofUrl: String) {
        viewModelScope.launch {
            homeRepository.submitProof(punishmentId, proofUrl).onSuccess {
                _activePunishment.value = _activePunishment.value?.copy(
                    status = "ProofSubmitted",
                    proofUrl = proofUrl,
                    proofSubmittedAt = kotlin.time.Clock.System.now().toEpochMilliseconds()
                )
            }
        }
    }

    fun completePunishment(punishmentId: String) {
        viewModelScope.launch {
            homeRepository.completePunishment(punishmentId).onSuccess {
                _activePunishment.value = null
            }
        }
    }

    fun fetchActivePunishment() {
        viewModelScope.launch {
            val punishment = homeRepository.getActivePunishment()
            _activePunishment.value = punishment
        }
    }

    fun getLeaderboard(mode: String, isGlobal: Boolean): Flow<List<LeaderboardUser>> {
        return homeRepository.getLeaderboard(mode, isGlobal).catch { emit(emptyList()) }
    }

    fun getGroupLeaderboard(groupId: String = "Morning Crew"): Flow<List<GroupMember>> {
        return homeRepository.getGroupLeaderboard(groupId).catch { emit(emptyList()) }
    }

    fun autoSetAlarm() {
        addAlarm(
            hour = 6,
            minute = 30,
            isAm = true,
            days = listOf(0, 1, 2, 3, 4), // Mon-Fri
            mode = "Solo",
            challenge = "Math"
        )
    }

    fun addAlarm(
        hour: Int,
        minute: Int,
        isAm: Boolean,
        days: List<Int>,
        mode: String,
        challenge: String,
        partnerUsername: String? = null,
        bondName: String? = null
    ) {
        viewModelScope.launch {
            val hour24 = if (isAm) {
                if (hour == 12) 0 else hour
            } else {
                if (hour == 12) 12 else hour + 12
            }

            val timeStr = "${hour24.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
            val timestamp = calculateNextOccurrence(hour24, minute, days)
            val selectedSound = _uiState.value.selectedSound

            val alarm = AlarmData(
                id = "",
                time = timeStr,
                label = "Alarm",
                days = days,
                isEnabled = true,
                mode = mode,
                challenge = challenge,
                isGroup = mode == "Group",
                timestamp = timestamp,
                soundUrl = selectedSound?.url,
                soundName = selectedSound?.name ?: "Default",
                soundId = selectedSound?.id,
                partnerUsername = partnerUsername,
                bondName = bondName
            )
            
            homeRepository.addAlarm(alarm)
        }
    }

    // calculateNextOccurrence is now in AlarmUtils.kt (shared with AlarmService)

    fun toggleAlarm(alarmId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            homeRepository.toggleAlarm(alarmId, isEnabled)
            // We'll handle actual scheduling in the observer to keep it in sync with DB
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarmId)
            homeRepository.deleteAlarm(alarmId)
        }
    }

    fun toggleSortOrder() {
        _uiState.update { state ->
            val newOrder = if (state.sortOrder == AlarmSortOrder.TIME) {
                AlarmSortOrder.RECENT
            } else {
                AlarmSortOrder.TIME
            }
            state.copy(
                sortOrder = newOrder,
                alarms = sortAlarmsList(state.alarms, newOrder)
            )
        }
    }

    private fun sortAlarmsList(alarms: List<AlarmData>, order: AlarmSortOrder): List<AlarmData> {
        return when (order) {
            AlarmSortOrder.TIME -> alarms.sortedBy { it.time }
            AlarmSortOrder.RECENT -> alarms.sortedByDescending { it.timestamp }
        }
    }

    fun addHabit(title: String, icon: HabitIconType, frequency: String, reminderTime: String, partnerUsername: String?, bondName: String? = null) {
        viewModelScope.launch {
            val newHabit = Habit(
                id = "",
                title = title,
                iconType = icon,
                isDone = false,
                streak = 0,
                frequency = frequency,
                reminderTime = reminderTime,
                partnerUsername = partnerUsername,
                bondName = bondName
            )
            homeRepository.addHabit(newHabit)
        }
    }

    fun autoSetHabit() {
        viewModelScope.launch {
            val suggestedHabit = Habit(
                id = "",
                title = "Morning Run",
                iconType = HabitIconType.RUN,
                isDone = false,
                streak = 0,
                frequency = "Daily",
                reminderTime = "6:15 AM",
                partnerUsername = null,
                bondName = null
            )
            homeRepository.addHabit(suggestedHabit)
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            homeRepository.deleteHabit(habitId)
        }
    }

    fun updateHabit(id: String, title: String, icon: HabitIconType, frequency: String, reminderTime: String, partnerUsername: String?, bondName: String? = null) {
        viewModelScope.launch {
            val current = _uiState.value.habits.find { it.id == id }
            val finalHabit = current?.copy(
                title = title,
                iconType = icon,
                frequency = frequency,
                reminderTime = reminderTime,
                partnerUsername = partnerUsername,
                bondName = bondName
            ) ?: Habit(
                id = id,
                title = title,
                iconType = icon,
                isDone = false,
                streak = 0,
                frequency = frequency,
                reminderTime = reminderTime,
                partnerUsername = partnerUsername,
                bondName = bondName
            )
            homeRepository.updateHabit(finalHabit)
        }
    }

    private fun isTimestampToday(timestamp: Long): Boolean {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now().toLocalDateTime(timeZone).date
        val alarmDate = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(timeZone).date
        return now == alarmDate
    }

    // ── Social Feed (Real-Time) ────────────────────────────────────────
    fun getSocialFeed(): Flow<List<FeedPost>> {
        return homeRepository.getSocialFeed().catch { emit(emptyList()) }
    }

    fun postToFeed(content: String, badge: String) {
        viewModelScope.launch {
            homeRepository.postToFeed(content, badge)
        }
    }

    // ── Stories (Real-Time) ───────────────────────────────────────────
    fun getStories(): Flow<List<StoryItem>> {
        return homeRepository.getStories().catch { emit(emptyList()) }
    }

    fun postStory(
        caption: String,
        badgeText: String,
        badgeColorHex: String,
        bgStartHex: String,
        bgEndHex: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            homeRepository.postStory(caption, badgeText, badgeColorHex, bgStartHex, bgEndHex)
            onComplete()
        }
    }

    // ── Notifications (Real-Time) ──────────────────────────────────────
    fun getNotifications(): Flow<List<FeedNotification>> {
        return homeRepository.getNotifications()
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            homeRepository.markNotificationRead(notificationId)
        }
    }

    fun loadNotificationPreferences() {
        viewModelScope.launch {
            val prefs = homeRepository.getNotificationPreferences()
            _uiState.update { it.copy(notificationPreferences = prefs) }
        }
    }

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        _uiState.update { it.copy(notificationPreferences = prefs) }
        viewModelScope.launch {
            homeRepository.updateNotificationPreferences(prefs)
        }
    }
}
