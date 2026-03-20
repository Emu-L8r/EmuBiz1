package com.emul8r.bizap.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor() {
    /**
     * No-op in release builds. Test data seeding is debug-only.
     */
    suspend fun seedDatabaseIfNeeded() {
        // No-op in release
    }
}

