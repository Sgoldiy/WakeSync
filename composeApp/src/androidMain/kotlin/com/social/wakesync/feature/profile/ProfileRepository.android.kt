package com.social.wakesync.feature.profile

import com.social.wakesync.FIRESTORE_DATABASE_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidProfileRepository : ProfileRepository {
    private val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
    private val auth = FirebaseAuth.getInstance()

    override suspend fun saveProfile(username: String, avatar: String, goal: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            try {
                val user = auth.currentUser
                if (user == null) {
                    if (continuation.isActive) continuation.resume(Result.failure(Exception("User not authenticated")))
                    return@suspendCancellableCoroutine
                }

                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { documents ->
                        val usernameTakenByAnotherUser = documents.any { it.id != user.uid }
                        if (usernameTakenByAnotherUser) {
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(IllegalStateException("Username is already taken")))
                            }
                            return@addOnSuccessListener
                        }

                        val data = hashMapOf(
                            "uid" to user.uid,
                            "email" to (user.email ?: ""),
                            "authDisplayName" to (user.displayName ?: ""),
                            "username" to username,
                            "avatar" to avatar,
                            "goal" to goal,
                            "streak" to 0,
                            "wins" to 0,
                            "losses" to 0,
                            "rank" to "Rookie",
                            "setupCompleted" to true,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )

                        db.collection("users").document(user.uid)
                            .set(data, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                if (continuation.isActive) continuation.resume(Result.success(Unit))
                            }
                            .addOnFailureListener { exception ->
                                if (continuation.isActive) continuation.resume(Result.failure(exception))
                            }
                            .addOnCanceledListener {
                                if (continuation.isActive) continuation.resume(Result.failure(Exception("Operation canceled")))
                            }
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resume(Result.failure(exception))
                    }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun checkUsername(username: String): Result<Boolean> {
        return suspendCancellableCoroutine { continuation ->
            try {
                db.collection("users")
                    .whereEqualTo("username", username)
                    .get()
                    .addOnSuccessListener { documents ->
                        val isAvailable = documents.isEmpty
                        if (continuation.isActive) continuation.resume(Result.success(isAvailable))
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resume(Result.failure(exception))
                    }
                    .addOnCanceledListener {
                        if (continuation.isActive) continuation.resume(Result.failure(Exception("Operation canceled")))
                    }
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun getCurrentProfile(): Result<UserProfile?> {
        return suspendCancellableCoroutine { continuation ->
            val user = auth.currentUser
            if (user == null) {
                if (continuation.isActive) continuation.resume(Result.success(null))
                return@suspendCancellableCoroutine
            }

            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val profile = UserProfile(
                            username = document.getString("username") ?: "",
                            avatar = document.getString("avatar") ?: "👤",
                            goal = document.getString("goal") ?: "",
                            setupCompleted = document.getBoolean("setupCompleted") ?: false
                        )
                        if (continuation.isActive) continuation.resume(Result.success(profile))
                    } else {
                        if (continuation.isActive) continuation.resume(Result.success(null))
                    }
                }
                .addOnFailureListener { exception ->
                    if (continuation.isActive) continuation.resume(Result.failure(exception))
                }
        }
    }

    override suspend fun updateAvatar(emoji: String): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val user = auth.currentUser
            if (user == null) {
                if (continuation.isActive) continuation.resume(Result.failure(Exception("User not authenticated")))
                return@suspendCancellableCoroutine
            }

            val userDoc = db.collection("users").document(user.uid)
            
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val lastUpdate = snapshot.getTimestamp("lastAvatarUpdate")
                
                if (lastUpdate != null) {
                    val lastUpdateCal = java.util.Calendar.getInstance().apply { time = lastUpdate.toDate() }
                    val today = java.util.Calendar.getInstance()
                    
                    val isSameDay = lastUpdateCal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                                   lastUpdateCal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)
                    
                    if (isSameDay) {
                        throw IllegalStateException("You can only change your avatar once a day")
                    }
                }
                
                transaction.update(userDoc, "avatar", emoji)
                transaction.update(userDoc, "lastAvatarUpdate", com.google.firebase.Timestamp.now())
            }.addOnSuccessListener {
                if (continuation.isActive) continuation.resume(Result.success(Unit))
            }.addOnFailureListener { exception ->
                if (continuation.isActive) continuation.resume(Result.failure(exception))
            }
        }
    }
}

actual fun getProfileRepository(): ProfileRepository = AndroidProfileRepository()
