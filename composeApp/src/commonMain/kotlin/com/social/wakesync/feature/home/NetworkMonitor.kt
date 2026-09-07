package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.Flow

/**
 * Shared interface for monitoring network connectivity.
 * Used to show offline indicators and queue writes when offline.
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    suspend fun awaitOnline()
}

expect fun getNetworkMonitor(): NetworkMonitor
