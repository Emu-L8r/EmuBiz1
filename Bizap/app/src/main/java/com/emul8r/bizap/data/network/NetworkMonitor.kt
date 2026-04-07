package com.emul8r.bizap.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    val networkState: Flow<NetworkState>
}

@Singleton
class ConnectivityNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signalQualityMonitor: SignalQualityMonitor
) : NetworkMonitor {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Connectivity flow (online/type) - updates on network changes
    private val connectivityFlow: Flow<Pair<Boolean, NetworkType>> = callbackFlow {
        fun emitCurrentState() {
            val currentNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(currentNetwork)

            if (caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                trySend(Pair(false, NetworkType.NONE))
                return
            }

            val type = when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
            trySend(Pair(true, type))
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("🌐 Network available")
                emitCurrentState()
            }

            override fun onLost(network: Network) {
                Timber.d("🌐 Network lost")
                trySend(Pair(false, NetworkType.NONE))
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                Timber.d("🌐 Network capabilities changed")
                emitCurrentState()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)
        emitCurrentState()

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()

    // Combine connectivity + signal quality into full NetworkState
    override val networkState: Flow<NetworkState> = combine(
        connectivityFlow,
        signalQualityMonitor.networkQuality
    ) { (isOnline, type), quality ->
        if (!isOnline) {
            Timber.w("🔴 Network: OFFLINE")
            NetworkState.Offline
        } else {
            val emoji = when (quality) {
                NetworkQuality.EXCELLENT -> "🟢"
                NetworkQuality.GOOD -> "🟡"
                NetworkQuality.POOR -> "🔴"
                NetworkQuality.UNKNOWN -> "⚪"
            }
            val typeStr = type.name
            Timber.i("$emoji Network: $typeStr ($quality)")

            NetworkState(
                isOnline = true,
                type = type,
                quality = quality
            )
        }
    }.distinctUntilChanged()

    override val isOnline: Flow<Boolean> = networkState
        .map { it.isOnline }
        .distinctUntilChanged()
}
