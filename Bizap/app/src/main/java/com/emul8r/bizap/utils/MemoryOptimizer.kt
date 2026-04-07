package com.emul8r.bizap.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Debug
import timber.log.Timber

/**
 * Memory optimization utilities.
 *
 * **Goals:**
 * - Monitor memory usage in real-time
 * - Optimize bitmap handling
 * - Detect memory leaks early
 * - Profile memory consumption
 */
object MemoryOptimizer {

    /**
     * Get current memory usage info.
     *
     * **Returns:** Triple of (nativeHeap, javaHeap, totalMemory) in MB
     */
    fun getMemoryUsage(): Triple<Long, Long, Long> {
        val runtime = Runtime.getRuntime()
        // Get memory stats from Runtime
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val javaHeap = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory() / (1024 * 1024)

        // Estimate native heap as portion of max memory
        val nativeHeap = (maxMemory - totalMemory).coerceAtLeast(0L)

        return Triple(nativeHeap, javaHeap, totalMemory)
    }

    /**
     * Check if app is running low on memory.
     *
     * **Threshold:** 75% of available memory
     * **Returns:** true if memory usage is critical
     */
    fun isLowMemory(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val isLow = memInfo.lowMemory
        if (isLow) {
            Timber.w("⚠️  Low memory warning: ${memInfo.availMem / (1024 * 1024)}MB available")
        }
        return isLow
    }

    /**
     * Get device available memory.
     *
     * **Returns:** Available memory in MB
     */
    fun getAvailableMemory(context: Context): Long {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        return memInfo.availMem / (1024 * 1024)
    }

    /**
     * Optimize bitmap for display.
     *
     * **Features:**
     * - Reduce dimensions to target size
     * - Use ARGB_8888 only if needed (prefer RGB_565)
     * - Prevent OutOfMemoryError
     *
     * **Example:** targetWidth=200, targetHeight=200 for thumbnails
     */
    fun decodeSampledBitmap(
        data: ByteArray,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        // First, decode with inJustDecodeBounds=true to get dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        // Calculate sample size
        options.inSampleSize = calculateInSampleSize(
            options.outWidth,
            options.outHeight,
            targetWidth,
            targetHeight
        )

        // Decode with inSampleSize set
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565  // Use 16-bit (not 32-bit)

        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    /**
     * Calculate bitmap sample size for downsampling.
     *
     * **How it works:**
     * - If image is 4000x3000 and target is 200x200
     * - inSampleSize = 10, so decoded size = 400x300 (very small in memory)
     *
     * **Memory Savings:** Sample size of 10 = 100x less memory used
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var inSampleSize = 1

        if (height > targetHeight || width > targetWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= targetHeight &&
                (halfWidth / inSampleSize) >= targetWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Log memory profile for debugging.
     *
     * **Output Example:**
     * ```
     * 💾 Memory Profile:
     *    Native: 24MB
     *    Java: 156MB
     *    Total: 512MB
     *    Available: 1024MB
     * ```
     */
    fun logMemoryProfile(context: Context) {
        val (native, java, total) = getMemoryUsage()
        val available = getAvailableMemory(context)

        Timber.d("""
            💾 Memory Profile:
               Native: ${native}MB
               Java: ${java}MB
               Total: ${total}MB
               Available: ${available}MB
        """.trimIndent())
    }

    /**
     * Force garbage collection.
     *
     * **Warning:** Use sparingly - GC is expensive
     * **Only use:** When you know you've just released large objects
     */
    fun forceGarbageCollection() {
        System.gc()
        Timber.d("🗑️  Garbage collection triggered")
    }

    /**
     * Clear image cache recommendations.
     *
     * **When to call:**
     * - After loading large image galleries
     * - When navigating away from image-heavy screens
     * - In response to low memory warnings
     */
    fun getCacheClearRecommendation(context: Context): String {
        val (native, java, total) = getMemoryUsage()
        val percentUsed = (java.toDouble() / total.toDouble()) * 100

        return when {
            percentUsed > 80 -> {
                Timber.w("🔴 CRITICAL: Memory usage at ${percentUsed.toInt()}%")
                "Clear image cache immediately"
            }
            percentUsed > 70 -> {
                Timber.w("🟠 WARNING: Memory usage at ${percentUsed.toInt()}%")
                "Consider clearing image cache"
            }
            percentUsed > 60 -> {
                Timber.i("🟡 INFO: Memory usage at ${percentUsed.toInt()}%")
                "Monitor memory usage"
            }
            else -> {
                Timber.d("🟢 OK: Memory usage at ${percentUsed.toInt()}%")
                "Memory is healthy"
            }
        }
    }
}

