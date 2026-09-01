package com.social.wakesync.feature.home

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.social.wakesync.FIRESTORE_DATABASE_ID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AndroidHomeRepository : HomeRepository {
    private val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
    private val auth = FirebaseAuth.getInstance()

    override fun getHabits(): Flow<List<Habit>> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .collection("habits")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
                    close(error)
                    return@addSnapshotListener
                }

                val currentUserId = auth.currentUser?.uid
                val friends = snapshot?.documents?.filter { it.id != currentUserId }?.mapNotNull { doc ->
                    val name = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: "👤"
                    val streak = doc.getLong("streak")?.toInt() ?: 0
                    // Status mapping would ideally come from a more complex logic, 
                    // for now we use a default based on streak activity or similar.
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
            return@callbackFlow
        }

        val subscription = db.collection("users")
            .document(user.uid)
            .collection("alarms")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
                    close(error)
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
                        partnerUid = doc.getString("partnerUid")
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
                val bondMap = hashMapOf(
                    "name" to bondName,
                    "type" to mode,
                    "members" to members,
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
                    close(error)
                    return@addSnapshotListener
                }

                var rankIndex = 1
                val users = snapshot?.documents?.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: "👤"
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

    override fun getGroupLeaderboard(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val subscription = db.collection("users")
            .orderBy("groupAlarmWins", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                var rankIndex = 1
                val members = snapshot?.documents?.mapNotNull { doc ->
                    val username = doc.getString("username") ?: return@mapNotNull null
                    val avatar = doc.getString("avatar") ?: "👤"
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
}

actual fun getHomeRepository(): HomeRepository = AndroidHomeRepository()
