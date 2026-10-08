package com.samarth.courseapp.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * [NetworkMonitor] backed by [ConnectivityManager].
 *
 * `NET_CAPABILITY_VALIDATED` is deliberately required: a Wi-Fi network with no actual internet
 * access would otherwise report as online and every request would fail with a timeout instead of a
 * clean "you are offline".
 */
class AndroidNetworkMonitor @Inject constructor(
    private val context: Context,
) : NetworkMonitor {

    private val connectivityManager: ConnectivityManager?
        get() = context.getSystemService()

    override val isOnline: Flow<Boolean> = callbackFlow {
        val manager = connectivityManager
        if (manager == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        // Track networks by id: callbacks can interleave across Wi-Fi/cellular handovers, so a
        // single boolean would flicker offline while the new network is still coming up.
        val validNetworks = mutableSetOf<Network>()

        fun emitCurrentState() {
            trySend(validNetworks.isNotEmpty())
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                validNetworks += network
                emitCurrentState()
            }

            override fun onLost(network: Network) {
                validNetworks -= network
                emitCurrentState()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        manager.registerNetworkCallback(request, callback)

        trySend(currentlyOnline())

        awaitClose { manager.unregisterNetworkCallback(callback) }
    }
        .conflate()
        .distinctUntilChanged()

    override suspend fun isCurrentlyOnline(): Boolean = currentlyOnline()

    private fun currentlyOnline(): Boolean {
        val manager = connectivityManager ?: return false
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
