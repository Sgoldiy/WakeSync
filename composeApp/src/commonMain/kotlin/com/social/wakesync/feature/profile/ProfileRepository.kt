package com.social.wakesync.feature.profile

interface ProfileRepository {
    suspend fun saveProfile(username: String, avatar: String, goal: String): Result<Unit>
    suspend fun checkUsername(username: String): Result<Boolean>
    suspend fun getCurrentProfile(): Result<UserProfile?>
    suspend fun updateAvatar(emoji: String): Result<Unit>
}

data class UserProfile(
    val username: String,
    val avatar: String,
    val goal: String,
    val setupCompleted: Boolean
)

expect fun getProfileRepository(): ProfileRepository
