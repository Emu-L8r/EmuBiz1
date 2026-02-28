package com.emul8r.bizap.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

/**
 * Manages network connectivity detection.
 * Emits reactive updates when device goes online/offline.
 */
class NetworkConnectivityManager(private val context: Context) {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Immediate check for internet availability.
     */
    fun isInternetAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Reactive stream of connectivity states.
     */
    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("🌐 Network restored - triggering sync queue")
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                Timber.d("📵 Network lost - entering offline mode")
                trySend(false)
            }
        }
        
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        trySend(isInternetAvailable())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
