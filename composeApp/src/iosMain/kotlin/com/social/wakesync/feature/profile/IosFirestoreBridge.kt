package com.social.wakesync.feature.profile

/**
 * Bridge interface that Swift implements to provide Firestore operations to Kotlin.
 * This follows the same bridging pattern used for Google Sign-In.
 */
interface IosFirestoreBridge {
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
}

object IosFirestoreBridgeHolder {
    var bridge: IosFirestoreBridge? = null
}
