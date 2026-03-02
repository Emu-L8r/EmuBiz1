package com.emul8r.bizap.data.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * HTTP Interceptor that transforms raw HTTP errors into structured business errors.
 */
class ErrorInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        return try {
            val response = chain.proceed(request)
            
            if (!response.isSuccessful) {
                val code = response.code
                Timber.e("🌐 API Error Detected: $code")
                response.close()  // Prevent OkHttp connection pool leak

                throw when (code) {
                    401 -> ApiException.AuthenticationException("Session expired. Please login again.")
                    403 -> ApiException.PermissionException("Access denied.")
                    404 -> ApiException.NotFoundException("Resource not found.")
                    in 400..499 -> ApiException.ClientException("Request failed with code $code")
                    in 500..599 -> ApiException.ServerException("The server is currently unavailable.")
                    else -> ApiException.UnknownException("An unexpected network error occurred.")
                }
            }
            
            response
            
        } catch (e: Exception) {
            when (e) {
                is ApiException -> throw e
                is SocketTimeoutException -> throw ApiException.TimeoutException("Connection timed out.")
                is UnknownHostException -> throw ApiException.NetworkException("No internet connection.")
                is IOException -> throw ApiException.NetworkException("Network communication error.")
                else -> {
                    Timber.e(e, "🔥 Unexpected Network Exception")
                    throw ApiException.UnknownException(e.message ?: "Unknown error.")
                }
            }
        }
    }
}

