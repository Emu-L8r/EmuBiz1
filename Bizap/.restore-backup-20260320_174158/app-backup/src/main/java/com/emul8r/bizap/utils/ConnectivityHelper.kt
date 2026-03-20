package com.emul8r.bizap.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import timber.log.Timber

/**
 * Utility to check network connectivity status for Phase 2: Offline-First Reliability.
 */
object ConnectivityHelper {
    
    fun isNetworkAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            capabilities?.let {
                when {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        Timber.d("📱 Network available: Cellular")
                        true
                    }
                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        Timber.d("📡 Network available: WiFi")
                        true
                    }
                    it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        Timber.d("🔌 Network available: Ethernet")
                        true
                    }
                    else -> {
                        Timber.w("❌ Network available but unknown type")
                        false
                    }
                }
            } ?: run {
                Timber.w("❌ No network available")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error checking network connectivity")
            false
        }
    }
}
