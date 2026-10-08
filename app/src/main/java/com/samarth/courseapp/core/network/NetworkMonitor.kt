package com.samarth.courseapp.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Connectivity signal, abstracted so the repository/API layer stays testable off-device.
 */
interface NetworkMonitor {

    /** Emits the current connectivity state and every change afterwards. */
    val isOnline: Flow<Boolean>

    /** Point-in-time check, used right before a network call is attempted. */
    suspend fun isCurrentlyOnline(): Boolean
}
