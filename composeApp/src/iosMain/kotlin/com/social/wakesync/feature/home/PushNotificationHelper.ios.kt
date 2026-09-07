package com.social.wakesync.feature.home

import com.social.wakesync.feature.profile.IosFirestoreBridgeHolder
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * iOS implementation using the Swift bridge to get Firebase FCM token.
 */
actual suspend fun getDeviceToken(): String? = suspendCoroutine { continuation ->
    val bridge = IosFirestoreBridgeHolder.bridge
    if (bridge == null) {
        continuation.resume(null)
        return@suspendCoroutine
    }
    bridge.getFCMToken(
        onResult = { token ->
            continuation.resume(token)
        },
        onError = { _ ->
            continuation.resume(null)
        }
    )
}
