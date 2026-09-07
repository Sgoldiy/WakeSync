package com.social.wakesync.feature.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.social.wakesync.FIRESTORE_DATABASE_ID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class AndroidHomeRepository : HomeRepository {
    private val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
    private val auth = FirebaseAuth.getInstance()

    override fun getHabits(): Flow<List<Habit>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val habits = snapshot?.documents?.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val isDone = doc.getBoolean("isDone") ?: false
                    val iconTypeStr = doc.getString("iconType") ?: "RUN"
                    val iconType = try { HabitIconType.valueOf(iconTypeStr) } catch (e: Exception) { HabitIconType.RUN }
                    val streak = doc.getLong("streak")?.toInt() ?: 0
                    val frequency = doc.getString("frequency") ?: "Daily"
                    val reminderTime = doc.getString("reminderTime") ?: "6:15 AM"
                    val partnerUsername = doc.getString("partnerUsername")
                    val bondName = doc.getString("bondName")
                    
                    Habit(doc.id, title, iconType, isDone, streak, frequency, reminderTime, partnerUsername, bondName)
                } ?: emptyList()
                
                trySend(habits)
            }

        awaitClose { subscription.remove() }
    }

    override fun getStats(): Flow<HomeStats> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(HomeStats(0, 0, 0, "-"))
            awaitClose { }
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(HomeStats(0, 0, 0, "-"))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val streak = snapshot.getLong("streak")?.toInt() ?: 0
                    val wins = snapshot.getLong("wins")?.toInt() ?: 0
                    val losses = snapshot.getLong("losses")?.toInt() ?: 0
                    val rank = snapshot.getString("rank") ?: "-"
                    
                    val soloStreak = snapshot.getLong("soloAlarmStreak")?.toInt() ?: 0
                    val soloWins = snapshot.getLong("soloAlarmWins")?.toInt() ?: 0
                    val soloLosses = snapshot.getLong("soloAlarmLosses")?.toInt() ?: 0
                    
                    val duoStreak = snapshot.getLong("duoAlarmStreak")?.toInt() ?: 0
                    val duoWins = snapshot.getLong("duoAlarmWins")?.toInt() ?: 0
                    val duoLosses = snapshot.getLong("duoAlarmLosses")?.toInt() ?: 0
                    
                    val groupStreak = snapshot.getLong("groupAlarmStreak")?.toInt() ?: 0
                    val groupWins = snapshot.getLong("groupAlarmWins")?.toInt() ?: 0
                    val groupLosses = snapshot.getLong("groupAlarmLosses")?.toInt() ?: 0
                    
                    trySend(HomeStats(
                        streak, wins, losses, rank,
                        soloStreak, soloWins, soloLosses,
                        duoStreak, duoWins, duoLosses,
                        groupStreak, groupWins, groupLosses
                    ))
                }
            }

        awaitClose { subscription.remove() }
    }

    override fun getFriends(): Flow<List<Friend>> = callbackFlow {
        val subscription = db.collection("users")
            .orderBy("streak", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val currentUserId = auth.currentUser?.uid
                val friends = snapshot?.documents?.filter { it.id != currentUserId }?.mapNotNull { doc ->
                    val name = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: doc.getString("avatarEmoji") ?: "👤"
                    val streak = doc.getLong("streak")?.toInt() ?: 0
                    val status = FriendStatus.ACTIVE 
                    
                    Friend(doc.id, name, avatar, streak, status)
                } ?: emptyList()

                trySend(friends)
            }

        awaitClose { subscription.remove() }
    }

    override fun getAlarms(): Flow<List<AlarmData>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .collection("alarms")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val alarms = snapshot?.documents?.mapNotNull { doc ->
                    AlarmData(
                        id = doc.id,
                        time = doc.getString("time") ?: "00:00",
                        label = doc.getString("label") ?: "Alarm",
                        days = (doc.get("days") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList(),
                        isEnabled = doc.getBoolean("isEnabled") ?: true,
                        mode = doc.getString("mode") ?: "Solo",
                        challenge = doc.getString("challenge") ?: "Math",
                        isGroup = doc.getBoolean("isGroup") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        soundUrl = doc.getString("soundUrl"),
                        soundName = doc.getString("soundName") ?: "Default",
                        soundId = doc.getString("soundId"),
                        partnerUid = doc.getString("partnerUid"),
                        partnerUsername = doc.getString("partnerUsername"),
                        mathDifficulty = doc.getString("mathDifficulty") ?: "Medium",
                        bondName = doc.getString("bondName")
                    )
                } ?: emptyList()
                trySend(alarms)
            }

        awaitClose { subscription.remove() }
    }

    override fun getNextAlarm(): Flow<AlarmData?> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .collection("alarms")
            .whereEqualTo("isEnabled", true)
            .whereGreaterThan("timestamp", System.currentTimeMillis())
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }

                val alarm = snapshot?.documents?.firstOrNull()?.let { doc ->
                    AlarmData(
                        id = doc.id,
                        time = doc.getString("time") ?: "00:00",
                        label = doc.getString("label") ?: "Alarm",
                        days = (doc.get("days") as? List<*>)?.mapNotNull { (it as? Long)?.toInt() } ?: emptyList(),
                        isEnabled = doc.getBoolean("isEnabled") ?: true,
                        mode = doc.getString("mode") ?: "Solo",
                        challenge = doc.getString("challenge") ?: "Math",
                        isGroup = doc.getBoolean("isGroup") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        soundUrl = doc.getString("soundUrl"),
                        soundName = doc.getString("soundName") ?: "Default",
                        soundId = doc.getString("soundId"),
                        partnerUid = doc.getString("partnerUid"),
                        partnerUsername = doc.getString("partnerUsername"),
                        mathDifficulty = doc.getString("mathDifficulty") ?: "Medium",
                        bondName = doc.getString("bondName")
                    )
                }
                trySend(alarm)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun toggleHabit(habitId: String, isDone: Boolean): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val habitRef = db.collection("users")
                .document(user.uid)
                .collection("habits")
                .document(habitId)
                
            val snapshot = habitRef.get().await()
            if (!snapshot.exists()) return Result.failure(Exception("Habit not found"))
            
            val partnerUsername = snapshot.getString("partnerUsername") ?: ""
            val bondName = snapshot.getString("bondName") ?: ""
            val currentStreak = snapshot.getLong("streak")?.toInt() ?: 0
            
            if (partnerUsername.isBlank()) {
                // Solo Habit: standard increment/reset
                val newStreak = if (isDone) currentStreak + 1 else maxOf(0, currentStreak - 1)
                habitRef.update(
                    "isDone", isDone,
                    "streak", newStreak
                ).await()
            } else {
                // Duo/Group Habit:
                // 1. Update current user's isDone first
                habitRef.update("isDone", isDone).await()
                
                // 2. Look up all participants (creator + partners)
                val currentUserDoc = db.collection("users").document(user.uid).get().await()
                val currentUsername = currentUserDoc.getString("username") ?: ""
                val allUsernames = partnerUsername.split(",").map { it.trim() } + currentUsername
                
                val usersQuery = db.collection("users")
                    .whereIn("username", allUsernames)
                    .get()
                    .await()
                
                val uids = usersQuery.documents.map { it.id }
                
                // 3. Fetch all participants' habit documents
                val habitDocs = uids.map { uid ->
                    db.collection("users").document(uid).collection("habits").document(habitId).get().await()
                }
                
                val allCompleted = habitDocs.all { doc ->
                    doc.exists() && (doc.getBoolean("isDone") ?: false)
                }
                
                // 4. Update streaks for everyone
                if (allCompleted) {
                    // Everyone is done! Increment streak for everyone
                    uids.forEach { uid ->
                        val ref = db.collection("users").document(uid).collection("habits").document(habitId)
                        val s = db.collection("users").document(uid).collection("habits").document(habitId).get().await().getLong("streak")?.toInt() ?: 0
                        ref.update("streak", s + 1).await()
                    }
                } else if (!isDone) {
                    // If we just unmarked it, we break/reset the shared streak for everyone
                    uids.forEach { uid ->
                        db.collection("users")
                            .document(uid)
                            .collection("habits")
                            .document(habitId)
                            .update("streak", 0)
                            .await()
                    }
                }

                // 5. Update centralized bond stats
                if (bondName.isNotBlank()) {
                    val bondId = "bond_${(allUsernames.sorted().joinToString(",") + "_" + bondName).hashCode()}"
                    val bondRef = db.collection("bonds").document(bondId)
                    
                    if (allCompleted) {
                        db.runTransaction { transaction ->
                            val bondSnap = transaction.get(bondRef)
                            if (bondSnap.exists()) {
                                val streak = bondSnap.getLong("habitStreak")?.toInt() ?: 0
                                val wins = bondSnap.getLong("habitWins")?.toInt() ?: 0
                                transaction.update(bondRef, "habitStreak", streak + 1, "habitWins", wins + 1)
                            }
                        }.await()
                    } else if (!isDone) {
                        db.runTransaction { transaction ->
                            val bondSnap = transaction.get(bondRef)
                            if (bondSnap.exists()) {
                                val losses = bondSnap.getLong("habitLosses")?.toInt() ?: 0
                                transaction.update(bondRef, "habitStreak", 0, "habitLosses", losses + 1)
                            }
                        }.await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAlarm(alarm: AlarmData): Result<Unit> {
        return try {
            val user = auth.currentUser
            val alarmWithId = alarm.copy(id = if (alarm.id.isBlank()) "alarm_${System.currentTimeMillis()}" else alarm.id)

            if (alarm.mode == "Solo") {
                // Solo alarms save directly to local user collection for 100% offline & local priority
                if (user != null) {
                    db.collection("users")
                        .document(user.uid)
                        .collection("alarms")
                        .document(alarmWithId.id)
                        .set(alarmWithId)
                        .await()
                }
            } else {
                // Duo and Group alarms sync directly to Firebase Firestore across shared collections
                if (user != null) {
                    val addedDoc = db.collection("users")
                        .document(user.uid)
                        .collection("alarms")
                        .add(alarm.copy(id = ""))
                        .await()

                    val duoData = hashMapOf(
                        "createdBy" to user.uid,
                        "creatorName" to (user.displayName ?: "Partner"),
                        "partnerUsername" to (alarm.partnerUsername ?: ""),
                        "participantUids" to listOf(user.uid),
                        "time" to alarm.time,
                        "label" to alarm.label,
                        "days" to alarm.days,
                        "mode" to alarm.mode,
                        "challenge" to alarm.challenge,
                        "mathDifficulty" to alarm.mathDifficulty,
                        "timestamp" to alarm.timestamp,
                        "isEnabled" to true,
                        "soundUrl" to (alarm.soundUrl ?: ""),
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "bondName" to (alarm.bondName ?: "")
                    )
                    
                    db.collection("duo_alarms")
                        .document(addedDoc.id)
                        .set(duoData)
                        .await()

                    if (!alarm.bondName.isNullOrBlank()) {
                        checkAndCreateBond(alarm.bondName, alarm.mode, user.uid, alarm.partnerUsername)
                    }

                    // Cross-Device Cross-Platform Sync: Find partners in Firebase by username and push alarm to their accounts
                    try {
                        val targetUsername = alarm.partnerUsername
                        val partnerQuery = if (!targetUsername.isNullOrBlank()) {
                            val usernamesList = targetUsername.split(",")
                            db.collection("users")
                                .whereIn("username", usernamesList)
                                .get()
                                .await()
                        } else {
                            db.collection("users").get().await()
                        }

                        val partnerDocs = partnerQuery.documents.filter { it.id != user.uid }
                        partnerDocs.forEach { partnerDoc ->
                            db.collection("users")
                                .document(partnerDoc.id)
                                .collection("alarms")
                                .document(addedDoc.id)
                                .set(alarm.copy(id = addedDoc.id, partnerUid = user.uid))
                                .await()
                        }

                        // Add partner UIDs to duo_alarms participantUids for security rules
                        val partnerUids = partnerDocs.map { it.id }
                        if (partnerUids.isNotEmpty()) {
                            db.collection("duo_alarms")
                                .document(addedDoc.id)
                                .update("participantUids", listOf(user.uid) + partnerUids)
                                .await()
                        }
                    } catch (e: Exception) {
                        // Fail gracefully if partner lookup fails
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAlarm(alarm: AlarmData): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            db.collection("users")
                .document(user.uid)
                .collection("alarms")
                .document(alarm.id)
                .set(alarm)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleAlarm(alarmId: String, isEnabled: Boolean): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            // 1. Update own alarm subcollection
            db.collection("users")
                .document(user.uid)
                .collection("alarms")
                .document(alarmId)
                .update("isEnabled", isEnabled)
                .await()
                
            // 2. Query alarm to propagate to partners
            val alarmDoc = db.collection("users")
                .document(user.uid)
                .collection("alarms")
                .document(alarmId)
                .get()
                .await()
                
            val partnerUsername = alarmDoc.getString("partnerUsername")
            val mode = alarmDoc.getString("mode") ?: "Solo"
            
            if (mode != "Solo" && !partnerUsername.isNullOrBlank()) {
                // Update shared duo_alarm doc status
                try {
                    db.collection("duo_alarms")
                        .document(alarmId)
                        .update("isEnabled", isEnabled)
                        .await()
                } catch (_: Exception) {}

                // Propagate status change to partners' alarms lists
                try {
                    val usernamesList = partnerUsername.split(",")
                    val partnerQuery = db.collection("users")
                        .whereIn("username", usernamesList)
                        .get()
                        .await()
                        
                    val partnerDocs = partnerQuery.documents.filter { it.id != user.uid }
                    partnerDocs.forEach { partnerDoc ->
                        db.collection("users")
                            .document(partnerDoc.id)
                            .collection("alarms")
                            .document(alarmId)
                            .update("isEnabled", isEnabled)
                            .await()
                    }
                } catch (_: Exception) {}
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            // 1. Query alarm details to retrieve partner information before deletion
            val alarmDoc = db.collection("users")
                .document(user.uid)
                .collection("alarms")
                .document(alarmId)
                .get()
                .await()
                
            val partnerUsername = alarmDoc.getString("partnerUsername")
            val mode = alarmDoc.getString("mode") ?: "Solo"
            
            // 2. Delete own alarm
            db.collection("users")
                .document(user.uid)
                .collection("alarms")
                .document(alarmId)
                .delete()
                .await()
                
            if (mode != "Solo" && !partnerUsername.isNullOrBlank()) {
                // Delete shared duo_alarm doc
                try {
                    db.collection("duo_alarms")
                        .document(alarmId)
                        .delete()
                        .await()
                } catch (_: Exception) {}

                // Propagate delete to partners' alarms lists
                try {
                    val usernamesList = partnerUsername.split(",")
                    val partnerQuery = db.collection("users")
                        .whereIn("username", usernamesList)
                        .get()
                        .await()
                        
                    val partnerDocs = partnerQuery.documents.filter { it.id != user.uid }
                    partnerDocs.forEach { partnerDoc ->
                        db.collection("users")
                            .document(partnerDoc.id)
                            .collection("alarms")
                            .document(alarmId)
                            .delete()
                            .await()
                    }
                } catch (_: Exception) {}
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSoundCatalog(): Flow<List<SoundMetadata>> = callbackFlow {
        val subscription = db.collection("sounds")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val sounds = snapshot?.documents?.mapNotNull { doc ->
                    SoundMetadata(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        url = doc.getString("url") ?: "",
                        category = doc.getString("category") ?: "Solo"
                    )
                } ?: emptyList()
                trySend(sounds)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Helper to seed the database with initial sounds.
     * You can call this once from MainActivity or a Debug menu.
     */
    override suspend fun seedSoundCatalog(): Result<Unit> {
        return try {
            val initialSounds = listOf(
                SoundMetadata("neon_pulse", "Neon Pulse", "https://firebasestorage.googleapis.com/v0/b/wakesync-77f68.appspot.com/o/sounds%2Fneon_pulse.mp3?alt=media", "Solo"),
                SoundMetadata("orbital_drift", "Orbital Drift", "https://firebasestorage.googleapis.com/v0/b/wakesync-77f68.appspot.com/o/sounds%2Forbital_drift.mp3?alt=media", "Chill"),
                SoundMetadata("battle_horn", "Battle Horn", "https://firebasestorage.googleapis.com/v0/b/wakesync-77f68.appspot.com/o/sounds%2Fbattle_horn.mp3?alt=media", "Battle")
            )

            initialSounds.forEach { sound ->
                db.collection("sounds").document(sound.id).set(sound).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsersByUsername(query: String): Result<List<Friend>> {
        return try {
            val snapshot = if (query.isNotBlank()) {
                db.collection("users")
                    .whereGreaterThanOrEqualTo("username", query.trim())
                    .whereLessThanOrEqualTo("username", query.trim() + "\uf8ff")
                    .limit(10)
                    .get()
                    .await()
            } else {
                db.collection("users")
                    .limit(10)
                    .get()
                    .await()
            }

            val currentUid = auth.currentUser?.uid
            val friends = snapshot.documents.mapNotNull { doc ->
                if (doc.id == currentUid) return@mapNotNull null
                val username = doc.getString("username") ?: doc.getString("name") ?: doc.id
                val avatar = doc.getString("avatar") ?: "👤"
                val streak = doc.getLong("streak")?.toInt() ?: 0
                Friend(
                    id = doc.id,
                    name = username,
                    avatar = avatar,
                    streak = streak,
                    status = FriendStatus.ACTIVE
                )
            }
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    override fun listenToDuoAlarm(alarmId: String): Flow<String?> = callbackFlow {
        val subscription = db.collection("duo_alarms")
            .document(alarmId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val winnerUid = snapshot?.getString("winnerUid")
                trySend(winnerUid)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun setDuoAlarmWinner(alarmId: String, winnerUid: String): Result<Unit> {
        return try {
            db.collection("duo_alarms")
                .document(alarmId)
                .update("winnerUid", winnerUid)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resetDuoAlarmWinner(alarmId: String): Result<Unit> {
        return try {
            db.collection("duo_alarms")
                .document(alarmId)
                .update("winnerUid", null)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            try {
                db.collection("duo_alarms")
                    .document(alarmId)
                    .update("winnerUid", "")
                    .await()
                Result.success(Unit)
            } catch (ex: Exception) {
                Result.failure(ex)
            }
        }
    }

    override suspend fun addHabit(habit: Habit): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            val currentUserDoc = db.collection("users").document(user.uid).get().await()
            val currentUsername = currentUserDoc.getString("username") ?: ""
            
            val partnerUsernames = habit.partnerUsername ?: ""
            val allUsernames = if (partnerUsernames.isNotBlank()) {
                partnerUsernames.split(",").map { it.trim() } + currentUsername
            } else emptyList()
            
            val habitId = if (habit.id.isBlank()) "habit_${System.currentTimeMillis()}" else habit.id
            
            val habitMap = hashMapOf(
                "title" to habit.title,
                "iconType" to habit.iconType.name,
                "isDone" to habit.isDone,
                "streak" to habit.streak,
                "frequency" to habit.frequency,
                "reminderTime" to habit.reminderTime,
                "partnerUsername" to partnerUsernames,
                "bondName" to (habit.bondName ?: "")
            )
            
            db.collection("users")
                .document(user.uid)
                .collection("habits")
                .document(habitId)
                .set(habitMap)
                .await()
                
            if (allUsernames.isNotEmpty()) {
                val partnersQuery = db.collection("users")
                    .whereIn("username", allUsernames)
                    .get()
                    .await()
                
                partnersQuery.documents.forEach { partnerDoc ->
                    if (partnerDoc.id != user.uid) {
                        db.collection("users")
                            .document(partnerDoc.id)
                            .collection("habits")
                            .document(habitId)
                            .set(habitMap)
                            .await()
                    }
                }
            }

            if (!habit.bondName.isNullOrBlank()) {
                checkAndCreateBond(
                    habit.bondName,
                    if (habit.partnerUsername.isNullOrBlank()) "Solo" else if (habit.partnerUsername.contains(",")) "Group" else "Duo",
                    user.uid,
                    habit.partnerUsername
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteHabit(habitId: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            val habitDoc = db.collection("users")
                .document(user.uid)
                .collection("habits")
                .document(habitId)
                .get()
                .await()
                
            val partnerUsername = habitDoc.getString("partnerUsername") ?: ""
            
            db.collection("users")
                .document(user.uid)
                .collection("habits")
                .document(habitId)
                .delete()
                .await()
                
            if (partnerUsername.isNotBlank()) {
                val currentUserDoc = db.collection("users").document(user.uid).get().await()
                val currentUsername = currentUserDoc.getString("username") ?: ""
                val allUsernames = partnerUsername.split(",").map { it.trim() } + currentUsername
                
                val partnersQuery = db.collection("users")
                    .whereIn("username", allUsernames)
                    .get()
                    .await()
                    
                partnersQuery.documents.forEach { partnerDoc ->
                    if (partnerDoc.id != user.uid) {
                        db.collection("users")
                            .document(partnerDoc.id)
                            .collection("habits")
                            .document(habitId)
                            .delete()
                            .await()
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateHabit(habit: Habit): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val partnerUsernames = habit.partnerUsername ?: ""
            val currentUserDoc = db.collection("users").document(user.uid).get().await()
            val currentUsername = currentUserDoc.getString("username") ?: ""
            val allUsernames = if (partnerUsernames.isNotBlank()) {
                partnerUsernames.split(",").map { it.trim() } + currentUsername
            } else emptyList()

            val habitMap = hashMapOf(
                "title" to habit.title,
                "iconType" to habit.iconType.name,
                "isDone" to habit.isDone,
                "streak" to habit.streak,
                "frequency" to habit.frequency,
                "reminderTime" to habit.reminderTime,
                "partnerUsername" to partnerUsernames,
                "bondName" to (habit.bondName ?: "")
            )

            db.collection("users")
                .document(user.uid)
                .collection("habits")
                .document(habit.id)
                .set(habitMap)
                .await()

            if (allUsernames.isNotEmpty()) {
                val partnersQuery = db.collection("users")
                    .whereIn("username", allUsernames)
                    .get()
                    .await()

                partnersQuery.documents.forEach { partnerDoc ->
                    if (partnerDoc.id != user.uid) {
                        db.collection("users")
                            .document(partnerDoc.id)
                            .collection("habits")
                            .document(habit.id)
                            .set(habitMap)
                            .await()
                    }
                }
            }

            if (!habit.bondName.isNullOrBlank()) {
                checkAndCreateBond(
                    habit.bondName,
                    if (habit.partnerUsername.isNullOrBlank()) "Solo" else if (habit.partnerUsername.contains(",")) "Group" else "Duo",
                    user.uid,
                    habit.partnerUsername
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordAlarmResult(alarmId: String, mode: String, isWin: Boolean): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val userDoc = db.collection("users").document(user.uid)
            
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                
                val wins = snapshot.getLong("wins")?.toInt() ?: 0
                val losses = snapshot.getLong("losses")?.toInt() ?: 0
                
                val soloStreak = snapshot.getLong("soloAlarmStreak")?.toInt() ?: 0
                val soloWins = snapshot.getLong("soloAlarmWins")?.toInt() ?: 0
                val soloLosses = snapshot.getLong("soloAlarmLosses")?.toInt() ?: 0
                
                val duoStreak = snapshot.getLong("duoAlarmStreak")?.toInt() ?: 0
                val duoWins = snapshot.getLong("duoAlarmWins")?.toInt() ?: 0
                val duoLosses = snapshot.getLong("duoAlarmLosses")?.toInt() ?: 0
                
                val groupStreak = snapshot.getLong("groupAlarmStreak")?.toInt() ?: 0
                val groupWins = snapshot.getLong("groupAlarmWins")?.toInt() ?: 0
                val groupLosses = snapshot.getLong("groupAlarmLosses")?.toInt() ?: 0
                
                val updates = hashMapOf<String, Any>()
                
                if (isWin) {
                    updates["wins"] = wins + 1
                    when (mode) {
                        "Solo" -> {
                            val newStreak = soloStreak + 1
                            updates["soloAlarmStreak"] = newStreak
                            updates["soloAlarmWins"] = soloWins + 1
                            updates["streak"] = newStreak
                        }
                        "Duo" -> {
                            val newStreak = duoStreak + 1
                            updates["duoAlarmStreak"] = newStreak
                            updates["duoAlarmWins"] = duoWins + 1
                            updates["streak"] = newStreak
                        }
                        else -> {
                            val newStreak = groupStreak + 1
                            updates["groupAlarmStreak"] = newStreak
                            updates["groupAlarmWins"] = groupWins + 1
                            updates["streak"] = newStreak
                        }
                    }
                } else {
                    updates["losses"] = losses + 1
                    updates["streak"] = 0 // Reset global streak on loss
                    when (mode) {
                        "Solo" -> {
                            updates["soloAlarmStreak"] = 0
                            updates["soloAlarmLosses"] = soloLosses + 1
                        }
                        "Duo" -> {
                            updates["duoAlarmStreak"] = 0
                            updates["duoAlarmLosses"] = duoLosses + 1
                        }
                        else -> {
                            updates["groupAlarmStreak"] = 0
                            updates["groupAlarmLosses"] = groupLosses + 1
                        }
                    }
                }
                
                transaction.update(userDoc, updates)
            }.await()

            // Update centralized bond stats if named Duo/Group alarm
            try {
                val alarmSnapshot = db.collection("users")
                    .document(user.uid)
                    .collection("alarms")
                    .document(alarmId)
                    .get()
                    .await()
                    
                val bondName = alarmSnapshot.getString("bondName") ?: ""
                val partnerUsername = alarmSnapshot.getString("partnerUsername") ?: ""
                
                if (bondName.isNotBlank()) {
                    val currentUserDoc = userDoc.get().await()
                    val currentUsername = currentUserDoc.getString("username") ?: ""
                    val allUsernames = (if (partnerUsername.isNotBlank()) partnerUsername.split(",").map { it.trim() } else emptyList()) + currentUsername
                    val members = allUsernames.sorted()
                    val bondId = "bond_${(members.joinToString(",") + "_" + bondName).hashCode()}"
                    val bondRef = db.collection("bonds").document(bondId)
                    
                    db.runTransaction { transaction ->
                        val bondSnap = transaction.get(bondRef)
                        if (bondSnap.exists()) {
                            val streak = bondSnap.getLong("alarmStreak")?.toInt() ?: 0
                            val wins = bondSnap.getLong("alarmWins")?.toInt() ?: 0
                            val losses = bondSnap.getLong("alarmLosses")?.toInt() ?: 0
                            
                            val updates = hashMapOf<String, Any>()
                            if (isWin) {
                                updates["alarmStreak"] = streak + 1
                                updates["alarmWins"] = wins + 1
                            } else {
                                updates["alarmStreak"] = 0
                                updates["alarmLosses"] = losses + 1
                            }
                            transaction.update(bondRef, updates)
                        }
                    }.await()
                }
            } catch (e: Exception) {
                // Fail silently
            }

            // Auto-generate social feed post for Duo/Group mode
            if (mode == "Duo" || mode == "Group") {
                try {
                    val userDoc = db.collection("users").document(user.uid).get().await()
                    val username = userDoc.getString("username") ?: "Unknown"
                    val avatar = userDoc.getString("avatarEmoji") ?: "👤"
                    val streak = userDoc.getLong("streak")?.toInt() ?: 0
                    val now = Clock.System.now().toEpochMilliseconds()

                    val content = if (isWin) {
                        when (mode) {
                            "Duo" -> "Won the duo alarm challenge ⚡"
                            "Group" -> "Finished 1st in group alarm 🏆"
                            else -> "Won the alarm challenge"
                        }
                    } else {
                        when (mode) {
                            "Duo" -> "Lost the duo alarm challenge 😴"
                            "Group" -> "Last place in group alarm 💀"
                            else -> "Missed the alarm"
                        }
                    }
                    val badge = if (isWin) "win" else "loss"
                    val badgeColorHex = if (isWin) "#22C55E" else "#FF3D71"

                    db.collection("feed").add(
                        hashMapOf(
                            "userId" to user.uid,
                            "username" to username,
                            "avatar" to avatar,
                            "content" to content,
                            "badgeText" to badge,
                            "badgeColorHex" to badgeColorHex,
                            "streak" to streak,
                            "createdAt" to now,
                            "reactions" to emptyMap<String, Int>()
                        )
                    ).await()

                    // Send push notification to partner
                    val alarmDoc = db.collection("users").document(user.uid)
                        .collection("alarms").document(alarmId).get().await()
                    val partnerUid = alarmDoc.getString("partnerUid") ?: ""

                    if (partnerUid.isNotBlank()) {
                        val pushTitle = if (isWin) "⏰ Partner woke up!" else "😴 Partner missed the alarm!"
                        val pushBody = if (isWin) {
                            "$username woke up on time! Streak: $streak 🔥"
                        } else {
                            "$username missed the alarm! Their streak is reset."
                        }
                        sendPushNotification(
                            toUid = partnerUid,
                            title = pushTitle,
                            body = pushBody,
                            type = if (isWin) "alarm_win" else "alarm_loss",
                            data = mapOf(
                                "alarmId" to alarmId,
                                "mode" to mode,
                                "senderUsername" to username
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Fail silently — feed post and push are non-critical
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun checkAndCreateBond(bondName: String, mode: String, creatorUid: String, partnerUsername: String?) {
        try {
            val currentUserDoc = db.collection("users").document(creatorUid).get().await()
            val currentUsername = currentUserDoc.getString("username") ?: ""
            
            val partners = partnerUsername ?: ""
            val allUsernames = if (partners.isNotBlank()) {
                partners.split(",").map { it.trim() } + currentUsername
            } else listOf(currentUsername)
            
            val members = allUsernames.sorted()
            val bondId = "bond_${(members.joinToString(",") + "_" + bondName).hashCode()}"
            val bondRef = db.collection("bonds").document(bondId)
            
            val snapshot = bondRef.get().await()
            if (!snapshot.exists()) {
                // Look up UIDs for all members to populate participantUids
                val memberUids = mutableListOf(creatorUid)
                if (partners.isNotBlank()) {
                    val partnerUsernames = partners.split(",").map { it.trim() }
                    val partnerDocs = db.collection("users")
                        .whereIn("username", partnerUsernames)
                        .get().await().documents
                    memberUids.addAll(partnerDocs.map { it.id })
                }

                val bondMap = hashMapOf(
                    "name" to bondName,
                    "type" to mode,
                    "members" to members,
                    "participantUids" to memberUids,
                    "alarmStreak" to 0,
                    "alarmWins" to 0,
                    "alarmLosses" to 0,
                    "habitStreak" to 0,
                    "habitWins" to 0,
                    "habitLosses" to 0
                )
                bondRef.set(bondMap).await()
            }
        } catch (e: Exception) {
            // fail silently
        }
    }

    override fun getLeaderboard(mode: String, isGlobal: Boolean): Flow<List<LeaderboardUser>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val sortField = when (mode) {
            "Duo" -> "duoAlarmWins"
            "Group" -> "groupAlarmWins"
            else -> "soloAlarmWins"
        }

        val limitCount = if (isGlobal) 50L else 10L
        val subscription = db.collection("users")
            .orderBy(sortField, Query.Direction.DESCENDING)
            .limit(limitCount)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                var rankIndex = 1
                val users = snapshot?.documents?.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: doc.getString("avatarEmoji") ?: "👤"
                    val wins = doc.getLong(sortField)?.toInt() ?: doc.getLong("wins")?.toInt() ?: 0
                    val streak = when (mode) {
                        "Duo" -> doc.getLong("duoAlarmStreak")?.toInt() ?: 0
                        "Group" -> doc.getLong("groupAlarmStreak")?.toInt() ?: 0
                        else -> doc.getLong("soloAlarmStreak")?.toInt() ?: doc.getLong("streak")?.toInt() ?: 0
                    }
                    val losses = when (mode) {
                        "Duo" -> doc.getLong("duoAlarmLosses")?.toInt() ?: 0
                        "Group" -> doc.getLong("groupAlarmLosses")?.toInt() ?: 0
                        else -> doc.getLong("soloAlarmLosses")?.toInt() ?: doc.getLong("losses")?.toInt() ?: 0
                    }

                    val isUser = (doc.id == currentUserId)
                    LeaderboardUser(
                        rank = rankIndex++,
                        username = if (isUser && !isGlobal) "YOU" else username,
                        avatar = avatar,
                        score = (wins * 100) + (streak * 10),
                        streak = streak,
                        isCurrentUser = isUser,
                        isRedLoss = losses > 3
                    )
                } ?: emptyList()

                trySend(users)
            }

        awaitClose { subscription.remove() }
    }

    private fun selectPunishment(challenge: String, mode: String): Pair<String, String> {
        // Returns (punishmentEmoji, punishmentText) based on challenge and mode
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
        return try {
            val now = Clock.System.now().toEpochMilliseconds()
            val dueAt = now + (2 * 60 * 60 * 1000L) // 2 hours from now
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

            // Write to loser's subcollection (primary record)
            db.collection("users").document(loserUid)
                .collection("punishments").document(punishmentId)
                .set(punishment.toMap()).await()

            // Write to challenger's subcollection so they can see it too
            if (challengerUid.isNotBlank()) {
                try {
                    db.collection("users").document(challengerUid)
                        .collection("punishments").document(punishmentId)
                        .set(punishment.copy(
                            loserUid = challengerUid,
                            loserUsername = challengerUsername,
                            challengerUid = loserUid,
                            challengerUsername = loserUsername
                        ).toMap()).await()
                } catch (_: Exception) {}
            }

            // Send notification to the challenger
            if (challengerUid.isNotBlank()) {
                try {
                    db.collection("users").document(challengerUid)
                        .collection("notifications").add(
                            hashMapOf(
                                "type" to "punishment_assigned",
                                "punishmentId" to punishmentId,
                                "fromUsername" to loserUsername,
                                "fromUid" to loserUid,
                                "message" to "$loserUsername failed the alarm! $emoji $text",
                                "createdAt" to now,
                                "read" to false
                            )
                        ).await()
                } catch (_: Exception) {}
            }

            // Also send notification to the loser confirming their punishment
            try {
                db.collection("users").document(loserUid)
                    .collection("notifications").add(
                        hashMapOf(
                            "type" to "punishment_assigned",
                            "punishmentId" to punishmentId,
                            "fromUsername" to loserUsername,
                            "fromUid" to loserUid,
                            "message" to "You were assigned a punishment: $emoji $text",
                            "createdAt" to now,
                            "read" to false
                        )
                    ).await()
            } catch (_: Exception) {}

            // Send push notification to challenger about the punishment
            if (challengerUid.isNotBlank()) {
                try {
                    sendPushNotification(
                        toUid = challengerUid,
                        title = "🏆 Punishment Assigned",
                        body = "$loserUsername failed! $emoji $text",
                        type = "punishment_assigned",
                        data = mapOf(
                            "punishmentId" to punishmentId,
                            "loserUsername" to loserUsername,
                            "mode" to mode
                        )
                    )
                } catch (_: Exception) {}
            }

            // Send push notification to loser confirming their punishment
            try {
                sendPushNotification(
                    toUid = loserUid,
                    title = "📋 Your Punishment",
                    body = "You were assigned: $emoji $text",
                    type = "punishment_assigned",
                    data = mapOf(
                        "punishmentId" to punishmentId,
                        "mode" to mode
                    )
                )
            } catch (_: Exception) {}

            Result.success(punishment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActivePunishment(): Punishment? {
        return try {
            val user = auth.currentUser ?: return null
            val now = Clock.System.now().toEpochMilliseconds()
            val snapshot = db.collection("users").document(user.uid)
                .collection("punishments")
                .whereEqualTo("status", "Assigned")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(1)
                .get().await()

            snapshot.documents.firstOrNull()?.toPunishment()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun submitProof(punishmentId: String, proofUrl: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val now = Clock.System.now().toEpochMilliseconds()
            db.collection("users").document(user.uid)
                .collection("punishments").document(punishmentId)
                .update(
                    mapOf(
                        "status" to "ProofSubmitted",
                        "proofUrl" to proofUrl,
                        "proofSubmittedAt" to now
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completePunishment(punishmentId: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val now = Clock.System.now().toEpochMilliseconds()
            db.collection("users").document(user.uid)
                .collection("punishments").document(punishmentId)
                .update(
                    mapOf(
                        "status" to "Completed",
                        "completedAt" to now
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── SOCIAL FEED (Real-Time) ───────────────────────────────────────
    override fun getSocialFeed(): Flow<List<FeedPost>> = callbackFlow {
        val subscription = db.collection("feed")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: return@mapNotNull null
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val reactionsMap = (doc.get("reactions") as? Map<*, *>)?.mapNotNull { (k, v) ->
                        val key = k as? String ?: return@mapNotNull null
                        val value = (v as? Number)?.toInt() ?: 0
                        key to value
                    }?.toMap() ?: emptyMap()

                    FeedPost(
                        id = doc.id,
                        userId = userId,
                        username = username,
                        avatar = doc.getString("avatar") ?: doc.getString("avatarEmoji") ?: "👤",
                        content = doc.getString("content") ?: "",
                        badgeText = doc.getString("badgeText") ?: "",
                        badgeColorHex = doc.getString("badgeColorHex") ?: "#22C55E",
                        streak = doc.getLong("streak")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        reactions = reactionsMap
                    )
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun postToFeed(content: String, badge: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val now = Clock.System.now().toEpochMilliseconds()
            val userDoc = db.collection("users").document(user.uid).get().await()
            val username = userDoc.getString("username") ?: "Unknown"
            val avatar = userDoc.getString("avatarEmoji") ?: "👤"
            val streak = userDoc.getLong("streak")?.toInt() ?: 0

            val badgeColorHex = when (badge) {
                "win" -> "#22C55E"
                "streak" -> "#FFD23D"
                "loss" -> "#FF3D71"
                else -> "#00E0FF"
            }

            db.collection("feed").add(
                hashMapOf(
                    "userId" to user.uid,
                    "username" to username,
                    "avatar" to avatar,
                    "content" to content,
                    "badgeText" to badge,
                    "badgeColorHex" to badgeColorHex,
                    "streak" to streak,
                    "createdAt" to now,
                    "reactions" to emptyMap<String, Int>()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── NOTIFICATIONS (Real-Time) ──────────────────────────────────────
    override fun getNotifications(): Flow<List<FeedNotification>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val subscription = db.collection("users").document(user.uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    FeedNotification(
                        id = doc.id,
                        type = doc.getString("type") ?: "",
                        message = doc.getString("message") ?: "",
                        fromUsername = doc.getString("fromUsername") ?: "",
                        fromUid = doc.getString("fromUid") ?: "",
                        punishmentId = doc.getString("punishmentId"),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        read = doc.getBoolean("read") ?: false
                    )
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            db.collection("users").document(user.uid)
                .collection("notifications").document(notificationId)
                .update("read", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── STORIES (Real-Time) ───────────────────────────────────────────
    override fun getStories(): Flow<List<StoryItem>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val subscription = db.collection("stories")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val stories = snapshot?.documents?.mapNotNull { doc ->
                    val userId = doc.getString("userId") ?: ""
                    val username = doc.getString("username") ?: "Unknown"
                    val avatar = doc.getString("avatar") ?: "👤"
                    val caption = doc.getString("caption") ?: ""
                    val badgeText = doc.getString("badgeText") ?: ""
                    val badgeColorHex = doc.getString("badgeColorHex") ?: "#00FF94"
                    val gradientStartHex = doc.getString("gradientStartHex") ?: "#050811"
                    val gradientEndHex = doc.getString("gradientEndHex") ?: "#1A102F"
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val isUser = (userId == currentUserId)

                    StoryItem(
                        id = doc.id,
                        username = username,
                        avatar = avatar,
                        borderColor = androidx.compose.ui.graphics.Color(0xFF00FF94),
                        isUser = isUser,
                        userId = userId,
                        caption = caption,
                        badgeText = badgeText,
                        badgeColorHex = badgeColorHex,
                        gradientStartHex = gradientStartHex,
                        gradientEndHex = gradientEndHex,
                        timestamp = timestamp,
                        hasActiveStory = true
                    )
                } ?: emptyList()

                trySend(stories)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun postStory(
        caption: String,
        badgeText: String,
        badgeColorHex: String,
        bgStartHex: String,
        bgEndHex: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            val now = Clock.System.now().toEpochMilliseconds()
            val userDoc = db.collection("users").document(user.uid).get().await()
            val username = userDoc.getString("username") ?: "You"
            val avatar = userDoc.getString("avatar") ?: userDoc.getString("avatarEmoji") ?: "👤"

            db.collection("stories").add(
                hashMapOf(
                    "userId" to user.uid,
                    "username" to username,
                    "avatar" to avatar,
                    "caption" to caption,
                    "badgeText" to badgeText,
                    "badgeColorHex" to badgeColorHex,
                    "gradientStartHex" to bgStartHex,
                    "gradientEndHex" to bgEndHex,
                    "timestamp" to now
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatTime(timestamp: Long): String {
        val instant = kotlin.time.Instant.fromEpochMilliseconds(timestamp)
        val datetime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
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

    override fun getGroupLeaderboard(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val subscription = db.collection("users")
            .orderBy("groupAlarmWins", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                var rankIndex = 1
                val members = snapshot?.documents?.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: doc.getString("avatarEmoji") ?: "👤"
                    val wins = doc.getLong("groupAlarmWins")?.toInt() ?: doc.getLong("wins")?.toInt() ?: 0
                    val streak = doc.getLong("groupAlarmStreak")?.toInt() ?: doc.getLong("streak")?.toInt() ?: 0
                    val losses = doc.getLong("groupAlarmLosses")?.toInt() ?: doc.getLong("losses")?.toInt() ?: 0
                    val isUser = (doc.id == currentUserId)

                    val calculatedPoints = (wins * 3) + streak
                    GroupMember(
                        rank = rankIndex++,
                        username = if (isUser) "YOU" else username,
                        avatar = avatar,
                        points = calculatedPoints,
                        wins = wins,
                        isCurrentUser = isUser,
                        isRedHighlight = losses > 2
                    )
                } ?: emptyList()

                trySend(members)
            }

        awaitClose { subscription.remove() }
    }

    override suspend fun getNotificationPreferences(): NotificationPreferences {
        return try {
            val user = auth.currentUser ?: return NotificationPreferences()
            val doc = db.collection("users").document(user.uid).get().await()
            NotificationPreferences(
                alarmSoundsEnabled = doc.getBoolean("notifAlarmSounds") ?: true,
                punishmentSoundsEnabled = doc.getBoolean("notifPunishmentSounds") ?: true,
                socialSoundsEnabled = doc.getBoolean("notifSocialSounds") ?: true,
                vibrationEnabled = doc.getBoolean("notifVibration") ?: true,
                soundVolume = doc.getDouble("notifSoundVolume")?.toFloat() ?: 1.0f,
                quietHoursStart = doc.getLong("notifQuietHoursStart")?.toInt() ?: -1,
                quietHoursEnd = doc.getLong("notifQuietHoursEnd")?.toInt() ?: -1
            )
        } catch (e: Exception) {
            NotificationPreferences()
        }
    }

    override suspend fun updateNotificationPreferences(prefs: NotificationPreferences): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            db.collection("users").document(user.uid).update(
                mapOf(
                    "notifAlarmSounds" to prefs.alarmSoundsEnabled,
                    "notifPunishmentSounds" to prefs.punishmentSoundsEnabled,
                    "notifSocialSounds" to prefs.socialSoundsEnabled,
                    "notifVibration" to prefs.vibrationEnabled,
                    "notifSoundVolume" to prefs.soundVolume.toDouble(),
                    "notifQuietHoursStart" to prefs.quietHoursStart.toLong(),
                    "notifQuietHoursEnd" to prefs.quietHoursEnd.toLong()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerDeviceToken(token: String, platform: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
            
            // Store FCM token in user document for push notifications
            db.collection("users").document(user.uid)
                .update(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to Clock.System.now().toEpochMilliseconds(),
                        "platform" to platform
                    )
                ).await()
            
            // Also store in tokens subcollection for multi-device support
            db.collection("users").document(user.uid)
                .collection("tokens").document(token)
                .set(
                    mapOf(
                        "token" to token,
                        "platform" to platform,
                        "createdAt" to Clock.System.now().toEpochMilliseconds(),
                        "active" to true
                    )
                ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPushNotification(
        toUid: String,
        title: String,
        body: String,
        type: String,
        data: Map<String, String>
    ): Result<Unit> {
        return try {
            // Read target user's FCM token from Firestore
            val targetDoc = db.collection("users").document(toUid).get().await()
            val fcmToken = targetDoc.getString("fcmToken")
            
            if (fcmToken.isNullOrBlank()) {
                // No token available — user hasn't registered for push yet
                return Result.success(Unit)
            }
            
            // Write a push notification document that Cloud Functions will pick up
            db.collection("push_queue").add(
                mapOf(
                    "toUid" to toUid,
                    "fcmToken" to fcmToken,
                    "title" to title,
                    "body" to body,
                    "type" to type,
                    "data" to data,
                    "createdAt" to Clock.System.now().toEpochMilliseconds(),
                    "processed" to false
                )
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Push notification failure is non-critical
            Result.success(Unit)
        }
    }
}

actual fun getHomeRepository(): HomeRepository = AndroidHomeRepository()

private fun Punishment.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "loserUid" to loserUid,
    "loserUsername" to loserUsername,
    "challengerUid" to challengerUid,
    "challengerUsername" to challengerUsername,
    "alarmId" to alarmId,
    "mode" to mode,
    "challenge" to challenge,
    "punishmentType" to punishmentType,
    "punishmentEmoji" to punishmentEmoji,
    "punishmentDetail" to punishmentDetail,
    "status" to status,
    "createdAt" to createdAt,
    "dueAt" to dueAt,
    "proofUrl" to proofUrl,
    "proofSubmittedAt" to proofSubmittedAt,
    "completedAt" to completedAt
)

private fun com.google.firebase.firestore.DocumentSnapshot.toPunishment(): Punishment = Punishment(
    id = getString("id") ?: "",
    loserUid = getString("loserUid") ?: "",
    loserUsername = getString("loserUsername") ?: "",
    challengerUid = getString("challengerUid") ?: "",
    challengerUsername = getString("challengerUsername") ?: "",
    alarmId = getString("alarmId") ?: "",
    mode = getString("mode") ?: "Solo",
    challenge = getString("challenge") ?: "",
    punishmentType = getString("punishmentType") ?: "",
    punishmentEmoji = getString("punishmentEmoji") ?: "",
    punishmentDetail = getString("punishmentDetail") ?: "",
    status = getString("status") ?: "Assigned",
    createdAt = getLong("createdAt") ?: 0L,
    dueAt = getLong("dueAt") ?: 0L,
    proofUrl = getString("proofUrl"),
    proofSubmittedAt = getLong("proofSubmittedAt"),
    completedAt = getLong("completedAt")
)
