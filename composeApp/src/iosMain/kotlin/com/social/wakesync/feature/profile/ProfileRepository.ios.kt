package com.social.wakesync.feature.profile

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosProfileRepository : ProfileRepository {

    override suspend fun saveProfile(
        username: String,
        avatar: String,
        goal: String
    ): Result<Unit> {
        val bridge = IosFirestoreBridgeHolder.bridge
            ?: return Result.failure(
                Exception("Firestore bridge not configured. Ensure iOS app provides the bridge.")
            )

        return suspendCancellableCoroutine { continuation ->
            bridge.saveProfile(
                username = username,
                avatar = avatar,
                goal = goal,
                onSuccess = {
                    if (continuation.isActive) continuation.resume(Result.success(Unit))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun checkUsername(username: String): Result<Boolean> {
        val bridge = IosFirestoreBridgeHolder.bridge
            ?: return Result.failure(
                Exception("Firestore bridge not configured. Ensure iOS app provides the bridge.")
            )

        return suspendCancellableCoroutine { continuation ->
            bridge.checkUsername(
                username = username,
                onResult = { isAvailable ->
                    if (continuation.isActive) continuation.resume(Result.success(isAvailable))
                },
                onError = { error ->
                    if (continuation.isActive) continuation.resume(Result.failure(Exception(error)))
                }
            )
        }
    }

    override suspend fun getCurrentProfile(): Result<UserProfile?> {
        // For now, placeholder or bridge implementation if available
        return Result.success(null)
    }

    override suspend fun updateAvatar(emoji: String): Result<Unit> {
        // iOS implementation placeholder
        return Result.success(Unit)
    }
}

actual fun getProfileRepository(): ProfileRepository = IosProfileRepository()
