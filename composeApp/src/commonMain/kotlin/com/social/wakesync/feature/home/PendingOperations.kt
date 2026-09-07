package com.social.wakesync.feature.home

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks pending write operations that need to be synced when back online.
 * Operations are persisted to Firestore's cache so they survive app restarts.
 */
object PendingOperations {

    data class PendingOp(
        val id: String,
        val type: String, // "habit_toggle", "alarm_add", "alarm_update", "alarm_delete", "habit_add", "habit_delete"
        val timestamp: Long,
        val payload: Map<String, String> = emptyMap()
    )

    private val _pendingOps = MutableStateFlow<List<PendingOp>>(emptyList())
    val pendingOps: StateFlow<List<PendingOp>> = _pendingOps.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    fun addOperation(op: PendingOp) {
        val current = _pendingOps.value.toMutableList()
        // Deduplicate: if same type+id exists, replace it
        val existingIndex = current.indexOfFirst { it.id == op.id && it.type == op.type }
        if (existingIndex >= 0) {
            current[existingIndex] = op
        } else {
            current.add(op)
        }
        _pendingOps.value = current
        _pendingCount.value = current.size
    }

    fun removeOperation(opId: String) {
        val current = _pendingOps.value.toMutableList()
        current.removeAll { it.id == opId }
        _pendingOps.value = current
        _pendingCount.value = current.size
    }

    fun clearCompleted() {
        _pendingOps.value = emptyList()
        _pendingCount.value = 0
    }

    fun hasPending(): Boolean = _pendingOps.value.isNotEmpty()
}
