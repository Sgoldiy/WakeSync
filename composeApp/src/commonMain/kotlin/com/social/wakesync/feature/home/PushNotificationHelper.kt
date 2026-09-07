package com.social.wakesync.feature.home

/**
 * Platform-specific FCM token retrieval.
 * Android: Uses FirebaseMessaging.getInstance().token
 * iOS: Uses Messaging.messaging().fcmToken
 */
expect suspend fun getDeviceToken(): String?
