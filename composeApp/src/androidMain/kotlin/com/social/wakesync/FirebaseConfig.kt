package com.social.wakesync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

const val FIRESTORE_DATABASE_ID = "wakesync"

/**
 * Configure Firestore with offline caching.
 * - Persistent cache: data survives app restarts
 * - Unlimited cache: no size limit
 * - Enabled on Android by default, but we set explicit settings
 */
fun configureFirestoreOffline() {
    try {
        val db = FirebaseFirestore.getInstance(FIRESTORE_DATABASE_ID)
        @Suppress("DEPRECATION")
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        db.firestoreSettings = settings
        Log.d("WakeSync", "Firestore offline persistence enabled (unlimited cache)")
    } catch (e: Exception) {
        // Settings can only be set before first use
        Log.w("WakeSync", "Firestore settings already applied: ${e.message}")
    }
}
