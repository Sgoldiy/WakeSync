package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toLocalDateTime
import com.social.wakesync.feature.profile.IosFirestoreBridgeHolder
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Clock

class IosHomeRepository : HomeRepository {
    private val defaults = platform.Foundation.NSUserDefaults.standardUserDefaults()

    // Active punishment stored locally
    private var _activePunishment: Punishment? = null

    private fun bridge() = IosFirestoreBridgeHolder.bridge

    // ── Auth ────────────────────────────────────────────────────────────

    override fun getCurrentUserUid(): String? = bridge()?.getCurrentUserUid()

    // ── Stats (with Firestore sync) ─────────────────────────────────────

    private val _stats = MutableStateFlow(loadStats())

    private fun loadStats(): HomeStats {
        val b = bridge()
        if (b != null) {
            // Try to load from Firestore via bridge
            return HomeStats(streak = 0, wins = 0, losses = 0, rank = "#0") // Will be populated by observer below
        }
        return HomeStats(
            streak = defaults.integerForKey("streak").toInt(),
            wins = defaults.integerForKey("wins").toInt(),
            losses = defaults.integerForKey("losses").toInt(),
            rank = defaults.stringForKey("rank") ?: "#0",
            soloStreak = defaults.integerForKey("soloStreak").toInt(),
            soloWins = defaults.integerForKey("soloWins").toInt(),
            soloLosses = defaults.integerForKey("soloLosses").toInt(),
            duoStreak = defaults.integerForKey("duoStreak").toInt(),
            duoWins = defaults.integerForKey("duoWins").toInt(),
            duoLosses = defaults.integerForKey("duoLosses").toInt(),
            groupStreak = defaults.integerForKey("groupStreak").toInt(),
            groupWins = defaults.integerForKey("groupWins").toInt(),
            groupLosses = defaults.integerForKey("groupLosses").toInt()
        )
    }

    init {
        // Load stats from Firestore if bridge available
        bridge()?.getUserDocument(
            onResult = { serialized ->
                if (serialized != null) {
                    val map = parseSerializedDoc(serialized)
                    val stats = HomeStats(
                        streak = map["streak"]?.toIntOrNull() ?: 0,
                        wins = map["wins"]?.toIntOrNull() ?: 0,
                        losses = map["losses"]?.toIntOrNull() ?: 0,
                        rank = map["rank"] ?: "#0",
                        soloStreak = map["soloAlarmStreak"]?.toIntOrNull() ?: 0,
                        soloWins = map["soloAlarmWins"]?.toIntOrNull() ?: 0,
                        soloLosses = map["soloAlarmLosses"]?.toIntOrNull() ?: 0,
                        duoStreak = map["duoAlarmStreak"]?.toIntOrNull() ?: 0,
                        duoWins = map["duoAlarmWins"]?.toIntOrNull() ?: 0,
                        duoLosses = map["duoAlarmLosses"]?.toIntOrNull() ?: 0,
                        groupStreak = map["groupAlarmStreak"]?.toIntOrNull() ?: 0,
                        groupWins = map["groupAlarmWins"]?.toIntOrNull() ?: 0,
                        groupLosses = map["groupAlarmLosses"]?.toIntOrNull() ?: 0
                    )
                    _stats.value = stats
                    saveStatsToLocal(stats)
                }
            },
            onError = {}
        )
    }

    private fun saveStatsToLocal(stats: HomeStats) {
        defaults.setInteger(stats.streak.toLong(), forKey = "streak")
        defaults.setInteger(stats.wins.toLong(), forKey = "wins")
        defaults.setInteger(stats.losses.toLong(), forKey = "losses")
        defaults.setObject(stats.rank, forKey = "rank")
    }

    private fun saveStats(stats: HomeStats) {
        saveStatsToLocal(stats)
        bridge()?.setUserDocument(
            data = mapOf(
                "streak" to stats.streak,
                "wins" to stats.wins,
                "losses" to stats.losses,
                "soloAlarmStreak" to stats.soloStreak,
                "soloAlarmWins" to stats.soloWins,
                "soloAlarmLosses" to stats.soloLosses,
                "duoAlarmStreak" to stats.duoStreak,
                "duoAlarmWins" to stats.duoWins,
                "duoAlarmLosses" to stats.duoLosses,
                "groupAlarmStreak" to stats.groupStreak,
                "groupAlarmWins" to stats.groupWins,
                "groupAlarmLosses" to stats.groupLosses
            ),
            merge = true,
            onSuccess = {},
            onError = {}
        )
    }

    override fun getStats(): Flow<HomeStats> = _stats

    // ── Alarms (Firestore via bridge) ──────────────────────────────────

    private val _alarms = MutableStateFlow(emptyList<AlarmData>())

    init {
        bridge()?.getAlarms(
            onResult = { serialized ->
                _alarms.value = parseAlarmsFromSerialized(serialized)
            },
            onError = {}
        )
    }

    private fun parseAlarmsFromSerialized(serialized: String): List<AlarmData> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 10) return@mapNotNull null
            AlarmData(
                id = parts[0],
                time = parts[1],
                label = parts[2],
                mode = parts[3],
                challenge = parts[4],
                isEnabled = parts[5] == "true",
                isGroup = parts[6] == "true",
                days = parts[7].split(",").mapNotNull { it.toIntOrNull() },
                timestamp = parts[8].toLongOrNull() ?: 0L,
                soundUrl = parts[9].ifBlank { null },
                soundName = parts.getOrNull(10) ?: "Default",
                partnerUid = parts.getOrNull(11)?.ifBlank { null },
                partnerUsername = parts.getOrNull(12)?.ifBlank { null },
                bondName = parts.getOrNull(13)?.ifBlank { null },
                mathDifficulty = parts.getOrNull(14) ?: "Medium"
            )
        }
    }

    override fun getAlarms(): Flow<List<AlarmData>> = _alarms

    override fun getNextAlarm(): Flow<AlarmData?> = _alarms.map { alarms ->
        alarms.filter { it.isEnabled }.minByOrNull { it.timestamp }
    }

    override suspend fun addAlarm(alarm: AlarmData): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            val data = mapOf(
                "time" to alarm.time,
                "label" to alarm.label,
                "mode" to alarm.mode,
                "challenge" to alarm.challenge,
                "isEnabled" to alarm.isEnabled,
                "isGroup" to alarm.isGroup,
                "days" to alarm.days,
                "timestamp" to alarm.timestamp,
                "soundUrl" to (alarm.soundUrl ?: ""),
                "soundName" to alarm.soundName,
                "partnerUid" to (alarm.partnerUid ?: ""),
                "partnerUsername" to (alarm.partnerUsername ?: ""),
                "bondName" to (alarm.bondName ?: ""),
                "mathDifficulty" to alarm.mathDifficulty
            )
            b.addAlarm(
                data = data,
                onSuccess = { newId ->
                    _alarms.value = _alarms.value + alarm.copy(id = newId)
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun updateAlarm(alarm: AlarmData): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            val data = mapOf(
                "time" to alarm.time,
                "label" to alarm.label,
                "mode" to alarm.mode,
                "challenge" to alarm.challenge,
                "isEnabled" to alarm.isEnabled,
                "isGroup" to alarm.isGroup,
                "days" to alarm.days,
                "timestamp" to alarm.timestamp,
                "soundUrl" to (alarm.soundUrl ?: ""),
                "soundName" to alarm.soundName,
                "partnerUid" to (alarm.partnerUid ?: ""),
                "partnerUsername" to (alarm.partnerUsername ?: ""),
                "bondName" to (alarm.bondName ?: ""),
                "mathDifficulty" to alarm.mathDifficulty
            )
            b.updateAlarm(
                alarmId = alarm.id,
                data = data,
                onSuccess = {
                    _alarms.value = _alarms.value.map { if (it.id == alarm.id) alarm else it }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun toggleAlarm(alarmId: String, isEnabled: Boolean): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.updateAlarm(
                alarmId = alarmId,
                data = mapOf("isEnabled" to isEnabled),
                onSuccess = {
                    _alarms.value = _alarms.value.map { if (it.id == alarmId) it.copy(isEnabled = isEnabled) else it }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.deleteAlarm(
                alarmId = alarmId,
                onSuccess = {
                    _alarms.value = _alarms.value.filter { it.id != alarmId }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Habits (Firestore via bridge) ──────────────────────────────────

    private val _habits = MutableStateFlow(emptyList<Habit>())

    init {
        bridge()?.getHabits(
            onResult = { serialized ->
                _habits.value = parseHabitsFromSerialized(serialized)
            },
            onError = {}
        )
    }

    private fun parseHabitsFromSerialized(serialized: String): List<Habit> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 8) return@mapNotNull null
            Habit(
                id = parts[0],
                title = parts[1],
                iconType = try { HabitIconType.valueOf(parts[2]) } catch (_: Exception) { HabitIconType.RUN },
                isDone = parts[3] == "true",
                streak = parts[4].toIntOrNull() ?: 0,
                frequency = parts[5],
                reminderTime = parts[6],
                partnerUsername = parts[7].ifBlank { null },
                bondName = parts.getOrNull(8)?.ifBlank { null }
            )
        }
    }

    override fun getHabits(): Flow<List<Habit>> = _habits

    override suspend fun addHabit(habit: Habit): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            val data = mapOf(
                "title" to habit.title,
                "iconType" to habit.iconType.name,
                "isDone" to habit.isDone,
                "streak" to habit.streak,
                "frequency" to habit.frequency,
                "reminderTime" to habit.reminderTime,
                "partnerUsername" to (habit.partnerUsername ?: ""),
                "bondName" to (habit.bondName ?: "")
            )
            b.addHabit(
                data = data,
                onSuccess = { newId ->
                    _habits.value = _habits.value + habit.copy(id = newId)
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.deleteHabit(
                habitId = habitId,
                onSuccess = {
                    _habits.value = _habits.value.filter { it.id != habitId }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun updateHabit(habit: Habit): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            val data = mapOf(
                "title" to habit.title,
                "iconType" to habit.iconType.name,
                "isDone" to habit.isDone,
                "streak" to habit.streak,
                "frequency" to habit.frequency,
                "reminderTime" to habit.reminderTime,
                "partnerUsername" to (habit.partnerUsername ?: ""),
                "bondName" to (habit.bondName ?: "")
            )
            b.updateHabit(
                habitId = habit.id,
                data = data,
                onSuccess = {
                    _habits.value = _habits.value.map { if (it.id == habit.id) habit else it }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun toggleHabit(habitId: String, isDone: Boolean): Result<Unit> {
        val habit = _habits.value.find { it.id == habitId }
        if (habit?.isDone == true) {
            return Result.success(Unit)
        }
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        val newStreak = (habit?.streak ?: 0) + 1
        _stats.value = _stats.value.copy(
            habitStreak = _stats.value.habitStreak + 1,
            habitWins = _stats.value.habitWins + 1
        )
        return suspendCancellableCoroutine { continuation ->
            b.updateHabit(
                habitId = habitId,
                data = mapOf("isDone" to true, "streak" to newStreak),
                onSuccess = {
                    _habits.value = _habits.value.map { if (it.id == habitId) it.copy(isDone = true, streak = newStreak) else it }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Sounds ──────────────────────────────────────────────────────────

    private val _sounds = MutableStateFlow(
        listOf(
            SoundMetadata("default", "Default", "", "Solo"),
            SoundMetadata("neon_pulse", "Neon Pulse", "", "Solo"),
            SoundMetadata("battle_horn", "Battle Horn", "", "Battle")
        )
    )

    override fun getSoundCatalog(): Flow<List<SoundMetadata>> = _sounds

    override suspend fun seedSoundCatalog(): Result<Unit> = Result.success(Unit)

    // ── Friends (real Firestore query) ──────────────────────────────────

    private val _friends = MutableStateFlow(emptyList<Friend>())

    init {
        // Load friends from Firestore (top users sorted by streak)
        bridge()?.getLeaderboard(
            sortField = "streak",
            limit = 10,
            onResult = { serialized ->
                val friends = parseLeaderboardFromSerialized(serialized).map { lb ->
                    Friend(
                        id = lb.username,
                        name = lb.username,
                        avatar = lb.avatar,
                        streak = lb.streak,
                        status = if (lb.streak > 10) FriendStatus.ACTIVE else FriendStatus.NEW
                    )
                }.filter { it.name != getCurrentUserUid() }
                _friends.value = friends
            },
            onError = {}
        )
    }

    override fun getFriends(): Flow<List<Friend>> = _friends

    override suspend fun searchUsersByUsername(query: String): Result<List<Friend>> {
        val b = bridge() ?: return Result.success(emptyList())
        return suspendCancellableCoroutine { continuation ->
            b.queryUsers(
                field = "username",
                value = query,
                onResult = { result ->
                    val friends = result.lines().filter { it.isNotBlank() }.map { line ->
                        val parts = line.split("|")
                        Friend(
                            id = parts.getOrElse(0) { "" },
                            name = parts.getOrElse(1) { "" },
                            avatar = parts.getOrElse(2) { "👤" },
                            streak = parts.getOrElse(3) { "0" }.toIntOrNull() ?: 0,
                            status = FriendStatus.NEW
                        )
                    }
                    if (continuation.isActive) continuation.resume(Result.success(friends))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Duo Alarm Sync (real Firestore) ─────────────────────────────────

    private val _duoAlarmWinner = MutableStateFlow<String?>(null)
    private var duoAlarmListenerSetup = false

    override fun listenToDuoAlarm(alarmId: String): Flow<String?> {
        if (!duoAlarmListenerSetup) {
            duoAlarmListenerSetup = true
            bridge()?.listenToDuoAlarm(
                alarmId = alarmId,
                onResult = { winnerUid -> _duoAlarmWinner.value = winnerUid },
                onError = {}
            )
        }
        return _duoAlarmWinner
    }

    override suspend fun setDuoAlarmWinner(alarmId: String, winnerUid: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.setDuoAlarmWinner(
                alarmId = alarmId,
                winnerUid = winnerUid,
                onSuccess = {
                    _duoAlarmWinner.value = winnerUid
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun resetDuoAlarmWinner(alarmId: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.setDuoAlarmWinner(
                alarmId = alarmId,
                winnerUid = "",
                onSuccess = {
                    _duoAlarmWinner.value = null
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Record Alarm Result (with Firestore sync) ───────────────────────

    override suspend fun recordAlarmResult(alarmId: String, mode: String, isWin: Boolean): Result<Unit> {
        val current = _stats.value
        val updated = when (mode) {
            "Solo" -> {
                val newStreak = if (isWin) current.soloStreak + 1 else 0
                current.copy(
                    soloStreak = newStreak,
                    soloWins = if (isWin) current.soloWins + 1 else current.soloWins,
                    soloLosses = if (!isWin) current.soloLosses + 1 else current.soloLosses,
                    streak = newStreak,
                    wins = current.wins + (if (isWin) 1 else 0),
                    losses = current.losses + (if (!isWin) 1 else 0)
                )
            }
            "Duo" -> {
                val newStreak = if (isWin) current.duoStreak + 1 else 0
                current.copy(
                    duoStreak = newStreak,
                    duoWins = if (isWin) current.duoWins + 1 else current.duoWins,
                    duoLosses = if (!isWin) current.duoLosses + 1 else current.duoLosses,
                    streak = newStreak,
                    wins = current.wins + (if (isWin) 1 else 0),
                    losses = current.losses + (if (!isWin) 1 else 0)
                )
            }
            else -> {
                val newStreak = if (isWin) current.groupStreak + 1 else 0
                current.copy(
                    groupStreak = newStreak,
                    groupWins = if (isWin) current.groupWins + 1 else current.groupWins,
                    groupLosses = if (!isWin) current.groupLosses + 1 else current.groupLosses,
                    streak = newStreak,
                    wins = current.wins + (if (isWin) 1 else 0),
                    losses = current.losses + (if (!isWin) 1 else 0)
                )
            }
        }
        _stats.value = updated
        saveStats(updated)
        return Result.success(Unit)
    }

    // ── Punishment ──────────────────────────────────────────────────────

    private fun selectPunishment(challenge: String, mode: String): Pair<String, String> {
        return when (challenge) {
            "Math" -> "🧮" to "20 math problems"
            "Typing" -> "⌨️" to "Type 100 WPM for 2 minutes"
            "Memory" -> "🧠" to "Recite 20 items from memory"
            "Riddle" -> "🧩" to "Solve 3 hard riddles"
            "Word" -> "📝" to "Write a 200-word essay"
            "Photo" -> "📸" to "Take a sunrise photo + post it"
            "Voice" -> "🎙️" to "Record a 60s motivational voice note"
            "Story" -> "📖" to "Post your fail story to your feed"
            "Sacrifice" -> "💰" to "Donate $5 to the bond fund"
            else -> "💪" to if (mode == "Group") "15 burpees" else "20 pushups"
        }
    }

    override suspend fun assignPunishment(
        alarmId: String,
        loserUid: String,
        loserUsername: String,
        challengerUid: String,
        challengerUsername: String,
        mode: String,
        challenge: String
    ): Result<Punishment> {
        val now = Clock.System.now().toEpochMilliseconds()
        val dueAt = now + (2 * 60 * 60 * 1000L)
        val (emoji, text) = selectPunishment(challenge, mode)
        val punishmentId = "pun_${loserUid}_${now}"
        val punishment = Punishment(
            id = punishmentId,
            loserUid = loserUid,
            loserUsername = loserUsername,
            challengerUid = challengerUid,
            challengerUsername = challengerUsername,
            alarmId = alarmId,
            mode = mode,
            challenge = challenge,
            punishmentType = challenge,
            punishmentEmoji = emoji,
            punishmentDetail = "Photo proof required · Due ${formatTime(dueAt)}",
            status = "Assigned",
            createdAt = now,
            dueAt = dueAt
        )
        _activePunishment = punishment

        // Write to Firestore via bridge
        bridge()?.writePunishment(
            punishmentId = punishmentId,
            data = mapOf(
                "id" to punishmentId,
                "loserUid" to loserUid,
                "loserUsername" to loserUsername,
                "challengerUid" to challengerUid,
                "challengerUsername" to challengerUsername,
                "alarmId" to alarmId,
                "mode" to mode,
                "challenge" to challenge,
                "punishmentType" to challenge,
                "punishmentEmoji" to emoji,
                "punishmentDetail" to punishment.punishmentDetail,
                "status" to "Assigned",
                "createdAt" to now,
                "dueAt" to dueAt
            ),
            onSuccess = {},
            onError = {}
        )

        return Result.success(punishment)
    }

    override suspend fun getActivePunishment(): Punishment? = _activePunishment

    override suspend fun submitProof(punishmentId: String, proofUrl: String): Result<Unit> {
        _activePunishment = _activePunishment?.copy(
            status = "ProofSubmitted",
            proofUrl = proofUrl,
            proofSubmittedAt = Clock.System.now().toEpochMilliseconds()
        )
        return Result.success(Unit)
    }

    override suspend fun completePunishment(punishmentId: String): Result<Unit> {
        _activePunishment = _activePunishment?.copy(
            status = "Completed",
            completedAt = Clock.System.now().toEpochMilliseconds()
        )
        return Result.success(Unit)
    }

    // ── Leaderboard (real Firestore query) ───────────────────────────────

    override fun getLeaderboard(mode: String, isGlobal: Boolean): Flow<List<LeaderboardUser>> {
        val sortField = when (mode) {
            "Duo" -> "duoAlarmWins"
            "Group" -> "groupAlarmWins"
            else -> "soloAlarmWins"
        }
        val result = MutableStateFlow(emptyList<LeaderboardUser>())
        bridge()?.getLeaderboard(
            sortField = sortField,
            limit = if (isGlobal) 50 else 10,
            onResult = { serialized -> result.value = parseLeaderboardFromSerialized(serialized) },
            onError = {}
        )
        return result
    }

    private fun parseLeaderboardFromSerialized(serialized: String): List<LeaderboardUser> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 7) return@mapNotNull null
            LeaderboardUser(
                rank = parts[0].toIntOrNull() ?: 0,
                username = parts[1],
                avatar = parts[2],
                score = parts[3].toIntOrNull() ?: 0,
                streak = parts[4].toIntOrNull() ?: 0,
                isCurrentUser = parts[5] == "1",
                isRedLoss = parts[6] == "1"
            )
        }
    }

    override fun getGroupLeaderboard(groupId: String): Flow<List<GroupMember>> {
        val result = MutableStateFlow(emptyList<GroupMember>())
        bridge()?.getLeaderboard(
            sortField = "groupAlarmWins",
            limit = 20,
            onResult = { serialized ->
                result.value = parseLeaderboardFromSerialized(serialized).map { lb ->
                    GroupMember(
                        rank = lb.rank,
                        username = lb.username,
                        avatar = lb.avatar,
                        points = lb.score,
                        wins = lb.score / 100,
                        isCurrentUser = lb.isCurrentUser,
                        isRedHighlight = lb.isRedLoss
                    )
                }
            },
            onError = {}
        )
        return result
    }

    // ── Social Feed (real Firestore) ─────────────────────────────────────

    private val _feedPosts = MutableStateFlow(emptyList<FeedPost>())

    init {
        bridge()?.getFeedPosts(
            limit = 50,
            onResult = { serialized -> _feedPosts.value = parseFeedFromSerialized(serialized) },
            onError = {}
        )
    }

    private fun parseFeedFromSerialized(serialized: String): List<FeedPost> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 10) return@mapNotNull null
            val reactionsMap = mutableMapOf<String, Int>()
            if (parts[9].isNotBlank()) {
                parts[9].split(",").forEach { pair ->
                    val kv = pair.split(":")
                    if (kv.size == 2) reactionsMap[kv[0]] = kv[1].toIntOrNull() ?: 0
                }
            }
            FeedPost(
                id = parts[0],
                userId = parts[1],
                username = parts[2],
                avatar = parts[3],
                content = parts[4],
                badgeText = parts[5],
                badgeColorHex = parts[6],
                streak = parts[7].toIntOrNull() ?: 0,
                createdAt = parts[8].toLongOrNull() ?: 0L,
                reactions = reactionsMap
            )
        }
    }

    override fun getSocialFeed(): Flow<List<FeedPost>> = _feedPosts

    override suspend fun postToFeed(content: String, badge: String): Result<Unit> {
        val now = Clock.System.now().toEpochMilliseconds()
        val badgeColorHex = when (badge) {
            "win" -> "#22C55E"
            "streak" -> "#FFD23D"
            "loss" -> "#FF3D71"
            else -> "#00E0FF"
        }
        val uid = getCurrentUserUid() ?: ""
        val newPost = FeedPost(
            id = "post_$now",
            userId = uid,
            username = "user",
            avatar = "👤",
            content = content,
            badgeText = badge,
            badgeColorHex = badgeColorHex,
            streak = _stats.value.streak,
            createdAt = now,
            reactions = emptyMap()
        )
        _feedPosts.value = listOf(newPost) + _feedPosts.value

        bridge()?.addFeedPost(
            data = mapOf(
                "userId" to uid,
                "username" to "user",
                "avatar" to "👤",
                "content" to content,
                "badgeText" to badge,
                "badgeColorHex" to badgeColorHex,
                "streak" to _stats.value.streak,
                "createdAt" to now,
                "reactions" to emptyMap<String, Int>()
            ),
            onSuccess = {},
            onError = {}
        )
        return Result.success(Unit)
    }

    // ── Notifications (real Firestore) ───────────────────────────────────

    private val _notifications = MutableStateFlow(emptyList<FeedNotification>())

    init {
        bridge()?.getNotifications(
            onResult = { serialized -> _notifications.value = parseNotificationsFromSerialized(serialized) },
            onError = {}
        )
    }

    private fun parseNotificationsFromSerialized(serialized: String): List<FeedNotification> {
        if (serialized.isBlank()) return emptyList()
        return serialized.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("|")
            if (parts.size < 8) return@mapNotNull null
            FeedNotification(
                id = parts[0],
                type = parts[1],
                message = parts[2],
                fromUsername = parts[3],
                fromUid = parts[4],
                punishmentId = parts[7].ifBlank { null },
                createdAt = parts[5].toLongOrNull() ?: 0L,
                read = parts[6] == "true"
            )
        }
    }

    override fun getNotifications(): Flow<List<FeedNotification>> = _notifications

    private val _stories = MutableStateFlow(emptyList<StoryItem>())

    override fun getStories(): Flow<List<StoryItem>> = _stories

    override suspend fun postStory(
        caption: String,
        badgeText: String,
        badgeColorHex: String,
        bgStartHex: String,
        bgEndHex: String
    ): Result<Unit> {
        val now = Clock.System.now().toEpochMilliseconds()
        val uid = getCurrentUserUid() ?: ""
        val newStory = StoryItem(
            id = "story_$now",
            username = "You",
            avatar = "👤",
            borderColor = androidx.compose.ui.graphics.Color(0xFF00FF94),
            isUser = true,
            userId = uid,
            caption = caption,
            badgeText = badgeText,
            badgeColorHex = badgeColorHex,
            gradientStartHex = bgStartHex,
            gradientEndHex = bgEndHex,
            timestamp = now,
            hasActiveStory = true
        )
        _stories.value = listOf(newStory) + _stories.value
        return Result.success(Unit)
    }

    override suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.markNotificationRead(
                notificationId = notificationId,
                onSuccess = {
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(read = true) else it
                    }
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Notification Preferences ───────────────────────────────────────

    override suspend fun getNotificationPreferences(): NotificationPreferences {
        return suspendCancellableCoroutine { continuation ->
            bridge()?.getUserDocument(
                onResult = { serialized ->
                    val prefs = if (serialized != null) {
                        val map = parseSerializedDoc(serialized)
                        NotificationPreferences(
                            alarmSoundsEnabled = map["notifAlarmSounds"]?.toBooleanStrictOrNull() ?: true,
                            punishmentSoundsEnabled = map["notifPunishmentSounds"]?.toBooleanStrictOrNull() ?: true,
                            socialSoundsEnabled = map["notifSocialSounds"]?.toBooleanStrictOrNull() ?: true,
                            vibrationEnabled = map["notifVibration"]?.toBooleanStrictOrNull() ?: true,
                            soundVolume = map["notifSoundVolume"]?.toFloatOrNull() ?: 1.0f,
                            quietHoursStart = map["notifQuietHoursStart"]?.toIntOrNull() ?: -1,
                            quietHoursEnd = map["notifQuietHoursEnd"]?.toIntOrNull() ?: -1
                        )
                    } else {
                        NotificationPreferences()
                    }
                    if (continuation.isActive) continuation.resume(prefs)
                },
                onError = { _ ->
                    if (continuation.isActive) continuation.resume(NotificationPreferences())
                }
            )
        }
    }

    override suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.setUserDocument(
                data = mapOf(
                    "notifAlarmSounds" to prefs.alarmSoundsEnabled,
                    "notifPunishmentSounds" to prefs.punishmentSoundsEnabled,
                    "notifSocialSounds" to prefs.socialSoundsEnabled,
                    "notifVibration" to prefs.vibrationEnabled,
                    "notifSoundVolume" to prefs.soundVolume.toDouble(),
                    "notifQuietHoursStart" to prefs.quietHoursStart.toLong(),
                    "notifQuietHoursEnd" to prefs.quietHoursEnd.toLong()
                ),
                merge = true,
                onSuccess = {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Push Notifications ─────────────────────────────────────────────

    override suspend fun registerDeviceToken(token: String, platform: String): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.registerDeviceToken(
                token = token,
                platform = platform,
                onSuccess = {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun sendPushNotification(
        toUid: String,
        title: String,
        body: String,
        type: String,
        data: Map<String, String>
    ): Result<Unit> {
        val b = bridge() ?: return Result.failure(Exception("Bridge not available"))
        return suspendCancellableCoroutine { continuation ->
            b.sendPushNotification(
                toUid = toUid,
                title = title,
                body = body,
                type = type,
                data = data,
                onSuccess = {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun formatTime(timestamp: Long): String {
        val instant = kotlin.time.Instant.fromEpochMilliseconds(timestamp)
        val datetime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        val hour = datetime.hour
        val minute = datetime.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "$displayHour:${minute.toString().padStart(2, '0')} $amPm"
    }

    /**
     * Parse a Firestore document serialized as "key=value" lines into a map.
     */
    private fun parseSerializedDoc(serialized: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        serialized.lines().forEach { line ->
            val eqIdx = line.indexOf('=')
            if (eqIdx > 0) {
                val key = line.substring(0, eqIdx)
                val value = line.substring(eqIdx + 1)
                map[key] = value
            }
        }
        return map
    }
}

actual fun getHomeRepository(): HomeRepository = IosHomeRepository()
