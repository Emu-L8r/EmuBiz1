package com.emul8r.bizap.data.sync

import timber.log.Timber

/**
 * Resolves conflicts between local and server data using Last-Write-Wins.
 */
class ConflictResolver {
    fun resolve(localUpdatedAt: Long, serverUpdatedAt: Long): ConflictWinner {
        return if (serverUpdatedAt > localUpdatedAt) {
            Timber.d("⚖️ Conflict: Server is newer.")
            ConflictWinner.SERVER
        } else {
            Timber.d("⚖️ Conflict: Local is newer or equal.")
            ConflictWinner.LOCAL
        }
    }
}

enum class ConflictWinner { LOCAL, SERVER }
