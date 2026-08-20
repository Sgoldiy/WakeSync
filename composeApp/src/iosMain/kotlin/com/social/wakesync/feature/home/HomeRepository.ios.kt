package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class IosHomeRepository : HomeRepository {
    private val _alarms = MutableStateFlow<List<AlarmData>>(emptyList())
    private val _habits = MutableStateFlow<List<Habit>>(
        listOf(
            Habit("1", "Morning Run", HabitIconType.RUN, true),
            Habit("2", "Cold Shower", HabitIconType.SHOWER, true),
            Habit("3", "No Phone 1hr", HabitIconType.NO_PHONE, true),
            Habit("4", "Read 20 mins", HabitIconType.READING, false),
            Habit("5", "Stretch", HabitIconType.STRETCH, false)
        )
    )
    private val _stats = MutableStateFlow(HomeStats(23, 18, 5, "#4"))
    private val _friends = MutableStateFlow(
        listOf(
            Friend("1", "maya.rises", "🦁", 41, FriendStatus.ACTIVE),
            Friend("2", "5amclub..", "🐺", 89, FriendStatus.INACTIVE),
            Friend("3", "nocturna..", "🦊", 7, FriendStatus.FAILED),
            Friend("4", "grind.rio", "🐻", 15, FriendStatus.NEW)
        )
    )
    private val _sounds = MutableStateFlow(
        listOf(
            SoundMetadata("neon_pulse", "Neon Pulse", "", "Solo"),
            SoundMetadata("orbital_drift", "Orbital Drift", "", "Chill"),
            SoundMetadata("battle_horn", "Battle Horn", "", "Battle")
        )
    )

    override fun getHabits(): Flow<List<Habit>> = _habits
    
    override fun getStats(): Flow<HomeStats> = _stats
    
    override fun getFriends(): Flow<List<Friend>> = _friends
    
    override fun getAlarms(): Flow<List<AlarmData>> = _alarms
    
    override fun getNextAlarm(): Flow<AlarmData?> = _alarms.map { list ->
        list.filter { it.isEnabled }.minByOrNull { it.timestamp }
    }

    override suspend fun toggleHabit(habitId: String, isDone: Boolean): Result<Unit> {
        _habits.value = _habits.value.map {
            if (it.id == habitId) {
                val newStreak = if (isDone) it.streak + 1 else maxOf(0, it.streak - 1)
                it.copy(isDone = isDone, streak = newStreak)
            } else it
        }
        return Result.success(Unit)
    }

    override suspend fun addAlarm(alarm: AlarmData): Result<Unit> {
        val alarmWithId = alarm.copy(id = if (alarm.id.isBlank()) "alarm_${getCurrentTimeMillis()}" else alarm.id)
        _alarms.value = _alarms.value + alarmWithId
        return Result.success(Unit)
    }

    override suspend fun updateAlarm(alarm: AlarmData): Result<Unit> {
        _alarms.value = _alarms.value.map {
            if (it.id == alarm.id) alarm else it
        }
        return Result.success(Unit)
    }

    override suspend fun toggleAlarm(alarmId: String, isEnabled: Boolean): Result<Unit> {
        _alarms.value = _alarms.value.map {
            if (it.id == alarmId) it.copy(isEnabled = isEnabled) else it
        }
        return Result.success(Unit)
    }

    override suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        _alarms.value = _alarms.value.filter { it.id != alarmId }
        return Result.success(Unit)
    }

    override fun getSoundCatalog(): Flow<List<SoundMetadata>> = _sounds

    override suspend fun seedSoundCatalog(): Result<Unit> = Result.success(Unit)

    override suspend fun searchUsersByUsername(query: String): Result<List<Friend>> {
        val filtered = _friends.value.filter { it.name.contains(query, ignoreCase = true) }
        return Result.success(filtered)
    }

    override fun getCurrentUserUid(): String? = "ios_user_uid"

    override fun listenToDuoAlarm(alarmId: String): Flow<String?> = MutableStateFlow(null)

    override suspend fun setDuoAlarmWinner(alarmId: String, winnerUid: String): Result<Unit> = Result.success(Unit)

    override suspend fun resetDuoAlarmWinner(alarmId: String): Result<Unit> = Result.success(Unit)

    override suspend fun addHabit(habit: Habit): Result<Unit> {
        val habitWithId = habit.copy(id = if (habit.id.isBlank()) "habit_${getCurrentTimeMillis()}" else habit.id)
        _habits.value = _habits.value + habitWithId
        return Result.success(Unit)
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        _habits.value = _habits.value.filter { it.id != habitId }
        return Result.success(Unit)
    }

    override suspend fun updateHabit(habit: Habit): Result<Unit> {
        _habits.value = _habits.value.map {
            if (it.id == habit.id) habit else it
        }
        return Result.success(Unit)
    }
}

actual fun getHomeRepository(): HomeRepository = IosHomeRepository()
